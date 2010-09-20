package net.nordu.mdx.signer;

import org.w3c.dom.Node;

public interface MetadataSigner {

	public void sign(Node doc, String signerName) throws Exception;
	
}
