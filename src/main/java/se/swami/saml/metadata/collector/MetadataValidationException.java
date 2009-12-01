/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector;

public class MetadataValidationException extends MetadataCollectorException {

	public MetadataValidationException(Exception inner) {
		super(inner);
	}
	
	public MetadataValidationException() {
	}
	
	public MetadataValidationException(String message) {
		super(message);
	}
	
}
