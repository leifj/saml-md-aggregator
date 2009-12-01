package se.swami.saml.metadata.utils;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.neo4j.api.core.Node;
import org.oasis.saml.assertion.AssertionType;
import org.oasis.saml.assertion.AttributeStatementType;
import org.oasis.saml.assertion.AttributeType;
import org.oasis.saml.metadata.AdditionalMetadataLocationType;
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
	
}
