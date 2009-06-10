/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector;

public class MetadataCollectorException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MetadataCollectorException() {
	}
	
	public MetadataCollectorException(String message) {
		super(message);
	}
	
	public MetadataCollectorException(Exception inner) {
		super(inner);
	}
}
