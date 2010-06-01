package se.swami.saml.metadata.web.controllers;
import java.util.Collection;
import java.util.Date;

import org.oasis.saml.metadata.EntitiesDescriptorType;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataCollectorException;
import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.collector.MetadataReference;
import se.swami.saml.metadata.collector.MetadataReferenceFactory;
import se.swami.saml.metadata.collector.impl.BasicMetadataCollector;
import se.swami.saml.metadata.policy.MetadataPolicyException;
import se.swami.saml.metadata.services.PolicyManager;
import se.swami.saml.metadata.store.MetadataStore;
import se.swami.saml.metadata.utils.MetadataUtils;

@Controller
@RequestMapping("/entity/*")
public class EntityController {

	@Autowired
	@Qualifier("metadataStore")
	private MetadataStore metadataStore;
	
	@Autowired
	private PolicyManager policyManager;
	
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listMetadata(Model model) throws MetadataIOException {
		EntitiesDescriptorType entities = MetadataUtils.aggregate(metadataStore.fetchAll(),"all",null,null);
		model.addAttribute("entities",entities);
		model.addAttribute("metadataUtils",new MetadataUtils());
		model.addAttribute("now",new Date());
		return "entityList";
	}
	
	@RequestMapping(value="/id/{id}",method=RequestMethod.GET)
	public String showEntityByID(@PathVariable("id") String id, Model model) throws MetadataIOException {
		model.addAttribute("entity",metadataStore.fetchByID(id));
		model.addAttribute("metadataUtils",new MetadataUtils());
		model.addAttribute("now",new Date());
		return "entityDetail";
	}
	
	@RequestMapping(value="/entityid/{eid}",method=RequestMethod.GET)
	public String showEntityByEntityID(@PathVariable("eid") String eid, Model model) throws MetadataIOException, MetadataPolicyException {
		Collection<EntityDescriptorType> entities = metadataStore.fetchByEntityID(eid);
		model.addAttribute("metadataUtils",new MetadataUtils());
		model.addAttribute("now",new Date());
		EntityDescriptorType entity = policyManager.select(entities);
		model.addAttribute("entity", entity);
		return "entityDetail";
	}
	
	@RequestMapping(value="/import",method=RequestMethod.POST)
	public void importEntity(@RequestParam("uri") String uri, @RequestParam("overwrite") boolean overwrite, Model model) throws MetadataCollectorException {
		MetadataReference ref = MetadataReferenceFactory.instance(uri);
		MetadataCollector collector = new BasicMetadataCollector();
		Collection<EntityDescriptorType> entities = collector.fetch(ref);
		
		for (EntityDescriptorType entity : entities) {
			metadataStore.store(entity);
		}
	}
}
