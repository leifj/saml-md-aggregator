package net.nordu.mdx.signer;

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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class PKCS11Signer implements MetadataSigner {

	private char[] pin;
	private String providerClassName;
	private String configName;
	
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
	
	private KeyStore keyStore;
	private boolean isInitialized = false;
	
	public KeyStore getKeyStore() {
		return keyStore;
	}
	
	@Override
	public Document sign(Document doc, String signer) throws Exception {
		
		if (!isInitialized)
			initialize();
		
		if (signer == null)
			return doc;
		
		char[] pin = getPin();
		PrivateKey signerKey = (PrivateKey)getKeyStore().getKey(signer, pin);
		X509Certificate certificate = (X509Certificate)getKeyStore().getCertificate(signer);
		//Key key = server.getKeyStore().getKey(signer, pin); 
		Node root = doc.getDocumentElement();
		Node firstChild = root.getFirstChild();
		DOMSignContext dsc = new DOMSignContext(signerKey, root, firstChild);
	
		//TODO: figure out if we need to allow users to specify the provider...
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
		return doc;
	}
	
	private synchronized void initialize() 
		throws Exception 
	{
		if (!isInitialized)
			return;
		
		Class<Provider> providerClass = (Class<Provider>)Class.forName(providerClassName);
		Constructor<Provider> constructor = providerClass.getConstructor(new Class[] { String.class });
		Provider cryptoProvider = constructor.newInstance(configName);
		int pos = Security.addProvider(cryptoProvider);
		if (pos == -1)
			throw new IllegalArgumentException("Unable to add crypto provider: "+providerClassName);
		
		keyStore = KeyStore.getInstance("PKCS11");
		keyStore.load(null,pin);
		isInitialized = true;
	}

}
