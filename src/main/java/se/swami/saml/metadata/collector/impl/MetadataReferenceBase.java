package se.swami.saml.metadata.collector.impl;

import java.util.Calendar;

import se.swami.saml.metadata.collector.MetadataReference;

public abstract class MetadataReferenceBase implements MetadataReference {

	private Calendar lastUpdate;
	private boolean stale;
	
	public boolean isStale() {
		return stale;
	}
	
	public void setStale(boolean stale) {
		this.stale = stale;
	}

	public void setLastUpdate(Calendar lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public Calendar getLastUpdate() {
		return lastUpdate;
	}

}
