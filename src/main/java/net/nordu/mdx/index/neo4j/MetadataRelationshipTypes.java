package net.nordu.mdx.index.neo4j;

import org.neo4j.api.core.RelationshipType;

/**
 * 
 * @author leifj
 * 
 * 
 *    entity-node
 *    	entityID:
 *      fileName:
 *      
 *    hash-node:
 *    	hashValue: <transform of entityID>
 *      hashType: sha1 
 *    
 *    tag-node
 *    	name:
 *    
 *    (entity-node) --hasTag-->   (tag-node)
 *    										  --associatedEntity-> (entity-node)
 *    (entity-node) --hasHash-->  (hash-node)
 *    
 */

public enum MetadataRelationshipTypes implements RelationshipType {
	HAS_TAG,
	HAS_HASH,
	ASSOCIATED_ENTITY,
}