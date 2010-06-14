package net.nordu.mdx.store.fs;

import java.io.File;
import java.io.FilenameFilter;

public class ExtFilter implements FilenameFilter {

	private String ext;
	
	public ExtFilter(String ext) {
		this.ext = ext;
	}
	
	@Override
	public boolean accept(File dir, String fn) {
		File f = new File(dir,fn);
		return f.isDirectory() || fn.endsWith(ext);
	}

}
