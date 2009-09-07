/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.oasis.saml.metadata.EntityDescriptorType;

import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataCollectorException;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.collector.MetadataValidationException;
import se.swami.saml.metadata.collector.impl.BasicMetadataCollector;

public class ValidateMDTest {

	private X509Certificate signer;
	private X509Certificate noSigner;

	private static final String MD_URI = "urn-mace-swami.se-swamid-test-1.0-metadata-signed.xml";
	
	@Before
	public void initialize() throws Exception {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			signer = (X509Certificate)cf.generateCertificate(Thread.currentThread().getContextClassLoader().getResourceAsStream("md-signer.crt"));
			noSigner =  (X509Certificate)cf.generateCertificate(Thread.currentThread().getContextClassLoader().getResourceAsStream("pcacert.pem"));			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	private void testGoodSignature(String uri, X509Certificate signer) {
		try {
			MetadataCollector collector = new BasicMetadataCollector();
			Collection<EntityDescriptorType> entities = 
				collector.fetch(MetadataReferenceFactory.instance(MD_URI,signer));
			
			assert(entities.size() > 0);
			
			EntityDescriptorType suidp = (EntityDescriptorType)CollectionUtils.find(entities, new Predicate() {
				public boolean evaluate(Object object) {
					EntityDescriptorType e = (EntityDescriptorType)object;
					return e.getEntityID().equalsIgnoreCase("https://idp.secure.su.se/identity");
				}
			});
			assertNotNull(suidp);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}
	
	private void testBadSignature(String uri, X509Certificate signer) {
		try {
			MetadataCollector collector = new BasicMetadataCollector();
			Collection<EntityDescriptorType> entities = 
				collector.fetch(MetadataReferenceFactory.instance(MD_URI,signer));
			// we should throw a validation exception above since the signature is expected to be bad
			fail();
		} catch (MetadataValidationException ex) {
			// expected result !
		} catch (MetadataCollectorException ex) {
			ex.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testValidate() {
		testGoodSignature(MD_URI,signer);
	}
	
	@Test
	public void testNoValidate() {
		testBadSignature(MD_URI,noSigner);
	}
}
