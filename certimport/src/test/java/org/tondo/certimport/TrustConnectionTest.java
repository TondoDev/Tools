package org.tondo.certimport;

import static org.junit.Assert.*;
import static org.tondo.certimport.CertImportTestUtils.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.junit.Test;
import org.tondo.certimport.handlers.CertStoreResult;
import org.tondo.testutils.StandardTestBase;

/**
 * 
 * @author @TondoDev
 *
 */
public class TrustConnectionTest extends StandardTestBase{
	
	@Test
	public void testNullInputConstructorParams() throws IOException {
		// nulls should create empty trustsore
		// getting exception 
		// java.security.InvalidAlgorithmParameterException: the trustAnchors parameter must be non-empty
		TrustedConnectionManager manager = new TrustedConnectionManager(null, "mytrust".toCharArray());
		URLConnection conn = manager.getConnection(new URL("https://www.facebook.com"));
		try {
			checkReadabilityFromConnection(conn);
			fail("SSLException expected");
		} catch (SSLException e) {
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testWrongKeystorePWD() throws IOException {
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			new TrustedConnectionManager(input, "blabla".toCharArray());
			fail("CertimportException expected!");
		} catch (CertimportException e) {
			
		}
	}
	
	@Test
	public void testGetConnection() throws FileNotFoundException, IOException {
		
		// empty trustore, no trusted certificate, connection fails
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			TrustedConnectionManager emptyManager = new TrustedConnectionManager(input, "trusted".toCharArray());
			URLConnection fbConn = emptyManager.getConnection(new URL("https://www.facebook.com"));
			try {
				checkReadabilityFromConnection(fbConn);
				fail("Expected exception");
			} catch (SSLHandshakeException e) {}
		}
		
		try (InputStream input = new FileInputStream(this.inResourceDir("fbtrust").toString())) {
			TrustedConnectionManager emptyManager = new TrustedConnectionManager(input, "trusted".toCharArray());
			URLConnection fbConn = emptyManager.getConnection(new URL("https://www.facebook.com"));
			checkReadabilityFromConnection(fbConn);
		}
	}
	
	@Test
	public void testRootCertificate() throws FileNotFoundException, IOException {
		TrustedConnectionManager emptyManager = null;
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			emptyManager = new TrustedConnectionManager(input, "trusted".toCharArray());
		}
		
		URL fburl = new URL("https://www.facebook.com");
		URLConnection fbConn = emptyManager.getConnection(fburl);
		// trustsore is empty so it should fail with handshake
		try {
			checkReadabilityFromConnection(fbConn);
			fail("Expected exception");
		} catch (SSLHandshakeException e) {}
		
		// now we add root certificate to connection manager
		CertStoreResult result = emptyManager.addRootCertificate(fburl, "facebook");
		assertEquals("One certificate should be added", 1, result.getCertificatesAdded());
		
		// now connection should be established
		fbConn = emptyManager.getConnection(fburl);
		checkReadabilityFromConnection(fbConn);
	}
	

}
