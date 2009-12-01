package se.swami.saml.metadata.store.neo4j.domain;

import java.util.Set;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.util.NodeWrapperRelationshipSet;

import se.swami.saml.metadata.store.neo4j.EntityStoreRelationshipTypes;
import se.swami.saml.metadata.store.neo4j.NodeBean;

public class Location extends NodeBean {

	protected Location(NeoService neo, Node node) {
		super(neo, node);
		
		entityDescriptors = new NodeWrapperRelationshipSet<EntityDescriptor>(neo,node,EntityStoreRelationshipTypes.CONTAINS_ENTITY,EntityDescriptor.class);
	}
	
	private Set<EntityDescriptor> entityDescriptors;
	
	public Set<EntityDescriptor> getEntityDescriptors() {
		return entityDescriptors;
	}
	
	public String getUri() {
		return (String)getProperty("uri");
	}
	
	public void setUri(String uri) {
		setIndexedProperty("uri", uri);
	}

}