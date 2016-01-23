package org.tondo.certimport.managers;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.tondo.certimport.handlers.TrustedHandler;

/**
 * 
 * @author TondoDev
 *
 */
public class InterceptingX509Manager implements X509TrustManager {
	
	private X509TrustManager originalTrustManager;
	private TrustedHandler handler;
	
	public InterceptingX509Manager(X509TrustManager original) {
		this.originalTrustManager = original;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		this.originalTrustManager.checkClientTrusted(chain, authType);
		
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (this.handler != null) {
			this.handler.handlerServerCert(originalTrustManager, chain, authType);
		} else {
			this.originalTrustManager.checkServerTrusted(chain, authType);
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.originalTrustManager.getAcceptedIssuers();
	}
	
	
	public void setTrustedHandler(TrustedHandler th) {
		this.handler = th;
	}

}
