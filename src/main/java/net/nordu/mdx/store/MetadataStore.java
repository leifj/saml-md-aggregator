package net.nordu.mdx.store;

import org.w3c.dom.Document;

public interface MetadataStore {

	public Document load(String id) throws Exception;
	
}
