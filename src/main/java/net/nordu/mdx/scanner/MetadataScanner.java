package net.nordu.mdx.scanner;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.store.MetadataStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetadataScanner {

	private static final Log log = LogFactory.getLog(MetadataScanner.class);
	
	@Autowired
	private MetadataStore store;
	@Autowired
	private MetadataIndex index;
	
	public void run() {
		try {
			for (String id: store.listIDs()) {
				index.update(id,store.load(id));
			}
			for (String id: index.listIDs()) {
				if (!store.exists(id)) {
					index.remove(id);
				}
			}
		} catch (Exception ex) {
			log.warn(ex.getMessage());
		}
	}
	
}
