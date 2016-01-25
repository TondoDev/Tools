package org.tondo.certimport.handlers;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class CertStoreResult {

	private X509Certificate mathchingCert;
	private String matchingAlias;
	private int certificatesAdded;
	private Certificate[] serverCertChain;
	
	public X509Certificate getMatchingCertificate() {
		return this.mathchingCert;
	}
	
	void setMatchingCertificate(X509Certificate cert) {
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
	
	/**
	 * Stores copy of certificate chain. 
	 * @param chain
	 * 	certificate chain
	 * 	
	 */
	void setServerCertChain(X509Certificate[] chain) {
		this.serverCertChain = Arrays.copyOf(chain, chain.length);
	}
	
	public Certificate[] getServerCertChain() {
		return serverCertChain;
	}
}
