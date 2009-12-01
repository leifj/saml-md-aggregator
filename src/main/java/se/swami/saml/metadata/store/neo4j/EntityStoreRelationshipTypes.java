package se.swami.saml.metadata.store.neo4j;

import org.neo4j.api.core.RelationshipType;

public enum EntityStoreRelationshipTypes implements RelationshipType {

	HAS_TAG,
	HAS_ORIGIN,
	CONTAINS_ENTITY
	
}
