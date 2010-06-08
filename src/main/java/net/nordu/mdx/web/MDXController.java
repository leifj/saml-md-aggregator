package net.nordu.mdx.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.signer.MetadataSigner;
import net.nordu.mdx.signer.MetadataSignerSelector;
import net.nordu.mdx.store.MetadataStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Controller
@RequestMapping("/entity/*")
public class MDXController {
	
	@Autowired
	private MetadataIndex index;
	
	@Autowired
	private MetadataStore store;
	
	@Autowired
	private MetadataSigner signer;
	
	@Autowired
	private MetadataSignerSelector signerSelector;
	
	@RequestMapping(value="/{tags}",method=RequestMethod.GET)
	public String mdx(@PathVariable("tags") String plustags, Model model) throws Exception {
		String[] tags = plustags.split("+");
		List<Document> docs = new ArrayList<Document>();
		for (String id: index.find(tags)) {
			docs.add(store.load(id));
		}
		model.addAttribute("entities", docs);
		String signerName = signerSelector.findSignerName(tags);
		if (docs.size() == 1) {
			model.addAttribute("entity", signer.sign(docs.get(0),signerName));
		} else {
			model.addAttribute("collection",signer.sign(makeCollection(docs, tags[0]),signerName));
		}
		return "mdx";
	}

	private Document makeCollection(List<Document> docs,String collectionName) throws ServletException {
		try {
			DocumentBuilder db = makeDocumentBuilder();
			Document collectionDoc = db.newDocument();
			Element entitiesDescriptorElt = collectionDoc.createElementNS("urn:oasis:names:tc:SAML:2.0:metadata","EntitiesDescriptor");
			collectionDoc.appendChild(entitiesDescriptorElt);
			entitiesDescriptorElt.setAttributeNS("urn:oasis:names:tc:SAML:2.0:metadata", "Name", collectionName);
			
			return collectionDoc;
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}
	
	private DocumentBuilder makeDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setXIncludeAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db;
	}
}
