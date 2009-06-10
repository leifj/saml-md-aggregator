/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector;

import java.net.URI;

public interface MetadataReference {

	public abstract URI getLocation();
	public abstract CertificateValidator getValidator();
	
}
