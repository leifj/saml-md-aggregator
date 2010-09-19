package net.nordu.mdx.scanner;

import org.springframework.integration.annotation.Gateway;

public interface MetadataChangeNotifier {

	@Gateway(requestChannel="changes")
	public void notifyChange(MetadataChange change);
	
}
