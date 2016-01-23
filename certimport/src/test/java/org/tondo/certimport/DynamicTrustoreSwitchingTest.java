package org.tondo.certimport;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Test;
import org.tondo.testutils.StandardTestBase;

/**
 * 
 * @author TondoDev
 *
 */
public class DynamicTrustoreSwitchingTest extends StandardTestBase{
	
	@Test
	public void testGetKeyManagers() throws 
			NoSuchAlgorithmException, 
			IOException, 
			KeyStoreException, 
			CertificateException, 
			UnrecoverableKeyException {
		
		// probably system dependent
		assertEquals("Default algorithm", "SunX509", KeyManagerFactory.getDefaultAlgorithm());
		
		KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		assertNotNull(factory);
		assertEquals("Created factory with algorithm", "SunX509", factory.getAlgorithm());
		
		try {
			KeyManagerFactory.getInstance("bla bla");
			fail("NoSuchAlgorithmException expected!");
		} catch (NoSuchAlgorithmException e) {}
		
		
		assertEquals("Keystore default type", "jks", KeyStore.getDefaultType());
		
		final String PWD = "trusted";
		// trustrore with single certificate entry
		Path keystoreFile = getFileKeeper().copyResource(Paths.get("fbtrust"));
		KeyStore keyStore = KeyStore.getInstance("jks");
		try (FileInputStream fis = new FileInputStream(keystoreFile.toFile())) {
			keyStore.load(fis, PWD.toCharArray());
		}
		
		factory.init(keyStore, PWD.toCharArray());
		KeyManager[] managers = factory.getKeyManagers();
		assertEquals("Key managers count", 1, managers.length);
	}
	
	/**
	 * TrustManagers are responsible for managing the trust material that is used when making trust decisions, 
	 * and for deciding whether credentials presented by a peer should be accepted.
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 */
	@Test
	public void testGetTrustManagers() throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException {
		// probably system dependent
		String defaultStrustAlgo  = TrustManagerFactory.getDefaultAlgorithm();
		// Public Key Infrastructure
		assertEquals("Default algorithm", "PKIX", defaultStrustAlgo);

		// throws NoSuchAlgorithmException
		TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(defaultStrustAlgo);
		assertNotNull(trustFactory);
		
		Path trustoreFile = getFileKeeper().copyResource(Paths.get("fbtrust"));
		// throws KeyStoreException
		KeyStore keyStore = KeyStore.getInstance("jks");
		final String PWD = "trusted";
		try (FileInputStream fis = new FileInputStream(trustoreFile.toFile())) {
			keyStore.load(fis, PWD.toCharArray());
		}

		try {
			trustFactory.getTrustManagers();
			fail("IllegalStateException exepcted, because trust factory is not initialized");
		} catch(IllegalStateException e) {
			// TrustManagerFactory is not initialized
		}
		// no password needed
		// throws KeyStoreException
		trustFactory.init(keyStore);
		TrustManager[] trustManagers =  trustFactory.getTrustManagers();
		assertEquals(1, trustManagers.length);
	}
	
	@Test
	public void testSSLContextInitialization() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		// encapsulated loading trust managers from trusstore
		Path trustoreFile = getFileKeeper().copyResource(Paths.get("fbtrust"));
		TrustStoreLoader loader = new TrustStoreLoader();
		TrustManager[] managers  = loader.getTrustManagers(trustoreFile.toFile(), "trusted");
		assertEquals(1, managers.length);
		
		// on my system SSL is not default
		assertEquals("Default", SSLContext.getDefault().getProtocol());
		assertEquals("SSL", SSLContext.getInstance("SSL").getProtocol());

		try {
			SSLContext.getInstance("asdasd");
			fail("NoSuchAlgorithmException expected!");
		} catch(NoSuchAlgorithmException e) {}
		
		SSLContext context = SSLContext.getInstance("SSL");
		// throws KeyManagementException
		// null for two first parameters means than security providers are searched by highest priority
		context.init(null, managers, null);
		
		// open connection
		URL address = new URL("https://www.facebook.com/");
		URLConnection connection = address.openConnection();
		assertTrue("connection should be https", connection instanceof HttpsURLConnection);
		HttpsURLConnection httpsConn = (HttpsURLConnection)connection;
		httpsConn.setSSLSocketFactory(context.getSocketFactory());

		// whem missing certificate this should crash
		httpsConn.getInputStream().read();
		httpsConn.disconnect();
	}
	
	@Test
	public void testSwitchTrustores() 
			throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		Path emptyTrustFile = getFileKeeper().copyResource(Paths.get("trustempty"));
		URL address = new URL("https://www.facebook.com/");
		HttpsURLConnection httpsConn = enchrichConnectionByTrustContext((HttpsURLConnection) address.openConnection(), 
				emptyTrustFile.toFile(), "trusted");
		
		// trustore is empty, so server is not authenticated
		try {
			httpsConn.getInputStream();
			fail("SSLHandshakeException exepected");
		} catch(SSLHandshakeException e) {}
		
		// again with trustore containing correct certificate
		Path fbTustFile = getFileKeeper().copyResource(Paths.get("fbtrust"));
		address = new URL("https://www.facebook.com/");
		httpsConn = enchrichConnectionByTrustContext((HttpsURLConnection) address.openConnection(), 
				fbTustFile.toFile(), "trusted");
		
		// whem missing certificate this should crash
		httpsConn.getInputStream().read();
		httpsConn.disconnect();
	}
	
	
	private HttpsURLConnection enchrichConnectionByTrustContext(HttpsURLConnection conn, File trustFile, String pwd) 
			throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
		TrustStoreLoader loader = new TrustStoreLoader();
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(null, loader.getTrustManagers(trustFile, pwd), null);
		conn.setSSLSocketFactory(context.getSocketFactory());
		return conn;
	}
}
