package se.swami.saml.metadata.utils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.apache.xmlbeans.GDuration;
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
import org.oasis.saml.metadata.RoleDescriptorType;
import org.oasis.saml.metadata.attribute.EntityAttributesDocument;
import org.oasis.saml.metadata.attribute.EntityAttributesType;
import org.w3c.dom.NodeList;

import se.swami.saml.metadata.collector.MetadataIOException;

public class MetadataUtils {

	private static final String NSDECL = "declare namespace ds='http://www.w3.org/2000/09/xmldsig#';"+
		"declare namespace md='urn:oasis:names:tc:SAML:2.0:metadata';"+
		"declare namespace shibmd='urn:mace:shibboleth:metadata:1.0';";
	
	public static EntityAttributesType findAttributes(EntityDescriptorType entity) throws MetadataIOException {
		try {
			if (entity.getExtensions() == null)
				return null;
			
			org.w3c.dom.Node domNode = entity.getExtensions().getDomNode();
			NodeList children = domNode.getChildNodes();
			if (children == null || children.getLength() == 0)
				return null;
			
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
	
	public static String[] getMetadataLocations(EntityDescriptorType entity) throws MetadataIOException {
		AdditionalMetadataLocationType[] locs = entity.getAdditionalMetadataLocationArray();
		String[] origin = new String[locs.length];
		int i = 0;
		
		for (AdditionalMetadataLocationType loc : locs) {
			origin[i++] = loc.getStringValue();
		}
		
		return origin;
	}
	
	public static String getOrigin(EntityDescriptorType entity) throws MetadataIOException {
		AdditionalMetadataLocationType[] locs = entity.getAdditionalMetadataLocationArray();
		return locs == null || locs.length == 0 ? null : locs[locs.length-1].getStringValue();
	}
	
	public static void addOrigin(EntityDescriptorType entity, String uri) {
		AdditionalMetadataLocationType loc = entity.addNewAdditionalMetadataLocation();
		loc.setStringValue(uri);
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
	
	public static boolean isIdP(EntityDescriptorType entity) {
		RoleDescriptorType[] idp =  entity.getIDPSSODescriptorArray();
		return idp != null && idp.length > 0;
	}
	
	public static boolean isSP(EntityDescriptorType entity) {
		RoleDescriptorType[] sp =  entity.getSPSSODescriptorArray();
		return sp != null && sp.length > 0;
	}
	
	public static String join(Object[] objects, String s) {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		for (Object o : objects) {
			if (i++ > 0)
				buf.append(s);
			buf.append(o);
		}
		return buf.toString();
	}
	
	public static String scope(EntityDescriptorType entity) {
		XmlObject scopes[] = (XmlObject[])entity.selectPath(NSDECL+"$this//shibmd:Scope");
		if (scopes == null || scopes.length == 0)
			return null;
		
		XmlCursor cursor = scopes[0].newCursor();
		return cursor.getTextValue();
	}
	
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
	
	private static final long HOUR = 3600L * 1000L;
	private static final long DAY = 24 * HOUR;
	private static final long YEAR = 365 * DAY;
	
	public static String timeOffset(Long o) {
		if (o == null)
			return "";
		
		long offset = o.longValue();
		String prefix = "";
		String suffix = "";
		if (offset < 0) {
			offset = -offset;
			suffix = "ago";
		} else {
			prefix = "in ";
		}
		long nyears = offset / YEAR;
		offset = (offset % YEAR);
		long ndays = offset / DAY;
		offset = (offset % DAY);
		long nhours = offset / HOUR;
		
		String text = prefix;
		if (nyears > 0) {
			text += nyears + " year";
			if (nyears > 1)
				text += "s";
			text += " ";
		}
		if (ndays > 0) {
			text += ndays + " day";
			if (ndays > 1)
				text += "s";
			text += " ";
		}
		if (nhours > 0) {
			text += nhours + " hour";
			if (nhours > 1)
				text += "s";
			text += " ";
		}
		text += suffix;
		return text;
	}
	
	public static Long timeToCertExpire(EntityDescriptorType entity) throws Base64DecodingException, CertificateException {
		Calendar expires = Calendar.getInstance();
		Date expiryDate = firstCertExpiration(entity);
		if (expiryDate == null)
			return null;
		expires.setTime(expiryDate);
		Calendar now = Calendar.getInstance();
		return new Long(expires.getTimeInMillis() - now.getTimeInMillis());
	}
	
	public static Long timeToInvalid(EntityDescriptorType entity) {
		Calendar invalid = entity.getValidUntil();
		Calendar now = Calendar.getInstance();
		return invalid == null ? null : new Long(invalid.getTimeInMillis() - now.getTimeInMillis());
	}
	
}
