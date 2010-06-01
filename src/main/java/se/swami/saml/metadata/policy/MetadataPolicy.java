package se.swami.saml.metadata.policy;

import java.util.Collection;

import org.oasis.saml.metadata.EntityDescriptorType;

public interface MetadataPolicy {

	/**
	 * 
	 * @param entities
	 * @return the selected EntityDescriptor
	 * @throws MetadataPolicyException
	 * 
	 * This function removes from a collection of @EntityDescriptorType of the same entityID those 
	 * which do not represent given entityID according to policy, possibly merging entity elements in 
	 * the process.
	 */
	public abstract Collection<EntityDescriptorType> refine(Collection<EntityDescriptorType> entities) 
		throws MetadataPolicyException;
	
}
