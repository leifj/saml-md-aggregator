package net.nordu.mdx.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.signer.MetadataSigner;
import net.nordu.mdx.signer.MetadataSignerSelector;
import net.nordu.mdx.signer.SignerInfo;
import net.nordu.mdx.store.MetadataStore;
import net.nordu.mdx.utils.MetadataUtils;

import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationBuilder;
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
		SignerInfo signerInfo = signerSelector.findSignerInfo(tags);
		if (docs.size() == 0)
			throw new MetadataNotFoundException();
		
		Node toBeSigned = null;
		Calendar validUntil = signerInfo.getValidUntil();
		GDuration duration = signerInfo.getCacheDuration() == null ? null : new GDuration(signerInfo.getCacheDuration());
		
		for (EntityDescriptorType entity: docs) {
			if (entity.isSetID())
				entity.unsetID();
			if (entity.isSetCacheDuration())
				entity.unsetCacheDuration();
			if (entity.isSetValidUntil())
				entity.unsetValidUntil();
			if (entity.isSetSignature())
				entity.unsetSignature();
		}
		
		if (docs.size() == 1) {
			EntityDescriptorType entity = docs.get(0);
			if (validUntil != null)
				entity.setValidUntil(validUntil);
			if (duration != null)
				entity.setCacheDuration(duration);
			toBeSigned = entity.getDomNode();
		} else {
			EntitiesDescriptorType collection = MetadataUtils.aggregate(docs, tags[0], validUntil, duration);
			toBeSigned = collection.getDomNode();
		}
		signer.sign(toBeSigned, signerInfo.getAlias());
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		response.setContentType("application/samlmetadata+xml");
		t.transform(new DOMSource(toBeSigned),new StreamResult(response.getOutputStream()));
	}
}
