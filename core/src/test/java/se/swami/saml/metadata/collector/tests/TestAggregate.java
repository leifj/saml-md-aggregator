/*
 * Created on Apr 26, 2008
 *
 */
package se.swami.saml.metadata.collector.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import junit.framework.TestCase;

import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;

import se.swami.saml.metadata.collector.MetadataAggregate;
import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.collector.impl.BasicMetadataCollector;
import se.swami.saml.metadata.store.MetadataStore;

public class TestAggregate extends TestCase {

	@Autowired
	private MetadataStore metadataStore;
	private X509Certificate signer;

	private static final String MD_URI = "urn-mace-swami.se-swamid-test-1.0-metadata-signed.xml";
	
	@Override
	protected void setUp() throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		signer = (X509Certificate)cf.generateCertificate(Thread.currentThread().getContextClassLoader().getResourceAsStream("md-signer.crt"));
	}
	
	public void testFetch() {
		try {
			MetadataCollector collector = new BasicMetadataCollector();
			Collection<EntityDescriptorType> entities = 
				collector.fetch(MetadataReferenceFactory.instance(MD_URI,signer));
			
			for (EntityDescriptorType entity : entities) {
				metadataStore.store(entity);
			}
			
			MetadataAggregate ma = new MetadataAggregate("test");
			ma.addAllEntities(metadataStore.fetchByEntityID("https://sp1.lab.it.su.se/shibboleth"));
			ma.addAllEntities(metadataStore.fetchByEntityID("https://idp.secure.su.se/identity"));
			File tmpf = File.createTempFile("test", ".xml");
			ma.write(new FileOutputStream(tmpf));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
	}
	
}
