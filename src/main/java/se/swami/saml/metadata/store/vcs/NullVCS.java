package se.swami.saml.metadata.store.vcs;

import java.io.File;
import java.io.IOException;

import se.swami.saml.metadata.store.VCS;

public class NullVCS implements VCS {

	public void commit(File file, String comment, boolean isNew) throws IOException {
		System.err.println(comment);
	}

	public void remove(File file, String comment) {
		System.err.println(comment);
	}

}
