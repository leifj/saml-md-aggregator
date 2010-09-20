package net.nordu.mdx.store;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.scanner.MetadataChangeNotifier;

public abstract class MetadataStoreContextBase implements MetadataStoreContext {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8559068110972869231L;

	private MetadataChangeNotifier changeNotifier;
	private MetadataIndex index;
	
	@Override
	public MetadataChangeNotifier getChangeNotifier() {
		return changeNotifier;
	}

	@Override
	public MetadataIndex getIndex() {
		return index;
	}

	public void setIndex(MetadataIndex index) {
		this.index = index;
	}
	
	public void setChangeNotifier(MetadataChangeNotifier changeNotifier) {
		this.changeNotifier = changeNotifier;
	}
	
}
