/*
 * Created on Apr 21, 2008
 *
 */
package se.swami.saml.metadata.collector.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import se.swami.saml.metadata.collector.CertificateValidator;
import se.swami.saml.metadata.collector.MetadataCollectorException;
import se.swami.saml.metadata.collector.MetadataValidationException;

public class PKIXCertificateValidator implements CertificateValidator {
	
	private Set<TrustAnchor> taSet;

	public PKIXCertificateValidator() { 
		taSet = new HashSet<TrustAnchor>();
	}
	
	protected Set<TrustAnchor> getTrustAnchors() {
		return taSet;
	}
	
	public void addTrustAnchor(X509Certificate anchor)
	{
		TrustAnchor ta = new TrustAnchor(anchor,null);
		taSet.add(ta);
	}
	
	public PKIXCertificateValidator(X509Certificate anchor) {
		this();
		addTrustAnchor(anchor);
	}
	
	public void validate(X509Certificate certificate) throws MetadataCollectorException
	{
		try {
			X509CertSelector criteria = new X509CertSelector();
			criteria.setCertificate(certificate);
			PKIXBuilderParameters bp = new PKIXBuilderParameters(taSet,criteria);
			CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
			CertPathBuilderResult buildResult = builder.build(bp);
			CertPath path = buildResult.getCertPath();
			CertPathValidator validator = CertPathValidator.getInstance("PKIX");
			validator.validate(path,new PKIXParameters(taSet));
		} catch (CertPathBuilderException ex) {
			throw new MetadataValidationException(ex);
		} catch (CertPathValidatorException ex) {
			throw new MetadataValidationException(ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw new MetadataCollectorException(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new MetadataCollectorException(ex);
		} 
	}
	
}
