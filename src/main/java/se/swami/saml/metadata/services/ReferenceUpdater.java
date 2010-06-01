package se.swami.saml.metadata.services;

import java.util.Map;

import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataCollectorException;
import se.swami.saml.metadata.collector.MetadataReference;
import se.swami.saml.metadata.store.MetadataStore;

@Service
public class ReferenceUpdater {

	@Autowired
	private Map<String, MetadataReference> references;
	
	@Autowired
	private MetadataCollector collector;
	
	@Autowired
	@Qualifier("metadataStore")
	private MetadataStore metadataStore;
	
	public void update(String id) throws MetadataCollectorException {
		MetadataReference ref = references.get(id);
		
		for (EntityDescriptorType entity : collector.fetch(ref)) {
			metadataStore.store(entity);
		}
	}
	
	public void updateAll() {
		for (String id : references.keySet()) {
			try {
				update(id);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
