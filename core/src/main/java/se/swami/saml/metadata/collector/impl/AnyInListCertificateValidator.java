/*
 * Created on Apr 24, 2009
 *
 */
package se.swami.saml.metadata.collector.impl;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import se.swami.saml.metadata.collector.CertificateValidator;
import se.swami.saml.metadata.collector.MetadataCollectorException;

public class AnyInListCertificateValidator implements CertificateValidator {

	private List<CertificateValidator> validators;
	
	public AnyInListCertificateValidator() {
		validators = new ArrayList<CertificateValidator>();
	}
	
	public void addValidator(CertificateValidator validator) {
		validators.add(validator);
	}
	
	public void validate(X509Certificate certificate) throws Exception {
		MetadataCollectorException last = null;
		for (CertificateValidator validator : validators) {
			try {
				validator.validate(certificate);
				return;
			} catch (MetadataCollectorException ex) {
				last = ex;
			}
		}
		
		if (last == null)
			last = new MetadataCollectorException("Unknown validatoin error");
		
		throw last;
	}

}
