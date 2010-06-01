/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector;

import java.io.InputStream;
import java.util.Calendar;


public interface MetadataReference {

	public abstract String getLocation();
	public abstract boolean isRemote();
	public abstract InputStream getMetadata() throws MetadataIOException;
	public abstract CertificateValidator getValidator();
	public abstract void setLastUpdate(Calendar lastUpdate);
	
}
