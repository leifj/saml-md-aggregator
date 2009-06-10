/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector;

import java.security.cert.X509Certificate;

public interface CertificateValidator {

	public abstract void validate(X509Certificate certificate) throws Exception;
	
}
