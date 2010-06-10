package net.nordu.mdx.signer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import net.nordu.mdx.signer.MetadataSigner;

import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class JKSSigner implements MetadataSigner {

	private char[] pin;
	private String providerClassName;
	private String configName;
	private String providerType;
	private String keyStoreLocation;
	
	public void setPin(char[] pin) {
		this.pin = pin;
	}
	
	public void setPin(String pin) {
		this.pin = pin.toCharArray();
	}
	
	public char[] getPin() {
		return pin;
	}
	
	public void setProviderClassName(String providerClassName) {
		this.providerClassName = providerClassName;
	}
	
	public String getProviderClassName() {
		return providerClassName;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public void setConfigName(String configName) {
		this.configName = configName;
	}
	
	public void setProviderType(String providerType) {
		this.providerType = providerType;
	}
	
	public String getProviderType() {
		return providerType;
	}
	
	public String getKeyStoreLocation() {
		return keyStoreLocation;
	}
	
	public void setKeyStoreLocation(String keyStoreLocation) {
		this.keyStoreLocation = keyStoreLocation;
	}
	
	private KeyStore keyStore;
	
	@Override
	public void sign(Node node, String signer) throws Exception {
		if (signer == null)
			throw new IllegalArgumentException("Missing signer");
		
		if (node == null)
			throw new IllegalArgumentException("Not signing empty document");
		
		char[] pin = getPin();
		System.err.println(signer);
		PrivateKey signerKey = (PrivateKey)keyStore.getKey(signer, pin);
		if (signerKey == null)
			throw new IllegalArgumentException("No such signer: "+signer);
		
		X509Certificate certificate = (X509Certificate)keyStore.getCertificate(signer);
		//Key key = server.getKeyStore().getKey(signer, pin); 
		System.err.println(node);
		assert(node != null);
		Node firstChild = node.getFirstChild();
		assert(firstChild != null);
		System.err.println(firstChild);
		DOMSignContext dsc = new DOMSignContext(signerKey, node, firstChild);
	
		//TODO: figure out if we really need to allow users to specify the provider...
		String providerName = System.getProperty("jsr105Provider","org.jcp.xml.dsig.internal.dom.XMLDSigRI");
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
				((Class<Provider>)Class.forName(providerName)).newInstance()); 
		
		Reference ref = 
			fac.newReference("",
					fac.newDigestMethod(DigestMethod.SHA1, null),
					Collections.singletonList(fac.newTransform(Transform.ENVELOPED,(TransformParameterSpec) null)), null, null); 

		SignedInfo si = 
			fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
					(C14NMethodParameterSpec) null),
					fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
					Collections.singletonList(ref));
		
		KeyInfoFactory kif = fac.getKeyInfoFactory(); 
		X509Data xd = kif.newX509Data(Collections.singletonList(certificate));
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));
		XMLSignature signature = fac.newXMLSignature(si, ki); 
		signature.sign(dsc);
	}
	
	
	public final void initialize() 
		throws Exception 
	{
		if (getProviderClassName() != null && getProviderClassName().length() > 0) {
			Class<Provider> providerClass = (Class<Provider>)Class.forName(getProviderClassName());
			Provider cryptoProvider = null;
			
			if (getConfigName() != null && getConfigName().length() > 0) {
				Constructor<Provider> constructor = providerClass.getConstructor(new Class[] { String.class });
				cryptoProvider = constructor.newInstance(getConfigName());
			} else {
				cryptoProvider = providerClass.newInstance();
			}
			assert(cryptoProvider != null);
			int pos = Security.addProvider(cryptoProvider);
			if (pos == -1)
				throw new IllegalArgumentException("Unable to add crypto provider: "+getProviderClassName());
		}
		
		keyStore = KeyStore.getInstance(getProviderType());
		InputStream ksIn = null;
		if (keyStoreLocation != null && keyStoreLocation.length() > 0) {
			File ksFile = new File(keyStoreLocation);
			ksIn = new FileInputStream(ksFile);
		}
		keyStore.load(ksIn,pin);
	}

}
