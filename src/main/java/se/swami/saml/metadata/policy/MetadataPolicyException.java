/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.policy;

public class MetadataPolicyException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MetadataPolicyException() {
	}
	
	public MetadataPolicyException(String message) {
		super(message);
	}
	
	public MetadataPolicyException(Exception inner) {
		super(inner);
	}
}
