package se.swami.saml.metadata.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.store.MetadataStore;

@Controller
@RequestMapping("/entity/*")
public class EntityController {

	@Autowired
	@Qualifier("metadataStore")
	private MetadataStore metadataStore;
	
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public String listMetadata(Model model) throws MetadataIOException {
		model.addAttribute("entities",metadataStore.fetchAll());
		return "entityList";
	}
	
	@RequestMapping(value="/id/{id}",method=RequestMethod.GET)
	public String showEntity(@PathVariable("id") String id, Model model) throws MetadataIOException {
		model.addAttribute("entity",metadataStore.fetchByID(id));
		return "entityDetail";
	}
	
}
