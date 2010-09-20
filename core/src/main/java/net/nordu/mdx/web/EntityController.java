package net.nordu.mdx.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.store.MetadataStore;
import net.nordu.mdx.utils.MetadataUtils;

import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/md/*")
public class EntityController {

	@Autowired
	private MetadataStore store;
	@Autowired
	private MetadataIndex index;
	
	@RequestMapping(value="/{tags}", method=RequestMethod.GET)
	public String list(@PathVariable("tags") String plustags, Model model) throws Exception {
		String[] tags = plustags.split("\\+");
		List<EntityDescriptorType> docs = new ArrayList<EntityDescriptorType>();
		for (String id: index.find(tags)) {
			docs.add(store.load(id));
		}
		
		model.addAttribute("tags",plustags);
		model.addAttribute("metadataUtils",new MetadataUtils());
		model.addAttribute("now",new Date());
		if (docs.size() == 1) {
			model.addAttribute("entity", docs.get(0));
			return "entityDetail";
		} else {
			model.addAttribute("entities",docs);
			return "entityList";
		}
	}
	
}
