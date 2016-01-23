package org.tondo.certimport.handlers;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public interface TrustedHandler {

	public void handlerServerCert(X509TrustManager original, X509Certificate[] chain, String authType);
}
