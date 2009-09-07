/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.oasis.saml.metadata.EntityDescriptorType;

import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.collector.MetadataReference;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.collector.impl.BasicMetadataCollector;

public class FetchSWAMIDTest {
	
	private MetadataReference collection;
	
	@Before
	public void initialize() throws Exception {
		try {
			collection = MetadataReferenceFactory.instance("http://wayf.swamid.se/md/urn-mace-swami.se-swamid-test-1.0-metadata.xml");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	@Test
	public void testFetchMD() {
		try {
			MetadataCollector collector = new BasicMetadataCollector();
			Collection<EntityDescriptorType> entities = collector.fetch(collection);
			assert(entities.size() > 0);
			EntityDescriptorType suidp = (EntityDescriptorType)CollectionUtils.find(entities, new Predicate() {
				public boolean evaluate(Object object) {
					EntityDescriptorType e = (EntityDescriptorType)object;
					return e.getEntityID().equalsIgnoreCase("https://idp.secure.su.se/identity");
				}
			});
			assertNotNull(suidp);
			
		} catch (MetadataIOException ex) {	
			System.err.println(ex.getMessage());
			System.err.println("Network probably offline - ignoring test...");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}
	
}
