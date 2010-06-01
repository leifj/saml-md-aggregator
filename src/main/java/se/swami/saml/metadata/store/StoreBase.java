package se.swami.saml.metadata.store;

import se.swami.saml.metadata.collector.MetadataIOException;

public abstract class StoreBase implements MetadataStore {

	private boolean readOnly = false;
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public synchronized void removeAll() throws MetadataIOException {
		for (String id : listAll()) {
			remove(id);
		}
	}
	
}
