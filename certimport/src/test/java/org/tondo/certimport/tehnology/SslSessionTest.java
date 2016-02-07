package org.tondo.certimport.tehnology;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Test;
import org.tondo.testutils.StandardTestBase;

/**
 * SSL framework tests to better understand technology. This test is focused on SSL session.
 * When connections to same host are created from same SSL context, SSL handshake is occurred only
 * at the beginning of first connection, because all connection to same host share same SSL session.
 * 
 * SSL session can be invalidated using {@code invalidate()} call. Next SSL connection will be assigned to new
 * SSL session and handshake will be made again. Active connections in invalidated session will work normally, till
 * connection is closed.
 * 
 * @author TondoDev
 *
 */
public class SslSessionTest extends StandardTestBase {
	
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
	
	/**
	 * Session sharing demo with HttpsUrlConnection
	 */
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
		assertEquals("Seccond session is also shared - no handshake", 2, handler.getHandshakeCount());
		con.disconnect();
		
		// once again try first connection if session was forgotten or not
		con = createConnection(fbUrl);
		con.connect();
		assertEquals("First session is still valid - no handshage", 2, handler.getHandshakeCount());
		con.disconnect();
	}
	
	
	@Test
	public void testReHandshakeWithSingleSSLScokets() throws KeyManagementException, NoSuchAlgorithmException, UnknownHostException, IOException {
		ExperimentalTrustManager handler = new ExperimentalTrustManager();
		initContext(handler);

		try (SSLSocket fbSocket = (SSLSocket) this.socketFactory.createSocket("https://www.facebook.com", 443)) {
			fail("with ssl socket, host must be provided without protocol");
		} catch (UnknownHostException e) {}
		
		// autocloseable
		try (SSLSocket fbSocket = (SSLSocket) this.socketFactory.createSocket("www.facebook.com", 443)) {
			assertEquals("handshake is not triggered right after socket is created", 0, handler.getHandshakeCount());
			
			fbSocket.startHandshake();
			assertEquals("Firstime handshake", 1, handler.getHandshakeCount());
			fbSocket.startHandshake();
			assertEquals("Secondtime handshake - ssl session is reused also in sockets", 1, handler.getHandshakeCount());
			
			// according to documentation, for full reauthentication session must be invalidated
			// connection already created with this session still belongs to it until they are closed
			fbSocket.getSession().invalidate();
			assertFalse("Session is invalid now", fbSocket.getSession().isValid());
			fbSocket.startHandshake();
			assertEquals("Third handshake - connection already created with this session still belongs to it until they are closed", 1, handler.getHandshakeCount());
		}
	}
	
	@Test
	public void testSessionBetweenMultipleSockets() throws UnknownHostException, IOException, KeyManagementException, NoSuchAlgorithmException {
		ExperimentalTrustManager handler = new ExperimentalTrustManager();
		initContext(handler);
		
		try (SSLSocket fbSocket = (SSLSocket) this.socketFactory.createSocket("www.facebook.com", 443)) {
			assertEquals("handshake is not triggered right after socket is created", 0, handler.getHandshakeCount());
			assertTrue("Session accessor causes session initialization", fbSocket.getSession().isValid());
			assertEquals("Handshake was triggered by session initialization", 1, handler.getHandshakeCount());
		}
		
		try (SSLSocket fbSocket = (SSLSocket) this.socketFactory.createSocket("www.facebook.com", 443)) {
			assertEquals("Handshake count remains from previous socket instance", 1, handler.getHandshakeCount());
		}
	}
	
	@Test
	public void testSessionInvalidation() throws UnknownHostException, IOException, KeyManagementException, NoSuchAlgorithmException {
		ExperimentalTrustManager handler = new ExperimentalTrustManager();
		initContext(handler);
		
		SSLSession session = null;
		try (SSLSocket fbSocket = (SSLSocket) this.socketFactory.createSocket("www.facebook.com", 443)) {
			assertEquals("handshake is not triggered right after socket is created", 0, handler.getHandshakeCount());
			session = fbSocket.getSession();
			assertTrue("Session accessor causes session initialization", session.isValid());
			assertEquals("Handshake was triggered by session initialization", 1, handler.getHandshakeCount());
		}
		
		session.invalidate();
		try (SSLSocket fbSocket = (SSLSocket) this.socketFactory.createSocket("www.facebook.com", 443)) {
			assertEquals("Handshake count remains from previous socket instance", 1, handler.getHandshakeCount());
			fbSocket.startHandshake();
			assertEquals("Handshake occurs again because session was invalidated", 2, handler.getHandshakeCount());
		}
		
		try (SSLSocket fbSocket = (SSLSocket) this.socketFactory.createSocket("www.facebook.com", 443)) {
			fbSocket.startHandshake();
			assertEquals("sharing previous session created after invalidation - no handshake", 2, handler.getHandshakeCount());
		
		}
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
