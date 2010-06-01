package se.swami.saml.metadata.fs.tests;


import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.collector.impl.BasicMetadataCollector;
import se.swami.saml.metadata.store.MetadataStore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:fs-tests.xml"})
public class FileStoreTest {

	@Autowired
	@Qualifier("metadataStore")
	private MetadataStore metadataStore;
	private X509Certificate signer;
	
	private static final String MD_URI = "swamid-1.0.xml";
	
	@Before
	public void setUp() throws Exception {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			signer = (X509Certificate)cf.generateCertificate(Thread.currentThread().getContextClassLoader().getResourceAsStream("md-signer.crt"));
		
			MetadataCollector collector = new BasicMetadataCollector();
			Collection<EntityDescriptorType> entities = 
				collector.fetch(MetadataReferenceFactory.instance(MD_URI,signer));
			
			for (EntityDescriptorType entity : entities) {
				metadataStore.store(entity);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFirst() {
		Assert.assertNotNull(metadataStore);
		System.err.println(metadataStore);
	}

}
