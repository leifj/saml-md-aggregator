package net.nordu.mdx.scanner;

import java.util.TimerTask;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.store.MetadataStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataScanner extends TimerTask {

	private static final Log log = LogFactory.getLog(MetadataScanner.class);
	
	@Autowired
	private MetadataStore store;
	@Autowired
	private MetadataIndex index;
	@Autowired
	private MetadataChangeNotifier changeNotifier;
	
	public void run() {
		try {
			for (String id: store.listIDs()) {
				if (!index.exists(id))
					changeNotifier.notifyChange(new MetadataChange(id, MetadataChangeType.ADD));
				else if (store.lastModified(id).after(index.lastModified(id)))
					changeNotifier.notifyChange(new MetadataChange(id, MetadataChangeType.MODIFY));
			}
			for (String id: index.listIDs()) {
				if (!store.exists(id))
					changeNotifier.notifyChange(new MetadataChange(id, MetadataChangeType.REMOVE));
			}
		} catch (Exception ex) {
			log.warn(ex.getMessage());
		}
	}
	
}
