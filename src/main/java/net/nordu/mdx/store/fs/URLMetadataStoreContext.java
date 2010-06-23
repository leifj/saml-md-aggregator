package net.nordu.mdx.store.fs;

import java.net.URL;

import net.nordu.mdx.store.MetadataStoreContextBase;

public class URLMetadataStoreContext extends MetadataStoreContextBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5358038794721104393L;

	private URL url;
	
	public URL getUrl() {
		return url;
	}
	
	public void setUrl(URL url) {
		this.url = url;
	}
	
}
