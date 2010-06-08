package net.nordu.mdx.index.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * 
 * @author leifj
 * 
 * 
 *    entity-node
 *    	entity.entityID: String
 *      entity.entityID-sha1: String
 *      entity.id: String
 *      
 *    attribute-value-node
 *      attribute.value: String
 *    
 *    (entity-node) --hasAttribute{name:String,nameForm:String}--> (attribute-value-node)
 *    
 */

public enum MetadataRelationshipTypes implements RelationshipType {
	HAS_ATTRIBUTE
}