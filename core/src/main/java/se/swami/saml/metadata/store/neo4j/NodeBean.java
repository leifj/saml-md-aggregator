package se.swami.saml.metadata.store.neo4j;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.util.NodeWrapperImpl;
import org.neo4j.util.index.IndexService;

public class NodeBean extends NodeWrapperImpl {

	private IndexService indexService;
	
	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}
	
	public NodeBean(NeoService neo, Node node) {
		this(neo,node,null);
	}
	
	public NodeBean(NeoService neo, Node node, IndexService indexService) {
		super(neo, node);
		this.indexService = indexService;
	}
	
	public Object getProperty(String propertyName) {
		return getUnderlyingNode().getProperty(propertyName);
	}

	public void setProperty(String propertyName, Object propertyValue) {
		getUnderlyingNode().setProperty(propertyName, propertyValue);
	}
	
	public void setIndexedProperty(String propertyName, String indexName, Object propertyValue) {
		setProperty(propertyName, propertyValue);
		if (indexService != null)
			indexService.index(getUnderlyingNode(), indexName, propertyValue);
	}
	
	public void setIndexedProperty(String propertyName, Object propertyValue) {
		setIndexedProperty(propertyName, propertyName, propertyValue);
	}
	
}
