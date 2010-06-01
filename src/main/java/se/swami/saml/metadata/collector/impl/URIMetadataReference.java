/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import se.swami.saml.metadata.collector.CertificateValidator;
import se.swami.saml.metadata.collector.MetadataIOException;

public class URIMetadataReference extends MetadataReferenceBase {

	private String location;
	private CertificateValidator validator;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public CertificateValidator getValidator() {
		return validator;
	}
	public void setValidator(CertificateValidator validator) {
		this.validator = validator;
	}

	public InputStream getMetadata() throws MetadataIOException {
        System.err.println(location);
        try {
        	InputStream in = null;
	        if (location.startsWith("http://") || location.startsWith("https://")) {
		        HttpClient client = new HttpClient();
		        GetMethod get = new GetMethod(location);
		        int code = client.executeMethod(get);
		        if (code == 200) {
		        	in = get.getResponseBodyAsStream();
		        }
	        } else {
	        	in = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
	        }

	        return in;
        } catch (IOException ex) {
        	throw new MetadataIOException(ex);
        }
    }
	public boolean isRemote() {
		return location.startsWith("http://") || location.startsWith("https://");
	}
	
}
