/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector;

import java.net.URI;
import java.security.cert.X509Certificate;

import se.swami.saml.metadata.collector.impl.BasicMetadataReference;
import se.swami.saml.metadata.collector.impl.SelfSignedCertificateValidator;

public class MetadataReferenceFactory {

	public static MetadataReference instance(String uri, X509Certificate signer) throws MetadataCollectorException {
		try {
			BasicMetadataReference ref = new BasicMetadataReference();
			if (signer != null)
				ref.setValidator(new SelfSignedCertificateValidator(signer));
			ref.setLocation(URI.create(uri));
			
			return ref;
		} catch (Exception ex) {
			throw new MetadataCollectorException(ex);
		}
	}
	
	public static MetadataReference instance(String uri) throws MetadataCollectorException {
		return instance(uri,null);
	}
	
}
