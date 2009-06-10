package se.swami.saml.metadata.store.neo4j.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.util.NodeWrapperRelationshipSet;
import org.oasis.saml.assertion.AttributeType;
import org.oasis.saml.metadata.AdditionalMetadataLocationType;
import org.oasis.saml.metadata.EntityDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.oasis.saml.metadata.attribute.EntityAttributesDocument;
import org.oasis.saml.metadata.attribute.EntityAttributesType;
import org.w3c.dom.NodeList;

import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.store.neo4j.EntityStoreRelationshipTypes;
import se.swami.saml.metadata.store.neo4j.NodeBean;

public class EntityDescriptor extends NodeBean {
	
	public EntityDescriptor(NeoService neo, Node node) {
		super(neo, node);
		
		this.locations = new NodeWrapperRelationshipSet<Location>(neo,node,EntityStoreRelationshipTypes.HAS_ORIGIN,Location.class);
	}

	public byte[] getXml() {
		return (byte[])getProperty("xml");
	}
	
	public void setXml(byte[] xml) {
		setProperty("xml", xml);
	}
	
	public String getEntityID() {
		return (String)getProperty("entityID");
	}
	
	public void setEntityID(String entityID) {
		setProperty("entityID", entityID);
	}
	
	private Set<Location> locations;
	
	public Set<Location> getLocations() {
		return locations;
	}
	
	public void setEntityDesciptorType(EntityDescriptorType entity) throws MetadataIOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			XmlOptions opts = new XmlOptions();
			opts.setSaveOuter();
			opts.setSavePrettyPrint();
			opts.setSaveAggressiveNamespaces();
			entity.save(out,opts);
			
			setXml(out.toByteArray());
		} catch (Exception ex) {
			throw new MetadataIOException(ex);
		}
	}
	
	public EntityDescriptorType getEntityDescriptorType() throws MetadataIOException {
		try {
			byte[] data = (byte[])getXml();
			if (data == null)
				throw new MetadataIOException("Unpopulated node");
			
			ByteArrayInputStream in = new ByteArrayInputStream(data);
	
			EntityDescriptorDocument mDoc = EntityDescriptorDocument.Factory.parse(in);
			
			return mDoc.getEntityDescriptor();
		} catch (XmlException ex) {
			throw new MetadataIOException(ex);
		} catch (IOException ex) {
			throw new MetadataIOException(ex);
		}
	}
	
	private void setAttribute(Node entityNode, AttributeType attr) {
		String n = attr.getName();
		XmlObject[] va = attr.getAttributeValueArray();
		String[] values = new String[va.length];
		int i = 0;
		for (XmlObject v : va) {
			values[i++] =  v.getDomNode().getTextContent();
		}
		entityNode.setProperty(n, values);
	}
	
	private EntityAttributesType findAttributes(EntityDescriptorType entity) throws MetadataIOException {
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
	
	
	public String[] getOrigin() throws MetadataIOException {
		EntityDescriptorType entity = getEntityDescriptorType();
		//EntityAttributesType entityAttributes = findAttributes(entity);
		
		AdditionalMetadataLocationType[] locs = entity.getAdditionalMetadataLocationArray();
		String[] origin = new String[locs.length];
		int i = 0;
		
		for (AdditionalMetadataLocationType loc : locs) {
			origin[i++] = loc.getStringValue();
		}
		
		return origin;
	}
	
}
