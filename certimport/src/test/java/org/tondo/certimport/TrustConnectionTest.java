package org.tondo.certimport;

import static org.junit.Assert.*;
import static org.tondo.certimport.CertImportTestUtils.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
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
		assertNull("Cetificat was untrusted before", result.getMatchingCertificate());
		
		// now connection should be established
		fbConn = emptyManager.getConnection(fburl);
		checkReadabilityFromConnection(fbConn);
		
		// TODO this won't work because  connecting to same site multiple times shares SSL session
		// need to be solved somehow
		// once again try add same certificate
//		result = emptyManager.addRootCertificate(fburl, "fb2");
//		// because default behavior is add even if exist
//		assertEquals("One certificate should be added even if exists", 1, result.getCertificatesAdded());
//		assertNotNull("Match in trustore was found", result.getMatchingCertificate());
//		assertEquals("Matched shoudl be root certificate", result.getMatchingCertificate(), result.getServerCertChain()[0]);
	}
	
	@Test
	public void testAddingOneCertMultipleTimes() throws FileNotFoundException, IOException {
		TrustedConnectionManager emptyManager = null;
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			emptyManager = new TrustedConnectionManager(input, "trusted".toCharArray());
		}
		
		URL fburl = new URL("https://www.facebook.com");
		// now we add root certificate to connection manager
		CertStoreResult result = emptyManager.addRootCertificate(fburl, "facebook");
		assertEquals("One certificate should be added", 1, result.getCertificatesAdded());
		assertNull("Cetificat was untrusted before", result.getMatchingCertificate());
		
		// once again try add same certificate
		result = emptyManager.addRootCertificate(fburl, "fb2");
		// handshake process occurs because ssl context is reloaded so session sharing is not applied here
		
		// because default behavior is doesn't add if exist
		assertEquals("One certificate should be added even if exists", 0, result.getCertificatesAdded());
		assertNotNull("Match in trustore was found", result.getMatchingCertificate());
		assertEquals("Matched shoudl be root certificate", result.getMatchingCertificate(), 
				result.getServerCertChain()[result.getServerCertChain().length - 1]);
	}
	
	
	@Test
	public void testAddingCertWithExistingAlias() throws FileNotFoundException, IOException {
		TrustedConnectionManager emptyManager = null;
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			emptyManager = new TrustedConnectionManager(input, "trusted".toCharArray());
		}
		
		URL fburl = new URL("https://www.facebook.com");
		// now we add root certificate to connection manager
		CertStoreResult result = emptyManager.addRootCertificate(fburl, "facebook");
		assertEquals("One certificate should be added", 1, result.getCertificatesAdded());
		assertNull("Cetificat was untrusted before", result.getMatchingCertificate());
		
		URL oraUrl = new URL("https://docs.oracle.com/en/");
		// lets add certificate with already used alias
		result = emptyManager.addRootCertificate(oraUrl, "facebook");
		
		// because alias points to trusted certificate, it is overridden so connection to previous one 
		// should fail
		try {
			HttpsURLConnection fbConn = emptyManager.getConnection(fburl);
			checkReadabilityFromConnection(fbConn);
			fail("SSLHandshakeException expected, because alias entry was overridden");
		} catch (SSLHandshakeException e) {}
	}
}
