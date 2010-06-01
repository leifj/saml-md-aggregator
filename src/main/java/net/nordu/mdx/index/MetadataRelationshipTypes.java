package net.nordu.mdx.index;

import org.neo4j.graphdb.RelationshipType;

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