package net.nordu.mdx.store;

import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

import org.oasis.saml.metadata.EntityDescriptorType;

public interface MetadataStore {

	public List<String> listIDs() throws Exception;
	public boolean exists(String id) throws Exception;
	public EntityDescriptorType load(String id) throws Exception;
	public Calendar lastModified(String id);
	public TimerTask scanner();
	public void setContext(MetadataStoreContext params) throws Exception;
	
}
