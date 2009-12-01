package se.swami.saml.metadata.store.neo4j;

import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.api.core.NeoService;
import org.neo4j.util.index.IndexService;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.store.MetadataStore;
import se.swami.saml.metadata.store.StoreBase;
import se.swami.saml.metadata.store.neo4j.domain.EntityDescriptor;
import se.swami.saml.metadata.store.neo4j.domain.Location;
import se.swami.saml.metadata.utils.MetadataUtils;

public class NeoStore extends StoreBase implements MetadataStore {
	
	@Autowired
	private NeoService neoService;
	@Autowired
	private IndexService indexService;

	@Autowired
	@Qualifier("entityDescriptorFactory")
	private NodeBeanFactory<EntityDescriptor> entityDescriptorFactory;
	
	@Autowired
	@Qualifier("locationFactory")
	private NodeBeanFactory<Location> locationFactory;
	
	public void setEntityDescriptorFactory(
			NodeBeanFactory<EntityDescriptor> entityDescriptorFactory) {
		this.entityDescriptorFactory = entityDescriptorFactory;
	}
	
	public void setLocationFactory(NodeBeanFactory<Location> locationFactory) {
		this.locationFactory = locationFactory;
	}
	
	public Collection<EntityDescriptorType> fetchByEntityID(String entityID) 
		throws MetadataIOException {
		ArrayList<EntityDescriptorType> entities = new ArrayList<EntityDescriptorType>();
		try {
			for (EntityDescriptor entity : entityDescriptorFactory.findByProperty("entityID", entityID)) {
				entities.add(entity.getEntityDescriptorType());
			}
		} catch (Exception ex) {
			throw new MetadataIOException(ex);
		}
			
		return entities;
	}

	public EntityDescriptorType fetchByID(String id) throws MetadataIOException {
		try {
			EntityDescriptor entity = entityDescriptorFactory.getByProperty("ID", id);
			
			return entity.getEntityDescriptorType();
		} catch (Exception ex) {
			throw new MetadataIOException(ex);
		}
	}

	public Collection<EntityDescriptorType> fetchByTag(String tag) throws MetadataIOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void addTag(String id, String tag) {
		
	}

	public void remove(String id) throws MetadataIOException {
		// TODO Auto-generated method stub
	}

	@Transactional
	public void store(EntityDescriptorType entity) throws MetadataIOException {
		if (isReadOnly())
			throw new MetadataIOException("Read Only Metadata Store");
		
		try {
			EntityDescriptor e = entityDescriptorFactory.findOrCreate("ID",entity.getID());
			e.setEntityID(entity.getEntityID());
			e.setEntityDesciptorType(entity);
			
			for (String origin: MetadataUtils.getOrigin(entity)) {
				Location loc = locationFactory.findOrCreate("url", origin);
				loc.getEntityDescriptors().add(e);
				e.getLocations().add(loc);
			}
			
		} catch (Exception ex) {
			throw new MetadataIOException(ex);
		}
	}

	public Collection<EntityDescriptorType> fetchAll()
			throws MetadataIOException {
		// TODO Auto-generated method stub
		return null;
	}

}
