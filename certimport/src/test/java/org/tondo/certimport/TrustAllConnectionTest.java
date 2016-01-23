package org.tondo.certimport;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.SSLHandshakeException;

import org.junit.Test;

public class TrustAllConnectionTest {

	@Test
	public void testNotHttpsUrlConnection() throws MalformedURLException {
		try {
			TrustedConnectionManager.getTrustAllConnection(new URL("http://www.pokec.sk"));
			fail("Expected exception because not https URL");
		} catch (CertimportException e) {
			assertEquals("Not HTTPS connection!", e.getMessage());
		}
	}
	
	@Test
	public void testTrustedURLConnection() throws IOException {
		URLConnection connection = TrustedConnectionManager.getTrustAllConnection(new URL("https://www.facebook.com"));
		byte[] buff = new byte[1024];
		// downloading 
		try (InputStream is = connection.getInputStream()) {
			while (is.read(buff) != -1) {}
		}
	}
	
	@Test
	public void testSelfSignedURL() throws IOException {
		// this site should not be signed by known CA (is self signed)
		URL myFaculty = new URL("https://wis.fit.vutbr.cz/FIT/");
		
		// this should crash because this site is self signed and for this reason
		// it should not be present in default java truststore
		try {
			myFaculty.openConnection().getInputStream();
			fail("Handshake expection expected!");
		} catch (SSLHandshakeException e) {}
		
		// with trust ALL connection it should be possible
		URLConnection connection = TrustedConnectionManager.getTrustAllConnection(myFaculty);
		byte[] buff = new byte[1024];
		// downloading 
		try (InputStream is = connection.getInputStream()) {
			while (is.read(buff) != -1) {}
		}
	}
	
}
