package org.tondo.certimport.managers;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class AllTrustManager implements X509TrustManager{

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// nothing to do
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// nothing to do
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return  new X509Certificate[] {};
	}

}
