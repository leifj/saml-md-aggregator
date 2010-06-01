package se.swami.saml.metadata.services;

import java.util.Collection;
import java.util.List;

import org.oasis.saml.metadata.EntityDescriptorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.swami.saml.metadata.policy.MetadataPolicy;
import se.swami.saml.metadata.policy.MetadataPolicyException;

@Service
public class PolicyManager {

	@Autowired
	private List<MetadataPolicy> policies;
	
	public EntityDescriptorType select(Collection<EntityDescriptorType> entities) throws MetadataPolicyException {
		for (MetadataPolicy policy : policies) {
			entities = policy.refine(entities);
			if (entities.size() == 1)
				return entities.iterator().next();
		}
		
		if (entities.size() == 0)
			return null;
		
		if (entities.size() == 1)
			return entities.iterator().next();
		
		throw new MetadataPolicyException("Policy resulted in > 1 entity");
	}
	
}
