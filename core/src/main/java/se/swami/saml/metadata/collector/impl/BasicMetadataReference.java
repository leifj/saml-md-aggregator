/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector.impl;

import java.net.URI;

import se.swami.saml.metadata.collector.CertificateValidator;
import se.swami.saml.metadata.collector.MetadataReference;

public class BasicMetadataReference implements MetadataReference {

	private URI location;
	private CertificateValidator validator;
	
	public URI getLocation() {
		return location;
	}
	public void setLocation(URI location) {
		this.location = location;
	}
	public CertificateValidator getValidator() {
		return validator;
	}
	public void setValidator(CertificateValidator validator) {
		this.validator = validator;
	}

}
