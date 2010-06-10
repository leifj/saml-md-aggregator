package net.nordu.mdx.signer.impl;

import net.nordu.mdx.signer.MetadataSignerSelector;
import net.nordu.mdx.signer.SignerInfo;

public class StaticSignerSelector implements MetadataSignerSelector {

	private SignerInfo signerInfo;

	public void setSigneInfo(SignerInfo signerInfo) {
		this.signerInfo = signerInfo;
	}
	
	@Override
	public SignerInfo findSignerInfo(String[] tags) {
		return signerInfo;
	}

}
