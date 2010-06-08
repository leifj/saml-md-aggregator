package net.nordu.mdx.index.neo4j;

import java.util.ArrayList;
import java.util.Collection;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.utils.MetadataUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.index.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

public class Neo4JMetadataIndex implements MetadataIndex {

	private static final String ENTITY_ID = "entity.id";
	private static final String ATTRIBUTE_VALUE = "attribute.value";
	private static final String ATTRIBUTE_NAME = "attribute.name";
	private static final String ATTRIBUTE_NAME_FORMAT = "attribute.nameFormat";
	private static final String NF_UNSPECIFIED = "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified";
	private static final String NF_NONE = "";
	private static final String NF_INTERNAL = "internal";

	@Autowired
	private GraphDatabaseService neoService;
	
	@Autowired
	private IndexService indexService;
	
	public GraphDatabaseService getNeoService() {
		return neoService;
	}
	
	@Override
	public Iterable<String> find(String[] tags) throws Exception {
		Collection<Node> nodes = getNodesByAttribute(NF_NONE, "tags", tags[0]);
		ArrayList<String> ids = new ArrayList<String>();
		for (Node n: nodes) {
			if (hasAttributes(n,NF_NONE,"tags",tags))
				ids.add((String)n.getProperty(ENTITY_ID));
		}
		return ids;
	}
	
	private void _set(Node n, String key, Object value) {
		n.setProperty(key, value);
		indexService.index(n,key,value);
	}
	
	private boolean hasAttributes(Node entityNode, String nameFormat, String name, String[] values) {
		for (String v: values) {
			if (!hasAttribute(entityNode,nameFormat,name,v))
				return false;
		}
		return true;
	}
	
	private boolean hasAttribute(Node entityNode, String nameFormat, String name,String value) {
		for (Relationship r : entityNode.getRelationships(MetadataRelationshipTypes.HAS_ATTRIBUTE,Direction.OUTGOING)) {
			Node vn = r.getEndNode();
			if (isAttribute(r,nameFormat,name) && vn.getProperty(ATTRIBUTE_VALUE).equals(value))
				return true;
		}
		return false;
	}
	
	private boolean isAttribute(Relationship r, String nameFormat, String name) {
		return r.getProperty(ATTRIBUTE_NAME_FORMAT).equals(nameFormat) && r.getProperty(ATTRIBUTE_NAME).equals(name);
	}
	
	@Override
	public void update(String id,Document doc) throws Exception {
		assert(id != null && id.length() > 0);
		Transaction tx = neoService.beginTx();
		try {
			Node entityNode = indexService.getSingleNode(ENTITY_ID, id);
			if (entityNode == null) {
				entityNode = neoService.createNode();
				_set(entityNode,ENTITY_ID,id);
			}
	
			/*
			 * TODO: make this a bit more effective perhaps - not sure if it is worth it though...
			 */
			for (Relationship r : entityNode.getRelationships(MetadataRelationshipTypes.HAS_ATTRIBUTE,Direction.OUTGOING)) {
				r.delete();
			}
			
			String entityID = MetadataUtils.getEntityID(doc);
			addAttribute(entityNode,NF_INTERNAL,"entityID",entityID, false);
			addAttribute(entityNode,NF_INTERNAL,"entityIDHash","{sha1}"+DigestUtils.shaHex(entityID), false);
			
			final Node n = entityNode;
			MetadataUtils.withAttributes(doc, new MetadataUtils.AttributeCallback() {
				
				@Override
				public void attribute(String nameFormat, String name, String value) {
					addAttribute(n,nameFormat,name,value, false);
				}
			});
			
			addAttribute(entityNode,NF_NONE, "tags", "all", false);
			
			tx.success();
		} finally {
			tx.finish();
		}
	}

	@Override
	public void remove(String id) {
		assert(id != null && id.length() > 0);
		Transaction tx = neoService.beginTx();
		try {
			Node entityNode = indexService.getSingleNode(ENTITY_ID, id);
			if (entityNode != null)
				entityNode.delete();
			//TODO: do reference counting on the value nodes and remove non-used ones.
		} finally {
			tx.finish();
		}
	}

	@Override
	public Iterable<String> listIDs() {
		ArrayList<String> ids = new ArrayList<String>();
		for (Node n : getNodesByAttribute(NF_NONE, "tags", "all")) {
			String id = (String)n.getProperty(ENTITY_ID);
			assert(id != null && id.length() > 0);
			ids.add(id);
		}
		return ids;
	}
	
	private Relationship getAttributeValueRelationship(Node entityNode, String nameFormat, String name, Node valueNode) {
		for (Relationship r : entityNode.getRelationships(MetadataRelationshipTypes.HAS_ATTRIBUTE,Direction.OUTGOING)) {
			if (r.getEndNode().equals(valueNode) && isAttribute(r,nameFormat,name))
				return r;
		}
		return null;
	}
	
	private void addAttribute(Node entityNode, String nameFormat, String name, String value) {
		addAttribute(entityNode, nameFormat, name, value, true);
	}
	
	private void addAttribute(Node entityNode, String nameFormat, String name, String value, boolean checkForDuplicates) {
		Node valueNode = indexService.getSingleNode(ATTRIBUTE_VALUE, value);
		if (valueNode == null) {
			valueNode = neoService.createNode();
			_set(valueNode,ATTRIBUTE_VALUE, value);
		}
		
		if (checkForDuplicates && getAttributeValueRelationship(entityNode, nameFormat, name, valueNode) == null) {
			Relationship r = entityNode.createRelationshipTo(valueNode, MetadataRelationshipTypes.HAS_ATTRIBUTE);
			r.setProperty(ATTRIBUTE_NAME_FORMAT, nameFormat);
			r.setProperty(ATTRIBUTE_NAME, name);
		}
	}
	
	/*
	 *  This is not a directory server. The attribute lookup is optimized for a sparse attribute-value set
	 */
	//@Cacheable(cacheName = "getNodesByAttributeCache")
	public Collection<Node> getNodesByAttribute(String nameFormat, String name, String value)
	{
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node n : indexService.getNodes(ATTRIBUTE_VALUE, value)) {
			for (Relationship r: n.getRelationships(MetadataRelationshipTypes.HAS_ATTRIBUTE, Direction.INCOMING)) {
				if (r.getProperty(ATTRIBUTE_NAME_FORMAT).equals(nameFormat) && r.getProperty(ATTRIBUTE_NAME).equals(name))
					nodes.add(r.getStartNode());
			}
		}
		return nodes;
	}
}
