package org.tondo.certimport.handlers;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.tondo.certimport.CertStoringOption;
import org.tondo.certimport.CertimportException;

public class StoreCertificateChainHandler implements TrustedHandler {
	
	private StoringConfiguration configuration;
	private KeyStore trustStore;
	
	private CertStoreResult lastResult;
	
	public StoreCertificateChainHandler(StoringConfiguration conf, KeyStore truststores) {
		this.trustStore = truststores;
		this.configuration = conf;
	}

	@Override
	public void handlerServerCert(X509TrustManager original, X509Certificate[] chain, String authType)  {
		this.lastResult = null;
		
		boolean alreadyTrustred = true;
		try {
			original.checkServerTrusted(chain, authType);
		} catch (CertificateException e) {
			alreadyTrustred = false;
		}
		
		CertStoreResult result = new CertStoreResult();
		if (alreadyTrustred) {
			for (X509Certificate cert : chain) {
				try {
					String foundAlias = this.trustStore.getCertificateAlias(cert);
					if (foundAlias != null) {
						result.setMatchingAlias(foundAlias);
						result.setMatchingCertificate(cert);
					}
				} catch (KeyStoreException e) {
					throw new CertimportException("Key store problem!", e);
				}
				
			}
		}
		int addedCerts = 0;
		// store only if cert is not already trusted or if it is forced
		if (!alreadyTrustred || configuration.isAddEvenIfTrusted()) {
			
			if (configuration.getOption() == CertStoringOption.CHAIN) {
				
			} else {
				X509Certificate certToStore = null;
				if (configuration.getOption() == CertStoringOption.ROOT) {
					// TODO can server send 0 certificates?
					certToStore = chain[chain.length - 1];
				} else if (configuration.getOption() == CertStoringOption.LEAF) {
					certToStore = chain[0];
				}
				
				try {
					trustStore.setCertificateEntry(configuration.getAlias(), certToStore);
					addedCerts++;
				} catch (KeyStoreException e) {
					throw new CertimportException("Key store problem!", e);
				}
			}
		}
		
		result.setCertificatesAdded(addedCerts);
		result.setServerCertChain(chain);
		this.lastResult = result;
	}
	
	public CertStoreResult getResult() {
		return this.lastResult;
	}
}
