package se.swami.saml.metadata.store.chain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.oasis.saml.metadata.EntityDescriptorType;

import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.store.MetadataStore;
import se.swami.saml.metadata.store.StoreBase;

public class ChainStore extends StoreBase implements MetadataStore {

	private List<MetadataStore> chain;

	public void setChain(List<MetadataStore> chain) {
		this.chain = chain;
	}
	
	public Collection<EntityDescriptorType> fetchByEntityID(String entityID)
			throws MetadataIOException {
		
		for (MetadataStore store : chain) {
			Collection<EntityDescriptorType> c = store.fetchByEntityID(entityID);
			if (c != null && c.size() > 0)
				return c;
		}
		
		return null;
	}

	public EntityDescriptorType fetchByID(String id) throws MetadataIOException {
		for (MetadataStore store : chain) {
			EntityDescriptorType entity = store.fetchByID(id);
			if (entity != null)
				return entity;
		}
		
		return null;
	}

	public Collection<EntityDescriptorType> fetchByTag(String tag)
			throws MetadataIOException {
		
		for (MetadataStore store : chain) {
			Collection<EntityDescriptorType> c = store.fetchByTag(tag);
			if (c != null && c.size() > 0)
				return c;
		}
		
		return null;
	}

	public void remove(String id) throws MetadataIOException {
		for (MetadataStore store : chain) {
			EntityDescriptorType entity = store.fetchByID(id);
			if (entity != null)
				store.remove(id);
		}
	}

	public void store(EntityDescriptorType entity) throws MetadataIOException {
		if (isReadOnly())
			throw new MetadataIOException("Read Only Metadata Store");
		
		MetadataIOException lastException = null;
		for (MetadataStore store : chain) {
			try {
				store.store(entity);
				return;
			} catch (MetadataIOException ex) {
				lastException = ex;
				ex.printStackTrace();
			}
		}
		throw lastException;
	}

	public Collection<EntityDescriptorType> fetchAll()
			throws MetadataIOException {
		ArrayList<EntityDescriptorType> c = new ArrayList<EntityDescriptorType>();
		for (MetadataStore store : chain) {
			Collection<EntityDescriptorType> sc = store.fetchAll();
			if (sc != null && sc.size() > 0)
				c.addAll(sc);
		}
		return c;
	}
}
