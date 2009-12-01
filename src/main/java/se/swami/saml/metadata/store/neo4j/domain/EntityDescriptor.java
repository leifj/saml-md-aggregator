package se.swami.saml.metadata.store.neo4j.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.util.NodeWrapperRelationshipSet;
import org.oasis.saml.metadata.EntityDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorType;

import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.store.neo4j.EntityStoreRelationshipTypes;
import se.swami.saml.metadata.store.neo4j.NodeBean;
import se.swami.saml.metadata.utils.XMLUtils;

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
			setXml(XMLUtils.o2b(entity));
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
}
