package net.nordu.mdx.store.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

import net.nordu.mdx.scanner.MetadataChange;
import net.nordu.mdx.scanner.MetadataChangeType;
import net.nordu.mdx.store.MetadataStore;
import net.nordu.mdx.store.MetadataStoreContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.saml.metadata.EntityDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorType;

public class FileSystemMetadataStore implements MetadataStore {
	
	private URLMetadataStoreContext context;
	
	public String getDirectory() {
		return context.getUrl().getPath();
	}
	
	public void setContext(URLMetadataStoreContext context) {
		this.context = context;
	}
	
	public URLMetadataStoreContext getContext() {
		return context;
	}
	
	private File getDir() {
		return new File(getDirectory());
	}
	
	public void setContext(MetadataStoreContext c) throws Exception {
		if (c instanceof URLMetadataStoreContext) {
			context = (URLMetadataStoreContext)c;
		} else {
			throw new IllegalArgumentException("Bad MetadataStoreContext implementation");
		}
	}
	
	@Override
	public List<String> listIDs() throws Exception {
		ArrayList<String> ids = new ArrayList<String>();
		File d = getDir();
		if (!d.isDirectory())
			throw new IllegalArgumentException("Not a directory: "+getDirectory());
		
		for (String fn: d.list()) {
			int pos = fn.lastIndexOf(".xml");
			
			if (pos > 0)
				ids.add(fn.substring(0, pos));
		}
		
		return ids;
	}

	@Override
	public EntityDescriptorType load(String id) throws Exception {
		File f = new File(getDir(),id+".xml");
		if (f.exists()) { 
			EntityDescriptorDocument doc = EntityDescriptorDocument.Factory.parse(f);
			EntityDescriptorType entity = doc.getEntityDescriptor();
			entity.setID(id);
			return entity;
		} else {
			return null;
		}
	}

	private File _file(String id) {
		return new File(getDir(),id+".xml");
	}
	
	@Override
	public boolean exists(String id) throws Exception {
		File f = _file(id);
		return f.exists() && f.canRead();
	}

	@Override
	public Calendar lastModified(String id) {
		File f = _file(id);
		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(f.lastModified());
		return t;
	}
	
	@Override
	public TimerTask scanner() {
		final MetadataStore store = this;
		
		return new TimerTask() {
			private final Log log = LogFactory.getLog(this.getClass());
			public void run() {
				try {
					for (String id: store.listIDs()) {
						if (!context.getIndex().exists(id))
							context.getChangeNotifier().notifyChange(new MetadataChange(id, MetadataChangeType.ADD));
						else if (store.lastModified(id).after(context.getIndex().lastModified(id)))
							context.getChangeNotifier().notifyChange(new MetadataChange(id, MetadataChangeType.MODIFY));
					}
					for (String id: context.getIndex().listIDs()) {
						if (!store.exists(id))
							context.getChangeNotifier().notifyChange(new MetadataChange(id, MetadataChangeType.REMOVE));
					}
				} catch (Exception ex) {
					log.warn(ex.getMessage());
				}
			}
		};
	}

}
