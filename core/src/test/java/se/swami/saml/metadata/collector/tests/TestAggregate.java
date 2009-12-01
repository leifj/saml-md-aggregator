/*
 * Created on Apr 26, 2008
 *
 */
package se.swami.saml.metadata.collector.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.swami.saml.metadata.collector.MetadataAggregate;
import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.collector.impl.BasicMetadataCollector;
import se.swami.saml.metadata.store.MetadataStore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class TestAggregate {

	@Autowired
	@Qualifier("neoStore")
	private MetadataStore metadataStore;
	private X509Certificate signer;
	
	private static final String MD_URI = "swamid-1.0.xml";
	
	@Before
	public void setUp() throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		signer = (X509Certificate)cf.generateCertificate(Thread.currentThread().getContextClassLoader().getResourceAsStream("md-signer.crt"));
	}
	
	@Test
	public void testStore() {
		System.err.println(metadataStore);
		assertNotNull(metadataStore);
	}
	
	@Test
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
