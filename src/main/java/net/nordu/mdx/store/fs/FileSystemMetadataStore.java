package net.nordu.mdx.store.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.nordu.mdx.store.MetadataStore;

import org.w3c.dom.Document;

public class FileSystemMetadataStore implements MetadataStore {

	private File dir;
	private String directory;
	
	public String getDirectory() {
		return directory;
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
		this.dir = new File(directory);
	}
	
	private File getDir() {
		return dir;
	}
	
	@Override
	public List<String> listIDs() throws Exception {
		ArrayList<String> ids = new ArrayList<String>();
		File d = getDir();
		if (!d.isDirectory())
			throw new IllegalArgumentException("Not a directory: "+getDirectory());
		
		for (String fn: d.list()) {
			int pos = fn.lastIndexOf(".xml");
			
			if (pos > 0)
				ids.add(fn.substring(0, pos));
		}
		
		return ids;
	}

	@Override
	public Document load(String id) throws Exception {
		File f = new File(getDir(),id+".xml");
		if (f.exists() && f.canRead()) {		
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(f);
			return doc;
		} else {
			return null;
		}
	}

}
