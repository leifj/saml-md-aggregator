package net.nordu.mdx.store.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.nordu.mdx.store.MetadataStore;

import org.apache.commons.lang.StringUtils;
import org.oasis.saml.metadata.EntityDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorType;

public class FileSystemMetadataStore implements MetadataStore {

	private File dir;
	private String directory;
	
	public String getDirectory() {
		return directory;
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
		this.dir = new File(directory);
	}
	
	private File getDir() {
		return dir;
	}
	
	@Override
	public List<String> listIDs() throws Exception {
		File d = getDir();
		if (!d.isDirectory())
			throw new IllegalArgumentException("Not a directory: "+getDirectory());
		
		ArrayList<String> ids = new ArrayList<String>();
		_list(d,ids);
		return ids;
	}
	
	private void _list(File d, List<String> ids) {
		for (File f: d.listFiles(new ExtFilter(".xml"))) {
			if (f.isHidden())
				continue;
			
			if (f.isFile()) {
				String p = StringUtils.removeEnd(f.getAbsolutePath(),".xml");
				String id = StringUtils.removeStart(p,getDir().getAbsolutePath()+File.separator);
				ids.add(id);
			} else if (f.isDirectory()) {
				_list(f,ids);
			}
		}
	}

	@Override
	public EntityDescriptorType load(String id) throws Exception {
		File f = new File(getDir(),id+".xml");
		System.err.println(f);
		if (f.exists()) { 
			EntityDescriptorDocument doc = EntityDescriptorDocument.Factory.parse(f);
			EntityDescriptorType entity = doc.getEntityDescriptor();
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

}
