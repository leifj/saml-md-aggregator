package net.nordu.mdx.signer.impl;

import net.nordu.mdx.signer.MetadataSignerSelector;

public class StaticSignerSelector implements MetadataSignerSelector {

	private String signerName;

	public void setSignerName(String signerName) {
		this.signerName = signerName;
	}
	
	@Override
	public String findSignerName(String[] tags) {
		return signerName;
	}

}
