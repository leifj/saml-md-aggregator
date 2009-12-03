package se.swami.saml.metadata.utils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationBuilder;
import org.apache.xmlbeans.XmlBase64Binary;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.neo4j.api.core.Node;
import org.oasis.saml.assertion.AssertionType;
import org.oasis.saml.assertion.AttributeStatementType;
import org.oasis.saml.assertion.AttributeType;
import org.oasis.saml.metadata.AdditionalMetadataLocationType;
import org.oasis.saml.metadata.EntitiesDescriptorDocument;
import org.oasis.saml.metadata.EntitiesDescriptorType;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.oasis.saml.metadata.attribute.EntityAttributesDocument;
import org.oasis.saml.metadata.attribute.EntityAttributesType;
import org.w3c.dom.NodeList;

import se.swami.saml.metadata.collector.MetadataIOException;

public class MetadataUtils {

	public static EntityAttributesType findAttributes(EntityDescriptorType entity) throws MetadataIOException {
		try {
			org.w3c.dom.Node domNode = entity.getExtensions().getDomNode();
			NodeList children = domNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				org.w3c.dom.Node cn = children.item(i);
				if (cn.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && cn.getLocalName().equals("EntityAttributes")) {
					EntityAttributesDocument entityAttributesDoc = EntityAttributesDocument.Factory.parse(cn);
					return entityAttributesDoc.getEntityAttributes();
				}
			}
			return null;
		} catch (XmlException ex) {
			throw new MetadataIOException(ex);
		}
	}
	
	public static XmlObject[] getAttributeAsXML(EntityDescriptorType entity, String attributeName) throws MetadataIOException {
		EntityAttributesType ea = MetadataUtils.findAttributes(entity);
		if (ea != null && ea.getAssertionArray() != null && ea.getAssertionArray().length > 0) {
			AssertionType assertion = ea.getAssertionArray(0);
			for (AttributeStatementType as : assertion.getAttributeStatementArray()) {
				for (AttributeType attribute : as.getAttributeArray()) {
					if (attribute.getName().equals(attributeName)) {
						return attribute.getAttributeValueArray();
					}
				}
			}
		}
		return null;
	}
	
	public static String[] getAttribute(EntityDescriptorType entity, String attributeName) throws MetadataIOException {
		XmlObject[] o = getAttributeAsXML(entity, attributeName);
		if (o == null)
			return null;
		
		String[] v = new String[o.length];
		for (int i = 0; i < o.length; i++) {
			v[i] = o[i].toString();
		}
		
		return v;
	}
	
	public static boolean hasAttribute(EntityDescriptorType entity, String attributeName, String attributeValue) throws MetadataIOException {
		XmlObject[] vo = getAttributeAsXML(entity, attributeName);
		if (vo == null)
			return false;
			
		for (XmlObject o : vo) {
			if (o.toString().equals(attributeValue))
				return true;
		}
		
		return false;
	}
	
	public static void setAttribute(Node entityNode, AttributeType attr) {
		String n = attr.getName();
		XmlObject[] va = attr.getAttributeValueArray();
		String[] values = new String[va.length];
		int i = 0;
		for (XmlObject v : va) {
			values[i++] =  v.getDomNode().getTextContent();
		}
		entityNode.setProperty(n, values);
	}
	
	public static String[] getOrigin(EntityDescriptorType entity) throws MetadataIOException {
		AdditionalMetadataLocationType[] locs = entity.getAdditionalMetadataLocationArray();
		String[] origin = new String[locs.length];
		int i = 0;
		
		for (AdditionalMetadataLocationType loc : locs) {
			origin[i++] = loc.getStringValue();
		}
		
		return origin;
	}
	
	public static EntitiesDescriptorType aggregate(Collection<EntityDescriptorType> entities, String name, Calendar validUntil, GDuration duration) {
		EntitiesDescriptorDocument doc = EntitiesDescriptorDocument.Factory.newInstance();
		EntitiesDescriptorType entitiesDescriptor = doc.addNewEntitiesDescriptor();
		
		XmlCursor cursor = doc.getEntitiesDescriptor().newCursor();
		cursor.toNextToken();
		for (EntityDescriptorType entity : entities) {
			entity.newCursor().copyXml(cursor);
			cursor.toNextSibling();
		}

		entitiesDescriptor.setName(name);
		if (validUntil != null)
			entitiesDescriptor.setValidUntil(validUntil);
		
		if (duration != null)
			entitiesDescriptor.setCacheDuration(duration);
		
		return doc.getEntitiesDescriptor();
	}
	
	public static String validUntil(EntityDescriptorType entity) {
		Calendar validUntil = entity.getValidUntil();
		return validUntil == null ? "" : validUntil.toString();
	}

	private static final String NSDECL = "declare namespace ds='http://www.w3.org/2000/09/xmldsig#';";
	
	public static X509Certificate[] getCertificates(EntityDescriptorType entity) throws Base64DecodingException, CertificateException {
		XmlObject b64certs[] = (XmlObject[])entity.selectPath(NSDECL+"$this//ds:X509Certificate");
		X509Certificate certs[] = new X509Certificate[b64certs.length];
		int i = 0;
		for (XmlObject b64cert : b64certs) {
			XmlCursor c = b64cert.newCursor();
			byte[] certData = Base64.decode(c.getTextValue());
			CertificateFactory cf = CertificateFactory.getInstance("X509");
			X509Certificate certificate = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(certData));
			certs[i++] = certificate;
		}
		return certs;
	}
	
	public static Date firstCertExpiration(EntityDescriptorType entity) throws Base64DecodingException, CertificateException {
		X509Certificate certs[] = getCertificates(entity);
		Date date = null;
		for (X509Certificate cert : certs) {
			Date notAfter = cert.getNotAfter();
			if (date == null || notAfter.before(date)) {	
				date = notAfter;
			}
		}
		return date;
	}
	
}
