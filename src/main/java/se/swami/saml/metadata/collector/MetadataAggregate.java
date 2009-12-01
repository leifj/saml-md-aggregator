/*
 * Created on Apr 25, 2008
 *
 */
package se.swami.saml.metadata.collector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collection;

import org.apache.xmlbeans.XmlCursor;
import org.oasis.saml.metadata.EntitiesDescriptorDocument;
import org.oasis.saml.metadata.EntitiesDescriptorType;
import org.oasis.saml.metadata.EntityDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorType;

public class MetadataAggregate {

	private EntitiesDescriptorDocument mDoc;
	private EntitiesDescriptorType entitesDescriptor;
	
	public MetadataAggregate(String name, Calendar validUntil) 
	{
		mDoc = EntitiesDescriptorDocument.Factory.newInstance();
		entitesDescriptor = mDoc.addNewEntitiesDescriptor();
		entitesDescriptor.setName(name);
		if (validUntil != null)
			entitesDescriptor.setValidUntil(validUntil);
	}
	
	public MetadataAggregate(String name) 
	{
		this(name,null);
	}
	
	public void addEntity(EntityDescriptorType entity) 
	{
		XmlCursor cursor = mDoc.getEntitiesDescriptor().newCursor();
		cursor.copyXml(entity.newCursor());
	}
	
	public void addAllEntities(Collection<EntityDescriptorType> entities) {
		for (EntityDescriptorType entity : entities) {
			addEntity(entity);
		}
	}
	
	public void addEntity(InputStream in) 
		throws MetadataCollectorException 
	{
		try {
			EntityDescriptorDocument mDoc = EntityDescriptorDocument.Factory.parse(in);
			addEntity(mDoc.getEntityDescriptor());
		} catch (Exception ex) {
			throw new MetadataCollectorException(ex);
		}
	}
	
	public void write(OutputStream out) 
		throws IOException  
	{
		mDoc.save(out);
	}
	
}
