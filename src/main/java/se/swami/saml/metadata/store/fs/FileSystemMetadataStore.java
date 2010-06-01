package se.swami.saml.metadata.store.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;

import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.collector.MetadataReference;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.store.MetadataStore;
import se.swami.saml.metadata.store.StoreBase;
import se.swami.saml.metadata.store.VCS;
import se.swami.saml.metadata.store.vcs.NullVCS;
import se.swami.saml.metadata.utils.MetadataUtils;
import se.swami.saml.metadata.utils.StreamUtils;
import se.swami.saml.metadata.utils.XMLUtils;

public class FileSystemMetadataStore extends StoreBase implements MetadataStore {

	@Autowired
	private MetadataCollector collector;
	private static final String TAGS_ATTRIBUTE = "tags";

	private File directory;
	private VCS vcs;
	private Hashtable<String, Set<String>> tag2ID;
	private Hashtable<String, Set<String>> entityID2ID;

	public void setVCS(VCS vcs) {
		this.vcs = vcs;
	}
	
	public void setDirectory(File directory) throws MetadataIOException {
		this.directory = directory;
		if (!directory.exists())
			directory.mkdir();
		rescan();
	}
	
	private void setIndex(Hashtable<String, Set<String>> index, String key, String value) {
		Set<String> values = index.get(key);
		if (values == null) {
			values = new HashSet<String>();
			index.put(key, values);
		}
		values.add(value);
	}
	
	private synchronized void rescan() throws MetadataIOException {
		tag2ID = new Hashtable<String, Set<String>>();
		entityID2ID = new Hashtable<String, Set<String>>();
		for (String id : list()) {
			EntityDescriptorType entity = load(id);
			if (entity != null)
				indexEntity(entity);
		}
	}

	private void indexEntity(EntityDescriptorType entity)
			throws MetadataIOException {
		String[] tags = MetadataUtils.getAttribute(entity, TAGS_ATTRIBUTE);
		if (tags != null && tags.length > 0) {
			for (String tag : tags) {
				setIndex(tag2ID,tag,entity.getID());
			}
		}
		setIndex(entityID2ID,entity.getID(),entity.getID());
	}

	private void removeFromIndex(Hashtable<String, Set<String>> index, String value) {
		for (Set<String> values : tag2ID.values()) {
			values.remove(value);
		}
	}
	
	private void unIndexEntity(String id) {
		removeFromIndex(tag2ID,id);
		removeFromIndex(entityID2ID,id);
	}
	
	public FileSystemMetadataStore() throws MetadataIOException {
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
			File metadataFile = file(id);
			if (!metadataFile.exists()) {
				unIndexEntity(id);
				return null;
			}
			
			boolean invalidateIndex = vcs.update(metadataFile);
			Collection<EntityDescriptorType> entities;
			MetadataReference ref = MetadataReferenceFactory.instance(file(id));
			entities = collector.fetch(ref);
			if (entities == null || entities.size() == 0)
				return null;
			
			if (entities.size() > 1)
				throw new MetadataIOException("Metadata contains multiple entities: "+ref.getLocation());
			
			EntityDescriptorType entity = entities.iterator().next();
			if (invalidateIndex)
				indexEntity(entity);
			
			return entity;
		} catch (Exception ex) {
			throw new MetadataIOException(ex);
		}
	}
	
	public Collection<EntityDescriptorType> fetchByEntityID(String entityID)
			throws MetadataIOException {
		ArrayList<EntityDescriptorType> c = new ArrayList<EntityDescriptorType>();
		Set<String> ids = entityID2ID.get(entityID);
		if (ids != null && ids.size() > 0) {
			for (String id : ids) {
				EntityDescriptorType e = load(id);
				c.add(e);
			}
		}
		return c;
	}

	public EntityDescriptorType fetchByID(String id) throws MetadataIOException {
		return load(id);
	}

	public Collection<EntityDescriptorType> fetchByTag(String tag)
			throws MetadataIOException {
		
		ArrayList<EntityDescriptorType> c = new ArrayList<EntityDescriptorType>();
		Set<String> ids = tag2ID.get(tag);
		if (ids != null && ids.size() > 0) {
			for (String id : ids) {
				EntityDescriptorType entity = load(id);
				if (entity != null)
					c.add(entity);
			}
		}
		return c;
	}

	protected String fileName(String id) {
		return id+".xml";
	}
	
	protected String fileName(EntityDescriptorType entity) {
		return fileName(entity.getID());
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
			EntityDescriptorType entity = load(id);
			if (entity != null)
				c.add(entity);
		}
		return c;
	}

	public Collection<String> listAll() throws MetadataIOException {
		return list();
	}
	
	public Collection<String> listByTag(String tag)
		throws MetadataIOException {

		ArrayList<String> c = new ArrayList<String>();
		Set<String> ids = tag2ID.get(tag);
		if (ids != null && ids.size() > 0) {
			for (String id : ids) {
				EntityDescriptorType entity = load(id);
				if (entity != null)
					c.add(entity.getID());
			}
		}
		return c;
	}

}
