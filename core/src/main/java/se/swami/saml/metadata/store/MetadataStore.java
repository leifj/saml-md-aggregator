package se.swami.saml.metadata.store;

import java.util.Collection;

import org.oasis.saml.metadata.EntityDescriptorType;

import se.swami.saml.metadata.collector.MetadataIOException;

public interface MetadataStore {

	public void store(EntityDescriptorType entity) throws MetadataIOException;
	public void remove(String id) throws MetadataIOException;
	public EntityDescriptorType fetchByID(String id) throws MetadataIOException;
	public Collection<EntityDescriptorType> fetchByEntityID(String entityID) throws MetadataIOException;
	public Collection<EntityDescriptorType> fetchByTag(String tag) throws MetadataIOException;
	
}
