package net.nordu.mdx.store;

import java.io.Serializable;

import net.nordu.mdx.index.MetadataIndex;
import net.nordu.mdx.scanner.MetadataChangeNotifier;

public interface MetadataStoreContext extends Serializable {

	public MetadataIndex getIndex();
	public MetadataChangeNotifier getChangeNotifier();
	
}
