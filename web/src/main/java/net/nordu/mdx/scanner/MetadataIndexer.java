package net.nordu.mdx.scanner;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.store.MetadataStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataIndexer {

	private static final Log log = LogFactory.getLog(MetadataIndexer.class);
	
	@Autowired
	private MetadataStore store;
	@Autowired
	private MetadataIndex index;
	
	public void index(MetadataChange change) throws Exception {
		log.info(change);
		if (change.getType() == MetadataChangeType.ADD || change.getType() == MetadataChangeType.MODIFY) {
			index.update(change.getId(),store.load(change.getId()));
		} else if (change.getType() == MetadataChangeType.REMOVE)
			index.remove(change.getId());
	}
	
}
