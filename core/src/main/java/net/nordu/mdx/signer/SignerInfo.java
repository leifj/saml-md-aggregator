package net.nordu.mdx.signer;

import java.util.Calendar;

public interface SignerInfo {

	/**
	 * 
	 * @return the keystore alias for the signer
	 */
	public String getAlias();
	/**
	 * 
	 * @return the time signed metadat is valid until
	 */
	public Calendar getValidUntil();
	/**
	 * 
	 * @return a string representing cache duration for signed metadata
	 */
	public String getCacheDuration();
	
}
