package net.nordu.mdx.index;

import org.w3c.dom.Document;

public interface MetadataIndex {

	public Iterable<String> find(String[] tags) throws Exception;
	public void update(String id, Document doc) throws Exception;
	public void remove(String id);
	public Iterable<String> listIDs();
	
}
