package net.nordu.mdx.store;

import java.util.Calendar;
import java.util.List;

import org.oasis.saml.metadata.EntityDescriptorType;

public interface MetadataStore {

	public List<String> listIDs() throws Exception;
	public boolean exists(String id) throws Exception;
	public EntityDescriptorType load(String id) throws Exception;
	public Calendar lastModified(String id);
	
}
