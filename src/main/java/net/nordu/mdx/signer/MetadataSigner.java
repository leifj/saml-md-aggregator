package net.nordu.mdx.signer;

import org.w3c.dom.Document;

public interface MetadataSigner {

	public Document sign(Document doc, String signerName) throws Exception;
	
}
