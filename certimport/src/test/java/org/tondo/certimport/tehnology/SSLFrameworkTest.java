package org.tondo.certimport.tehnology;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Before;
import org.junit.Test;
import org.tondo.certimport.managers.AllTrustManager;
import org.tondo.certimport.managers.InterceptingX509Manager;
import org.tondo.testutils.StandardTestBase;

/**
 * SSL framework tests to better understand technology
 * @author TondoDev
 *
 */
public class SSLFrameworkTest extends StandardTestBase {
	
	private static class ExperimentalTrustManager implements X509TrustManager {
		private int handshakeCount = 0;

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			handshakeCount++;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public int getHandshakeCount() {
			return handshakeCount;
		}
		
	}
	
	private SSLSocketFactory socketFactory;
	
	@Test
	public void testHandshakesExecuted() throws KeyManagementException, NoSuchAlgorithmException, IOException {
		ExperimentalTrustManager handler = new ExperimentalTrustManager();
		initContext(handler);
		URL fbUrl = new URL("https://www.facebook.com");
		HttpsURLConnection con = createConnection(fbUrl);
		con.connect();
		assertEquals("Firstime handshake should occur", 1, handler.getHandshakeCount());
		con.disconnect();
		
		con = createConnection(fbUrl);
		con.connect();
		assertEquals("Second handshake didn't occur because of shared ssl session", 1, handler.getHandshakeCount());
		con.disconnect();
		
		URL ytUrl = new URL("https://www.youtube.com/");
		con = createConnection(ytUrl);
		con.connect();
		assertEquals("Connection to other location is in other ssl session so handshage occurs", 2, handler.getHandshakeCount());
		con.disconnect();
		
		con = createConnection(ytUrl);
		con.connect();
		assertEquals("Seccond session is also shared - no handshage", 2, handler.getHandshakeCount());
		con.disconnect();
		
		// once again try first connection if session was forgotten or not
		con = createConnection(fbUrl);
		con.connect();
		assertEquals("First session is still valid - no handshage", 2, handler.getHandshakeCount());
		con.disconnect();
	}
	
	
	private void initContext(X509TrustManager handler) throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(null, new TrustManager[] {handler}, null);
		this.socketFactory = context.getSocketFactory();
	}
	
	private HttpsURLConnection createConnection(URL location) throws KeyManagementException, NoSuchAlgorithmException, IOException {
		HttpsURLConnection conn = (HttpsURLConnection)location.openConnection();
		conn.setSSLSocketFactory(this.socketFactory);
		return conn;
	}
	
}
