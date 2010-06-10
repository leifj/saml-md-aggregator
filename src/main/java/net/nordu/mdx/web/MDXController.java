package net.nordu.mdx.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.signer.MetadataSigner;
import net.nordu.mdx.signer.MetadataSignerSelector;
import net.nordu.mdx.store.MetadataStore;
import net.nordu.mdx.utils.MetadataUtils;

import org.oasis.saml.metadata.EntitiesDescriptorType;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Node;

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
	public void mdx(@PathVariable("tags") String plustags, HttpServletResponse response) throws Exception {
		String[] tags = plustags.split("\\+");
		List<EntityDescriptorType> docs = new ArrayList<EntityDescriptorType>();
		for (String id: index.find(tags)) {
			System.err.println(id);
			docs.add(store.load(id));
		}
		String signerName = signerSelector.findSignerName(tags);
		if (docs.size() == 0)
			throw new MetadataNotFoundException();
		
		Node toBeSigned = null;
		if (docs.size() == 1) {
			toBeSigned = docs.get(0).getDomNode();
		} else {
			EntitiesDescriptorType collection = MetadataUtils.aggregate(docs, tags[0], null, null);
			toBeSigned = collection.getDomNode();
		}
		signer.sign(toBeSigned, signerName);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		response.setContentType("application/samlmetadata+xml");
		t.transform(new DOMSource(toBeSigned),new StreamResult(response.getOutputStream()));
	}
}
