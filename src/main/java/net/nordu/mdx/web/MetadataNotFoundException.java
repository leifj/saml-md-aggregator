package net.nordu.mdx.web;

import net.nordu.mdx.MetadataIOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND,reason="Not Found")
public class MetadataNotFoundException extends MetadataIOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4334592176563414393L;

	public MetadataNotFoundException() { }
	
}
