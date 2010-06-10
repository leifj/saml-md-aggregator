package net.nordu.mdx;

public class MetadataIOException extends RuntimeException {

	private static final long serialVersionUID = -4323006006007227142L;
	
	public MetadataIOException() { super(); }
	public MetadataIOException(String msg) { super(msg); }
	public MetadataIOException(Exception inner) { super(inner); }
	
}
