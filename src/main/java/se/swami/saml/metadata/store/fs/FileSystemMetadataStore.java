package se.swami.saml.metadata.store.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Required;

import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.collector.impl.BasicMetadataCollector;
import se.swami.saml.metadata.store.VCS;
import se.swami.saml.metadata.store.MetadataStore;
import se.swami.saml.metadata.store.StoreBase;
import se.swami.saml.metadata.store.vcs.NullVCS;
import se.swami.saml.metadata.utils.MetadataUtils;
import se.swami.saml.metadata.utils.StreamUtils;
import se.swami.saml.metadata.utils.XMLUtils;

public class FileSystemMetadataStore extends StoreBase implements MetadataStore {

	private static final String TAGS_ATTRIBUTE = "tags";

	private File directory;
	private VCS vcs;

	public void setVCS(VCS vcs) {
		this.vcs = vcs;
	}
	
	public void setDirectory(File directory) {
		this.directory = directory;
		if (!directory.exists())
			directory.mkdir();
	}
	
	public FileSystemMetadataStore() {
		setVCS(new NullVCS()); /* easier than having to test */
	}
	
	private List<String> list() {
		ArrayList<String> ids = new ArrayList<String>();
		for (String f : directory.list()) {
			f = f.replace(".xml", "");
			ids.add(f);
		}
		return ids;
	}
	
	private EntityDescriptorType load(String id) throws MetadataIOException {
		try {
			BasicMetadataCollector collector = new BasicMetadataCollector();
			Collection<EntityDescriptorType> entities;
			File f = file(id);
			FileInputStream fin = new FileInputStream(f);
			entities = collector.processXml(fin, null);
			if (entities == null || entities.size() == 0)
				return null;
			
			if (entities.size() > 1)
				throw new MetadataIOException("File contains multiple entities: "+f.getAbsolutePath());
			
			return entities.iterator().next();
		} catch (Exception ex) {
			throw new MetadataIOException(ex);
		}
	}
	
	public Collection<EntityDescriptorType> fetchByEntityID(String entityID)
			throws MetadataIOException {
		ArrayList<EntityDescriptorType> c = new ArrayList<EntityDescriptorType>();
		EntityDescriptorType e = load(eid2id(entityID));
		c.add(e);
		return c;
	}

	public EntityDescriptorType fetchByID(String id) throws MetadataIOException {
		return load(id);
	}

	public Collection<EntityDescriptorType> fetchByTag(String tag)
			throws MetadataIOException {
		
		ArrayList<EntityDescriptorType> c = new ArrayList<EntityDescriptorType>();
		
		for (String id : list()) {
			EntityDescriptorType entity = load(id);
			if (MetadataUtils.hasAttribute(entity, TAGS_ATTRIBUTE, tag))
				c.add(entity);
		}
		return c;
	}

	protected String fileName(String id) {
		return id+".xml";
	}
	
	protected String eid2id(String entityID) {
		return DigestUtils.shaHex(entityID);
	}
	
	protected String fileName(EntityDescriptorType entity) {
		String id = entity.getID();
		if (id == null) {
			entity.setID(eid2id(entity.getEntityID()));
			id = entity.getID();
		}
		return fileName(id);
	}
	
	protected File file(EntityDescriptorType entity) {
		return new File(directory,fileName(entity));
	}
	
	protected File file(String id) {
		return new File(directory,fileName(id));
	}
	
	public void remove(String id) {
		File entityFile = new File(directory,id+".xml");
		if (entityFile.exists()) {
			entityFile.delete();
			vcs.remove(entityFile,"Removed id");
		}
	}

	public void store(EntityDescriptorType entity) throws MetadataIOException {
		if (isReadOnly())
			throw new MetadataIOException("Read Only Metadata Store");
		
		try {
			File entityFile = new File(directory,fileName(entity));
			boolean isNew = !entityFile.exists();
			FileOutputStream fos = new FileOutputStream(entityFile);
			ByteArrayInputStream bais = new ByteArrayInputStream(XMLUtils.o2b(entity));
			StreamUtils.copyStream(fos,bais);
			fos.flush();
			fos.close();
			vcs.commit(entityFile, "Comitted "+entity.getID(), isNew);
		} catch (Exception ex) {
			throw new MetadataIOException(ex);
		}
	}

	public Collection<EntityDescriptorType> fetchAll()
			throws MetadataIOException {
		ArrayList<EntityDescriptorType> c = new ArrayList<EntityDescriptorType>();
		
		for (String id : list()) {
			c.add(load(id));
		}
		return c;
	}

}
