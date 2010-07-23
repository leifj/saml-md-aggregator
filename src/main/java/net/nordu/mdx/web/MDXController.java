package net.nordu.mdx.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.signer.MetadataSigner;
import net.nordu.mdx.signer.MetadataSignerSelector;
import net.nordu.mdx.signer.SignerInfo;
import net.nordu.mdx.store.MetadataStore;
import net.nordu.mdx.utils.MetadataUtils;
import net.nordu.mdx.utils.StreamUtils;
import net.nordu.mdx.utils.XMLUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.GDuration;
import org.oasis.saml.metadata.EntitiesDescriptorType;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Node;

@Controller
@RequestMapping("/entity/**/*")
public class MDXController {
	
	@Autowired
	private MetadataIndex index;
	
	@Autowired
	private MetadataStore store;
	
	@Autowired
	private MetadataSigner signer;
	
	@Autowired
	private MetadataSignerSelector signerSelector;
	
	@Autowired
	private EhCacheFactoryBean cacheFactory;
	
	@RequestMapping(method=RequestMethod.GET)
	public void mdx(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getPathInfo().replaceFirst("/entity/", "");
		
		Ehcache cache = cacheFactory.getObject();
		Element cacheElement = cache.get(path);
		if (cacheElement == null) {
			String[] tags = path.split("\\+");
			SignerInfo signerInfo = signerSelector.findSignerInfo(tags);
			Calendar validUntil = signerInfo.getValidUntil();
			Calendar now = Calendar.getInstance();
			long timeDiff = validUntil.getTimeInMillis() - now.getTimeInMillis();
			byte[] data = createResponse(request,signerInfo,validUntil,tags);
			cacheElement = new Element(path,data);
			cacheElement.setTimeToLive((int)(timeDiff / 2000)); // cache the signature for half of the validity period
			cache.put(cacheElement);
		}
		
		response.setContentType("application/samlmetadata+xml");
		StreamUtils.copyStream(response.getOutputStream(), new ByteArrayInputStream((byte[])cacheElement.getObjectValue()));
	}

	private byte[] createResponse(HttpServletRequest request, SignerInfo signerInfo, Calendar validUntil, String[] tags) 
		throws Exception
	{
		List<EntityDescriptorType> docs = new ArrayList<EntityDescriptorType>();
		for (String id: index.find(tags)) {
			System.err.println(id);
			docs.add(store.load(id));
		}
		
		if (docs.size() == 0)
			throw new MetadataNotFoundException();
		
		Node toBeSigned = null;
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
			EntitiesDescriptorType collection = MetadataUtils.aggregate(docs, request.getRequestURL().toString(), validUntil, duration);
			toBeSigned = collection.getDomNode();
		}
		signer.sign(toBeSigned, signerInfo.getAlias());
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		t.transform(new DOMSource(toBeSigned),new StreamResult(baos));
		return baos.toByteArray();
	}
}
