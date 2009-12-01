/*
 * Created on Apr 24, 2009
 *
 */
package se.swami.saml.metadata.collector.impl;

import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import se.swami.saml.metadata.collector.MetadataCollectorException;

public class SelfSignedCertificateValidator extends PKIXCertificateValidator {
	
	public SelfSignedCertificateValidator(X509Certificate cert) {
		super(cert);
	}
	
	public void validate(X509Certificate certificate) throws MetadataCollectorException {
		final byte[] myKeyBytes = certificate.getPublicKey().getEncoded();
		boolean inSet = CollectionUtils.exists(getTrustAnchors(), new Predicate() {
			public boolean evaluate(Object object) {
				try {
					byte[] trustedKeyBytes = ((TrustAnchor)object).getTrustedCert().getPublicKey().getEncoded();
					
					if (myKeyBytes.length != trustedKeyBytes.length) {
						//System.err.println("lengths differ "+myKeyBytes.length+" != "+trustedKeyBytes.length);
						return false;
					}
					
					System.err.println(myKeyBytes.length+" bytes");
					for (int i = 0; i < myKeyBytes.length; i++) {
						if (myKeyBytes[i] != trustedKeyBytes[i]) {
							//System.err.println("byte "+i+" differs "+myKeyBytes[i]+" != "+trustedKeyBytes[i]);
							return false;
						}
					}
					
					return true;
				} catch (Exception ex) {
					ex.printStackTrace();
					return false;
				}
			}
		});
		
		if (!inSet)
			throw new MetadataCollectorException("Signer not in trusted set");
	}

}
