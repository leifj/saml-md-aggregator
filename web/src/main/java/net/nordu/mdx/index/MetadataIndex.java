package net.nordu.mdx.index;

import java.util.Calendar;

import org.oasis.saml.metadata.EntityDescriptorType;

public interface MetadataIndex {

	public Iterable<String> find(String[] tags) throws Exception;
	public void update(String id, EntityDescriptorType entity) throws Exception;
	public void remove(String id);
	public boolean exists(String id);
	public Iterable<String> listIDs();
	public Calendar lastModified(String id);
	
}
