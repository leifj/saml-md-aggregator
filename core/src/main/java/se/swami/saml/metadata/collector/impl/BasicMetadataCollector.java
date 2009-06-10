/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.oasis.saml.metadata.EntitiesDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorDocument;
import org.oasis.saml.metadata.EntityDescriptorType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import se.swami.saml.metadata.collector.CertificateValidator;
import se.swami.saml.metadata.collector.MetadataCollector;
import se.swami.saml.metadata.collector.MetadataCollectorException;
import se.swami.saml.metadata.collector.MetadataIOException;
import se.swami.saml.metadata.collector.MetadataReference;
import se.swami.saml.metadata.collector.MetadataValidationException;
import se.swami.saml.metadata.utils.StreamUtils;

public class BasicMetadataCollector implements MetadataCollector {

	private static final Log log = LogFactory.getLog(BasicMetadataCollector.class);
	
	// most of this is copy-pasted from the apache xml-security samples
	protected void validate(String xml, CertificateValidator validator) throws MetadataCollectorException {
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc =
	            dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
			
			// Find Signature element
	        NodeList nl =
	            doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
	        if (nl.getLength() == 0) {
	            throw new Exception("Cannot find Signature element");
	        }
	
	        // Create a DOM XMLSignatureFactory that will be used to unmarshal the
	        // document containing the XMLSignature
	        String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
	        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",(Provider)Class.forName(providerName).newInstance());
	
	        // Create a DOMValidateContext and specify a KeyValue KeySelector
	        // and document context
	        DOMValidateContext valContext = new DOMValidateContext(new X509KeySelector(validator), nl.item(0));
	        
	        // unmarshal the XMLSignature
	        XMLSignature signature = fac.unmarshalXMLSignature(valContext);
	        
	        // Validate the XMLSignature (generated above)
	        boolean coreValidity = signature.validate(valContext);

	        // Check core validation status
	        if (coreValidity == false) {
	            System.err.println("Signature failed core validation");
	            boolean sv = signature.getSignatureValue().validate(valContext);
	            System.out.println("signature validation status: " + sv);
	            // check the validation status of each Reference
	            Iterator<?> iterator = signature.getSignedInfo().getReferences().iterator();
	            for (int j=0; iterator.hasNext(); j++) {
	                boolean refValid = ((Reference) iterator.next()).validate(valContext);
	                log.error("ref["+j+"] validity status: " + refValid);
	            }
	            
	            throw new MetadataValidationException("Invalid signature");
	        } else {
	        	log.debug("Signature passed core validation");
	        }
		} catch (XMLSignatureException ex) {
			throw new MetadataValidationException(ex);
		} catch (KeySelectorException ex) {
			throw new MetadataValidationException(ex);
		} catch (Exception ex) {
			throw new MetadataCollectorException(ex);
		}
		
	}
	
    private class X509KeySelector extends KeySelector {
    	
    	private CertificateValidator validator;
    	
    	public X509KeySelector(CertificateValidator validator) {
    		this.validator = validator;
    	}
    	
        public KeySelectorResult select(KeyInfo keyInfo,
                                        KeySelector.Purpose purpose,
                                        AlgorithmMethod method,
                                        XMLCryptoContext context)
            throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }
            SignatureMethod sm = (SignatureMethod) method;
            List<?> list = keyInfo.getContent();
            for (Object elt : list) {
                if (elt instanceof X509Data) {
                	
                	X509Data x509Data = (X509Data)elt;
                	List<?> certList = x509Data.getContent();
                	for (Object celt : certList) {
                		if (celt instanceof X509Certificate) {
                			X509Certificate certificate = (X509Certificate)celt;
                			//System.err.println(certificate);
                			try {
                				validator.validate(certificate);
                			} catch (Exception ex) {
                				throw new KeySelectorException(ex);
                			}
                			PublicKey publicKey = certificate.getPublicKey();
                			if (algEquals(sm.getAlgorithm(), publicKey.getAlgorithm())) {
                				return new SimpleKeySelectorResult(publicKey);
                			}
                			System.err.println(sm.getAlgorithm()+" != "+publicKey.getAlgorithm());
                		}
                	}
                }
            }
            throw new KeySelectorException("No KeyValue element found!");
        }

		//@@@FIXME: this should also work for key types other than DSA/RSA
        private boolean algEquals(String algURI, String algName) {
            if (algName.equalsIgnoreCase("DSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
                return true;
            } else if (algName.equalsIgnoreCase("RSA") &&
                    	algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
                return true;
            } else {
                return false;
            }
        }
    }
	
    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private PublicKey pk;
        SimpleKeySelectorResult(PublicKey pk) {
            this.pk = pk;
        }

        public Key getKey() { return pk; }
    }
    
    public Collection<EntityDescriptorType> processXml(MetadataReference collection, InputStream in) throws MetadataCollectorException {
    	
    	List<EntityDescriptorType> entities = new ArrayList<EntityDescriptorType>();
    	
    	if (in == null)
    		return entities;
    	
		try {
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	StreamUtils.copyStream(baos, in);
	    	String xmlData = baos.toString();
	    	
	    	if (collection.getValidator() != null)
				validate(xmlData,collection.getValidator());
	    	
	    	if (xmlData.contains("EntitiesDescriptor")) {
				EntitiesDescriptorDocument doc = EntitiesDescriptorDocument.Factory.parse(xmlData);
				EntityDescriptorType[] entityArray = doc.getEntitiesDescriptor().getEntityDescriptorArray();
				for (EntityDescriptorType entity : entityArray) {
					entities.add(entity);
				}
			} else if (xmlData.contains("EntityDescriptor")) {
				EntityDescriptorDocument doc = EntityDescriptorDocument.Factory.parse(xmlData);
				entities.add(doc.getEntityDescriptor());
			} else {
				throw new IllegalArgumentException("Unknown metadata format");
			}
		} catch (IOException ex) {
			throw new MetadataCollectorException(ex);
		} catch (XmlException ex) {
			throw new MetadataCollectorException(ex);
		}
		
		return entities;
    }
	
    private InputStream getXML(String uri) throws IOException {
        System.err.println(uri);
        
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
	        HttpClient client = new HttpClient();
	        GetMethod get = new GetMethod(uri);
	        int code = client.executeMethod(get);
	        if (code == 200) {
	        	return get.getResponseBodyAsStream();
	        } else {
	            return null;
	        }
        } else {
        	return Thread.currentThread().getContextClassLoader().getResourceAsStream(uri);
        }
    }
    
	public Collection<EntityDescriptorType> fetch(MetadataReference collection) throws MetadataCollectorException {
		try {
			InputStream mdxml = getXML(collection.getLocation().toString());
			if (mdxml == null)
				throw new MetadataIOException("Unable to fetch "+collection.getLocation().toString());
			return processXml(collection,mdxml);
		} catch (IOException ex) {
			throw new MetadataIOException(ex);
		}
	}
}
