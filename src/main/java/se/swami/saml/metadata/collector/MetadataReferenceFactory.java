/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import se.swami.saml.metadata.collector.impl.FileMetadataReference;
import se.swami.saml.metadata.collector.impl.SelfSignedCertificateValidator;
import se.swami.saml.metadata.collector.impl.URIMetadataReference;

public class MetadataReferenceFactory {

	public static MetadataReference instance(String uri, File certFile) throws MetadataCollectorException {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			InputStream certData = null;
			System.err.println(certFile.getPath());
			if (certFile.isAbsolute())
				certData = new FileInputStream(certFile);
			else
				certData = Thread.currentThread().getContextClassLoader().getResourceAsStream(certFile.getPath());
			
			if (certData == null)
				throw new MetadataIOException("Missing signer: "+certFile);
			
			X509Certificate signer = (X509Certificate)cf.generateCertificate(certData);
			return instance(uri,signer);
		} catch (MetadataCollectorException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new MetadataCollectorException(ex);
		}
	}
	
	public static MetadataReference instance(String uri, X509Certificate signer) throws MetadataCollectorException {
		try {
			URIMetadataReference ref = new URIMetadataReference();
			if (signer != null)
				ref.setValidator(new SelfSignedCertificateValidator(signer));
			ref.setLocation(uri);
			
			return ref;
		} catch (Exception ex) {
			throw new MetadataCollectorException(ex);
		}
	}
	
	public static MetadataReference instance(String uri) throws MetadataCollectorException {
		return instance(uri,(X509Certificate)null);
	}
	
	public static MetadataReference instance(File metadataFile) throws MetadataCollectorException {
		return new FileMetadataReference(metadataFile);
	}
	
}
