package org.tondo.certimport.handlers;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import org.tondo.certimport.CertStoringOption;
import org.tondo.certimport.CertimportException;
import org.tondo.certimport.handlers.CertStoreResult.CertificateEntry;

public class StoreCertificateChainHandler implements TrustedHandler {
	
	private StoringConfiguration configuration;
	private KeyStore trustStore;
	
	private CertStoreResult lastResult;
	
	public StoreCertificateChainHandler(StoringConfiguration conf, KeyStore truststore) {
		this.trustStore = truststore;
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
		} catch (RuntimeException e) {
			// this can happen when trustore is empty without any entry.
			if (e.getCause() instanceof InvalidAlgorithmParameterException) {
				alreadyTrustred = false;
			} else {
				throw new CertimportException("Error occured during server certificate check!", e);
			}
		}
		
		CertStoreResult result = new CertStoreResult();
		if (alreadyTrustred) {
			for (X509Certificate cert : chain) {
				try {
					String foundAlias = this.trustStore.getCertificateAlias(cert);
					if (foundAlias != null) {
						result.setMatchingCertificate(new CertificateEntry(foundAlias, cert));
					}
				} catch (KeyStoreException e) {
					throw new CertimportException("Key store problem!", e);
				}
				
			}
		}
		CertStoringOption option = configuration.getOption();
		// store only if cert is not already trusted or if it is forced and is enabled by configuration
		if (option != CertStoringOption.DONT_ADD && (!alreadyTrustred || configuration.isAddEvenIfTrusted())) {
			
			X509Certificate[] certsToStore = null;
			if (option == CertStoringOption.CHAIN) {
				certsToStore = Arrays.copyOf(chain, chain.length);
			} else {
				if (option == CertStoringOption.ROOT) {
					// TODO can server send 0 certificates?
					certsToStore = new X509Certificate[] {chain[chain.length - 1]};
				} else if (option == CertStoringOption.LEAF) {
					certsToStore =  new X509Certificate[] {chain[0]};
				}
			}
			
			try {
				List<CertificateEntry> added = new ArrayList<>();
				for (X509Certificate cert : certsToStore) {
					String newCertAlias = configuration.getAliasCreator().createAlias(cert);
					trustStore.setCertificateEntry(newCertAlias, cert);
					added.add(new CertificateEntry(newCertAlias, cert));
				}
				result.setAddedCertificates(added.toArray(new CertificateEntry[added.size()]));
			} catch (KeyStoreException e) {
				throw new CertimportException("Key store problem!", e);
			}
		}
		
		result.setServerCertChain(chain);
		this.lastResult = result;
	}
	
	public CertStoreResult getResult() {
		return this.lastResult;
	}
}
