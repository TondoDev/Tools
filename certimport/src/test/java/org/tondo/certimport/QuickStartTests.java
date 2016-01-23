package org.tondo.certimport;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;

import javax.net.ssl.SSLHandshakeException;

import org.junit.Test;
import org.tondo.testutils.StandardTestBase;

/**
 * Some tests to introduce SSL sertificate 
 * @author TondoDev
 *
 */
public class QuickStartTests extends StandardTestBase {

	/**
	 * Connection to standard insecure HTTP
	 */
	@Test
	public void testConnectionToHttp() throws IOException {
		URL address = new URL("http://www.topky.sk");
		URLConnection connection = address.openConnection();
		assertEquals("sun.net.www.protocol.http.HttpURLConnection", connection.getClass().getCanonicalName());
		// look like this also opens connection
		InputStream input = connection.getInputStream();
		assertNotNull(input);
		
		// according to used protocol, URLConnection should be closed when stream is closed
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
			assertTrue(reader.readLine().length() > 0);
		}
	}
	
	
	/**
	 * Connection to secure HTTPS using URLConnection.
	 * This test demonstrate initial problem of this project.
	 * We want to connect to somewhere using https, but we dont have certificate for that
	 * site in truststore. Importing certs manualy is anoying, so this tool should
	 * speed ut this process.
	 */
	@Test
	public void testConnectionToHttpsUsingUrlConnection() throws IOException {
		getFileKeeper().copyResource(Paths.get("trustempty"));
		// treba zmenit trustore na nieco ine, lebo ten defaultny ma certifikacny chain na vseky zname stranky
		System.setProperty("javax.net.ssl.trustStore", "trustempty");
		System.setProperty("javax.net.ssl.trustStorePassword", "trusted");
		URL address = new URL("https://www.facebook.com/");
		URLConnection connection = address.openConnection();
		assertEquals("sun.net.www.protocol.https.HttpsURLConnectionImpl", connection.getClass().getCanonicalName());
		// look like this also opens connection
		connection.setConnectTimeout(2000);
		
		// this fails because not valid certification path is found in provided truststore
		//javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
		try {
			connection.getInputStream();
			fail("SSLHAndshakeExceptionExpected");
		} catch (SSLHandshakeException e) {}
	}
	
	
	// setting javax.net.ssl.trustStore multiple times in one program doesn't work
//	/**
//	 * Connect to https site using trustrore only with leaf site certificate (not whole chain)
//	 * @throws IOException 
//	 */
//	@Test
//	public void testConnectWithLeafCertOnly() throws IOException {
//		getFileKeeper().copyResource(Paths.get("gtitrust"));
//		System.setProperty("javax.net.ssl.trustStore", "gtitrust");
//		System.setProperty("javax.net.ssl.trustStorePassword", "trusted");
//		URL address = new URL("https://mail2.gratex.com:443/");
//		URLConnection connection = address.openConnection();
//		assertEquals("sun.net.www.protocol.https.HttpsURLConnectionImpl", connection.getClass().getCanonicalName());
//		// look like this also opens connection
//		connection.setConnectTimeout(2000);
//		System.out.println();connection.getInputStream().read();
////		BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()));
////		System.out.println(reader.readLine());
//	}
}
