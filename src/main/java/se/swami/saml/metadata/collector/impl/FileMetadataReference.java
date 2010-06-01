package se.swami.saml.metadata.collector.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import se.swami.saml.metadata.collector.CertificateValidator;
import se.swami.saml.metadata.collector.MetadataIOException;

public class FileMetadataReference extends MetadataReferenceBase {

	private File metadataFile;
	
	public FileMetadataReference(File metadataFile) {
		this.metadataFile = metadataFile;
	}
	
	public String getLocation() {
		return metadataFile.getAbsolutePath();
	}

	public CertificateValidator getValidator() {
		return null;
	}

	public InputStream getMetadata() throws MetadataIOException {
		try {
			return new FileInputStream(metadataFile);
		} catch (IOException ex) {
			throw new MetadataIOException(ex);
		}
	}
	
	public boolean isRemote() {
		return false;
	}

}
