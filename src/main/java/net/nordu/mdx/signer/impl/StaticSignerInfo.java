package net.nordu.mdx.signer.impl;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import net.nordu.mdx.signer.SignerInfo;

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
		setValidity(Integer.parseInt(validityStr));
	}
	
	public void setValidity(int validity) {
		this.validity = validity;
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
