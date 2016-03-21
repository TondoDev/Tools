package org.tondo.certimport.handlers;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class CertStoreResult {
	
	public static class CertificateEntry {
		private String alias;
		private X509Certificate certificate;
		
		public CertificateEntry(String alias, X509Certificate cert) {
			this.alias = alias;
			this.certificate = cert;
		}
		
		public String getAlias() {
			return alias;
		}
		
		public X509Certificate getCertificate() {
			return certificate;
		}
	}

	private Certificate[] serverCertChain;
	private CertificateEntry[] addedCertirifcates;
	private CertificateEntry matchingCertificate;
	
	public int getCertificatesAdded() {
		return this.addedCertirifcates == null ? 0 : this.addedCertirifcates.length;
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
	
	
	void setAddedCertificates(CertificateEntry[] certificates) {
		this.addedCertirifcates = Arrays.copyOf(certificates, certificates.length);
	}
	
	public CertificateEntry[] getAddedCertificates() {
		return this.addedCertirifcates == null ? new CertificateEntry[] {} : this.addedCertirifcates;
	}
	
	void setMatchingCertificate(CertificateEntry matching) {
		this.matchingCertificate = matching;
	}
	
	public CertificateEntry getMatchingCertificate() {
		return matchingCertificate;
	}
}
