/*
 * Created on Apr 19, 2008
 *
 */
package se.swami.saml.metadata.collector;

import java.util.Collection;

import org.oasis.saml.metadata.EntityDescriptorType;

public interface MetadataCollector {

	public abstract Collection<EntityDescriptorType> fetch(MetadataReference collection) throws MetadataCollectorException;
	
}
