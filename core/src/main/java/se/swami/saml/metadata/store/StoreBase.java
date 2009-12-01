package se.swami.saml.metadata.store;

public abstract class StoreBase {

	private boolean readOnly;
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
}
