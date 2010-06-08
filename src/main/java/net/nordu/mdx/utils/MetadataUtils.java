package net.nordu.mdx.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MetadataUtils {
	
	public interface AttributeCallback {
		public void attribute(String nameFormat, String name, String value);
	}
	
	private static final String SAMLMD = "urn:oasis:names:tc:SAML:2.0:metadata";
	private static final String SAML = "urn:oasis:names:tc:SAML:2.0:assertion";

	public static String getEntityID(Document doc) throws IllegalArgumentException {
		
		Element entity = doc.getDocumentElement();
		
		String entityID = entity.getAttributeNS(SAMLMD, "entityID");
		if (entityID == null)
			throw new IllegalArgumentException("Missing entityID from document metadata");
		return entityID;
	}
	
	public static Element getFirstElementByTagNameNS(Element root, String ns, String element) {
		NodeList nl = root.getElementsByTagNameNS(ns, element);
		if (nl == null || nl.getLength() == 0)
			return null;
		return (Element)nl.item(0);
	}
	
	public static Element getAttributes(Document doc) {
		Element entity = doc.getDocumentElement();
		Element exts = getFirstElementByTagNameNS(entity, SAMLMD, "Extensions");
		if (exts == null)
			return null;
		
		return getFirstElementByTagNameNS(exts, SAML, "Attribute");
	}
	
	public static void withAttributes(Document doc, AttributeCallback cb) {
		Element attrsElt = MetadataUtils.getAttributes(doc);
		if (attrsElt == null)
			return;
		
		NodeList avl = attrsElt.getElementsByTagNameNS(SAML, "Attribute");
		for (int i = 0; i < avl.getLength(); i++) {
			Element av = (Element)avl.item(i);
			String nameFormat = av.getAttributeNS(SAML, "NameFormat");
			String name = av.getAttributeNS(SAML, "Name");
			NodeList vl = attrsElt.getElementsByTagNameNS(SAML, "Value");
			for (int k = 0; k < vl.getLength(); k++) {
				Element v = (Element)vl.item(k);
				String value = v.getTextContent();
				cb.attribute(nameFormat, name, value);
			}
		}
	}
}
