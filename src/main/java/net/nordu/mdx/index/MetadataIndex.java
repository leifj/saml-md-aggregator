package net.nordu.mdx.index;

import java.util.List;

import org.w3c.dom.Document;

public interface MetadataIndex {

	public String[] find(String[] tags) throws Exception;
	public void update(String id, Document doc) throws Exception;
	public void remove(String id);
	public List<String> listIDs();
	
}
