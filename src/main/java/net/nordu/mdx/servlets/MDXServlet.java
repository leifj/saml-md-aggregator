package net.nordu.mdx.servlets;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.nordu.mdx.MDXServer;

import org.mortbay.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class MDXServlet extends HttpServlet {
	
	private MDXServer server;
	
	
	public MDXServlet(MDXServer server) {
		this.server = server;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		MDXRequest mdxRequest = null;
		try {
			mdxRequest = new MDXRequest(server,req,resp);
		} catch (ServletException ex) {
			Log.info(ex.getMessage());
		}

		List<Document> docs = findDocuments(mdxRequest);
		if (docs.isEmpty() && mdxRequest.isSingle()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Metadata not found: "+mdxRequest.toString());
		} 
		
		String signer = findSigner(mdxRequest);
		if (docs.size() == 1) {
			sendDocument(docs.get(0),signer,resp);
		} else {
			String collectionName = mdxRequest.getLocation();
			sendDocument(makeCollection(docs,collectionName),signer,resp);
		}
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
	
	private void sendDocument(Document doc, String signer, HttpServletResponse resp) 
		throws ServletException 
	{
		try {
			signDocument(signer, doc); 
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			resp.setContentType("application/samlmetadata+xml");
			t.transform(new DOMSource(doc.getDocumentElement()),new StreamResult(resp.getOutputStream()));
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}
	
	private String findSigner(MDXRequest request) {
		return "SWAMID";
	}
	
	private DocumentBuilder makeDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setXIncludeAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db;
	}
	
	private List<Document> findDocuments(MDXRequest request) 
		throws ServletException,IOException
	{
		ArrayList<Document> docs = new ArrayList<Document>();
		
		File entitiesDir = new File(server.getConfig().getProperty("mdx.servlet.dir"));
		File xmlFile = new File(entitiesDir,request.getTags()[0]+".xml");
		System.err.println(xmlFile);
		
		if (!xmlFile.isFile())
			return null;
		
		try {
			DocumentBuilder db = makeDocumentBuilder();
			Document doc = db.parse(xmlFile);
			docs.add(doc);
			//TODO - optionally fix the Name, cache and validity attributes
			//TODO - sign and cache
			return docs;
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
		
	}

	@SuppressWarnings("unchecked")
	private void signDocument(String signer, Document doc)
			throws KeyStoreException, NoSuchAlgorithmException,
			UnrecoverableKeyException, InvalidAlgorithmParameterException,
			KeyException, MarshalException, XMLSignatureException, 
			InstantiationException, IllegalAccessException, ClassNotFoundException 
	{
		char[] pin = server.getConfig().getProperty("mdx.pkcs11.pin").toCharArray();
		PrivateKey signerKey = (PrivateKey)server.getKeyStore().getKey(signer, pin);
		X509Certificate certificate = (X509Certificate)server.getKeyStore().getCertificate(signer);
		//Key key = server.getKeyStore().getKey(signer, pin); 
		Node root = doc.getDocumentElement();
		Node firstChild = root.getFirstChild();
		DOMSignContext dsc = new DOMSignContext(signerKey, root, firstChild);
	
		//TODO: figure out if we need to allow users to specify the provider...
		String providerName = System.getProperty("jsr105Provider","org.jcp.xml.dsig.internal.dom.XMLDSigRI");
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
				((Class<Provider>)Class.forName(providerName)).newInstance()); 
		
		Reference ref = 
			fac.newReference("",
					fac.newDigestMethod(DigestMethod.SHA1, null),
					Collections.singletonList(fac.newTransform(Transform.ENVELOPED,(TransformParameterSpec) null)), null, null); 

		SignedInfo si = 
			fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
					(C14NMethodParameterSpec) null),
					fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
					Collections.singletonList(ref));
		
		KeyInfoFactory kif = fac.getKeyInfoFactory(); 
		X509Data xd = kif.newX509Data(Collections.singletonList(certificate));
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));
		XMLSignature signature = fac.newXMLSignature(si, ki); 
		signature.sign(dsc);
	}
	
}
