package net.nordu.mdx.store;

import java.io.File;
import java.net.URL;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.scanner.MetadataChangeNotifier;
import net.nordu.mdx.store.fs.FileSystemMetadataStore;
import net.nordu.mdx.store.fs.URLMetadataStoreContext;
import net.nordu.mdx.store.git.GitRepositoryMetadataStore;

import org.springframework.beans.factory.annotation.Autowired;

public class MetadataStoreFactory {

	@Autowired
	private MetadataIndex index;
	@Autowired
	private MetadataChangeNotifier changeNotifier;
		
	public MetadataStore createInstance(String urlStr)
		throws Exception
	{
		if (urlStr.contains(".git") && urlStr.startsWith(File.separator))
			urlStr = "git://"+urlStr;
		
		if (urlStr.startsWith(File.separator))
			urlStr = "file://"+urlStr;
		
		URL url = new URL(urlStr);
		System.err.println(url);
		URLMetadataStoreContext ctx = new URLMetadataStoreContext();
		ctx.setIndex(index);
		ctx.setChangeNotifier(changeNotifier);
		ctx.setUrl(url);
		MetadataStore store = null;
		if (url.getProtocol().equals("git"))
			store = new GitRepositoryMetadataStore();
			
		if (url.getProtocol().equals("file"))
			store = new FileSystemMetadataStore();
		
		if (store == null)
			throw new IllegalArgumentException("Unknown store URL: "+url);
		
		store.setContext(ctx);
		return store;
	}
}
