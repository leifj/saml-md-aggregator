package net.nordu.mdx.signer;

public interface MetadataSignerSelector {

	public SignerInfo findSignerInfo(String[] tags);
	
}