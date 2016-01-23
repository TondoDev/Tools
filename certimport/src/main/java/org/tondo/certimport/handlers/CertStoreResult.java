package org.tondo.certimport.handlers;

import java.security.cert.Certificate;

public class CertStoreResult {

	private Certificate mathchingCert;
	private String matchingAlias;
	private int certificatesAdded;
	
	public Certificate getMatchingCertificat() {
		return this.mathchingCert;
	}
	
	void setMatchingCertificat(Certificate cert) {
		this.mathchingCert = cert;
	}
	
	public String getMatchingAlias() {
		return matchingAlias;
	}
	
	void setMatchingAlias(String matchingAlias) {
		this.matchingAlias = matchingAlias;
	}
	
	public int getCertificatesAdded() {
		return certificatesAdded;
	}
	
	void setCertificatesAdded(int certificatesAdded) {
		this.certificatesAdded = certificatesAdded;
	}
}
