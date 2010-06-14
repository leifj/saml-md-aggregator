package net.nordu.mdx;

public class MetadataException extends RuntimeException {

	private static final long serialVersionUID = -4323006006007227142L;
	
	public MetadataException() { super(); }
	public MetadataException(String msg) { super(msg); }
	public MetadataException(Exception inner) { super(inner); }
	
}
