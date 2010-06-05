package net.nordu.mdx.store;

import java.util.List;

import org.w3c.dom.Document;

public interface MetadataStore {

	public List<String> listIDs() throws Exception;
	public Document load(String id) throws Exception;
	
}
