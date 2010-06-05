package net.nordu.mdx.index;

import org.w3c.dom.Document;

public interface MetadataIndex {

	public String[] find(String[] tags) throws Exception;
	public void add(String id, Document doc) throws Exception;
	public boolean exists(String id) throws Exception;
	public void remove(String id);
	
}
