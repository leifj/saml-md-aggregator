package se.swami.saml.metadata.store;

import java.io.File;
import java.io.IOException;

public interface VCS {

	public void commit(File file, String comment, boolean isNew) throws IOException;
	public void remove(File file, String comment);
	
}
