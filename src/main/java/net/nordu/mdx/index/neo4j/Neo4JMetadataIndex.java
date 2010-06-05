package net.nordu.mdx.index.neo4j;

import java.util.List;

import net.nordu.mdx.index.MetadataIndex;

import org.neo4j.api.core.NeoService;
import org.neo4j.util.index.NeoIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class Neo4JMetadataIndex implements MetadataIndex {

	@Autowired
	private NeoService neo;
	private NeoIndexService neoIndex;
	
	
	public Neo4JMetadataIndex(NeoService neo) {
		neoIndex = new NeoIndexService(neo);
	}
	
	
	@Override
	public String[] find(String[] tags) throws Exception {
		return new String[] { tags[0] };
	}

	
	
	@Override
	public void update(String id,Document doc) throws Exception {
		
	}

	@Override
	public void remove(String id) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public List<String> listIDs() {
		// TODO Auto-generated method stub
		return null;
	}

}
