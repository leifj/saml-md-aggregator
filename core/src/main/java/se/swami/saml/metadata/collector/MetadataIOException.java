/*
 * Created on Apr 24, 2009
 *
 */
package se.swami.saml.metadata.collector;


public class MetadataIOException extends MetadataCollectorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MetadataIOException(Exception inner) {
		super(inner);
	}
	
	public MetadataIOException() {
	}
	
	public MetadataIOException(String message) {
		super(message);
	}
	
}
