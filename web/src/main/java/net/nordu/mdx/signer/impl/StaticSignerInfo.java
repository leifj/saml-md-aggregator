package net.nordu.mdx.signer.impl;

import java.util.Calendar;

import net.nordu.mdx.signer.SignerInfo;

import org.apache.commons.lang.StringUtils;

public class StaticSignerInfo implements SignerInfo {

	private String alias;
	private String cacheDuration;
	private int validity = -1;
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public void setCacheDuration(String cacheDuration) {
		if (!StringUtils.isEmpty(cacheDuration))
			this.cacheDuration = cacheDuration;
	}
	
	public void setValidity(String validityStr) {
		if (!StringUtils.isEmpty(validityStr))
			this.validity = Integer.parseInt(validityStr);
	}
	
	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public String getCacheDuration() {
		return cacheDuration;
	}

	@Override
	public Calendar getValidUntil() {
		if (validity == -1) {
			return null;
		} else {
			Calendar validUntil = Calendar.getInstance();
			validUntil.add(Calendar.SECOND, validity);
			return validUntil;
		}
	}

}
