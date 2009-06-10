package se.swami.saml.metadata.rest;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.oasis.saml.metadata.AdditionalMetadataLocationType;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataCollectorException;
import se.swami.saml.metadata.collector.MetadataReference;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.store.MetadataStore;

@Path("/import")
@Component
public class EntityImportResource {

	@Autowired
	private MetadataStore metadataStore;
	@Autowired
	private MetadataCollector collector;
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public void importEntity(@FormParam("location") String location) throws MetadataCollectorException {
		MetadataReference ref = MetadataReferenceFactory.instance(location);
		Collection<EntityDescriptorType> entities = collector.fetch(ref);
		for (EntityDescriptorType entity : entities) {
			AdditionalMetadataLocationType loc = entity.addNewAdditionalMetadataLocation();
			loc.setStringValue(location);
			metadataStore.store(entity);
		}
	}
	
}
