package org.tondo.certimport;

import static org.junit.Assert.*;
import static org.tondo.certimport.CertImportTestUtils.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.junit.Test;
import org.tondo.certimport.handlers.CertStoreResult;
import org.tondo.certimport.handlers.DnAliasCreator;
import org.tondo.certimport.handlers.StoringConfiguration;
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
		

		result = emptyManager.addRootCertificate(fburl, "fb2");
		// because default behavior is add even if exist
		assertEquals("No certificate should be added because default setting is not overwrite", 0, result.getCertificatesAdded());
		assertNotNull("Match in trustore was found", result.getMatchingCertificate());
		assertEquals("Matching alias should be this we added in previous step", "facebook", result.getMatchingAlias());
		assertEquals("Matched shoudl be root certificate", result.getMatchingCertificate(), result.getServerCertChain()[result.getServerCertChain().length - 1]);
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
	
	@Test
	public void testAddLeafCertificate() throws FileNotFoundException, IOException {
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
		
		CertStoreResult result = emptyManager.addLeafCertificate(fburl, "facebook");
		assertEquals("One certificate should be added", 1, result.getCertificatesAdded());
		assertNull("Cetificat was untrusted before", result.getMatchingCertificate());
		assertNull("Cetificat was untrusted before so alias is also null", result.getMatchingAlias());
		
		// this should work
		fbConn = emptyManager.getConnection(fburl);
		checkReadabilityFromConnection(fbConn);
		
		// try adding again
		result = emptyManager.addLeafCertificate(fburl, "facebook");
		assertEquals("Default is not add even if exists so nothing is added", 0, result.getCertificatesAdded());
		assertNotNull("Found already matching certificate", result.getMatchingCertificate());
		// added used alias in previous step
		assertEquals("Found already matching certificate alias", "facebook", result.getMatchingAlias());
		// matched certificate should be first in server cert chain because we are adding leaf
		assertEquals("Matching certificate is leaf", result.getServerCertChain()[0], result.getMatchingCertificate());
		assertTrue("Contains facebook somewhere", result.getMatchingCertificate().getSubjectDN().getName().contains("facebook"));
		
	}
	
	@Test
	public void testChekIfTrusted() throws FileNotFoundException, IOException {
		TrustedConnectionManager emptyManager = null;
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			emptyManager = new TrustedConnectionManager(input, "trusted".toCharArray());
		}
		URL fburl = new URL("https://www.facebook.com");
		CertStoreResult result = emptyManager.checkIfTrusted(fburl);
		// not trusted so far
		assertNull("When not trusted alias is null", result.getMatchingAlias());
		assertNull("When not trusted matching certificate is null", result.getMatchingCertificate());
		assertEquals("Nothing was added", 0, result.getCertificatesAdded());
		
		// now we add certificate
		CertStoreResult storingResult = emptyManager.addLeafCertificate(fburl, "fbcert");
		assertEquals("One certificate was added", 1, storingResult.getCertificatesAdded());
		
		// no check if trusted again
		CertStoreResult matchingResult = emptyManager.checkIfTrusted(fburl);
		assertEquals("Nothing was added, its only predicate", 0, matchingResult.getCertificatesAdded());
		assertEquals("Found matched alias", "fbcert", matchingResult.getMatchingAlias());
		assertEquals("Match was on leaf certificate added in previous step", 
					storingResult.getServerCertChain()[0], 
					matchingResult.getMatchingCertificate());
	}
	
	@Test
	public void testStoreWholeChain() throws FileNotFoundException, IOException {
		TrustedConnectionManager emptyManager = null;
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			emptyManager = new TrustedConnectionManager(input, "trusted".toCharArray());
		}
		URL fburl = new URL("https://www.facebook.com");
		StoringConfiguration conf = StoringConfiguration.builder()
				.setOption(CertStoringOption.CHAIN)
			.create();
		
		assertTrue("Default alias creator is DN", conf.getAliasCreator() instanceof DnAliasCreator);
		assertEquals("Default store if trusted", false, conf.isAddEvenIfTrusted());
		
		CertStoreResult result = emptyManager.addCertificate(fburl, conf);
		assertEquals("Added certs should be equal to whole chain sent by server", result.getServerCertChain().length, result.getCertificatesAdded());
		
		CertStoreResult checkResult = emptyManager.checkIfTrusted(fburl);
		// checking is done from root to leaf?
		int chainLen = checkResult.getServerCertChain().length;
		assertEquals("Matching last in chain (root)", 
				checkResult.getMatchingCertificate(), 
				checkResult.getServerCertChain()[chainLen - 1]);
		
		DnAliasCreator aliasCreator = new DnAliasCreator();
		assertEquals("Alias was by default generated from DN", 
				aliasCreator.createAlias((X509Certificate)checkResult.getServerCertChain()[chainLen -1]),
				checkResult.getMatchingAlias());
	}
	
	/**
	 * Demo of initialization of TrustConnectionManager with null as InputStream parameter for 
	 * truststore
	 * @throws IOException 
	 */
	@Test
	public void testInitializedWithNull() throws IOException {
		TrustedConnectionManager manager = new TrustedConnectionManager(null, null);
		URL fburl = new URL("https://www.facebook.com");
		CertStoreResult checkResult = manager.checkIfTrusted(fburl);
		assertNull("empty trust store trust nothing", checkResult.getMatchingAlias());
		
		CertStoreResult addResult = manager.addRootCertificate(fburl, "facebook");
		assertEquals("One certificate should be added", 1, addResult.getCertificatesAdded());
		
		CertStoreResult recheckResult = manager.checkIfTrusted(fburl);
		assertEquals("facebook", recheckResult.getMatchingAlias());
	}
	
	@Test
	public void testSaveTrustStore() throws FileNotFoundException, IOException {
		TrustedConnectionManager manager = null;
		try (InputStream input = new FileInputStream(this.inResourceDir("trustempty").toString())) {
			manager = new TrustedConnectionManager(input, "trusted".toCharArray());
		}
		URL fburl = new URL("https://www.facebook.com");
		// fb is not trusted initially
		CertStoreResult checkResult = manager.checkIfTrusted(fburl);
		assertNull("empty trust store trust nothing", checkResult.getMatchingAlias());
		
		// add fb to trustore
		CertStoreResult addResult = manager.addRootCertificate(fburl, "facebook");
		assertEquals("One certificate should be added", 1, addResult.getCertificatesAdded());
		
		Path fbtrust = this.inTempDir("fbtrust.jks");
		// file will be deleted after this test
		getFileKeeper().markForWatch(fbtrust);
		try (FileOutputStream fos = new FileOutputStream(fbtrust.toFile())) {
			manager.save(fos, "shrek".toCharArray());
		}
		
		// so now we have new trustore with fb certificate so init
		// new trust connection manager with that new trustore and fb should be 
		// trusted
		TrustedConnectionManager fbManager = null;
		try (InputStream input = new FileInputStream(fbtrust.toFile())) {
			fbManager = new TrustedConnectionManager(input, "shrek".toCharArray());
		}
		
		CertStoreResult reloadedResult = fbManager.checkIfTrusted(fburl);
		assertEquals("From reloaded trustore fb should be trusted", "facebook", reloadedResult.getMatchingAlias());
		// keytool.exe -list -v -keystore fbtrust.jks
	}
	
	@Test
	public void testSaveEmptyTrustStore() throws FileNotFoundException, IOException {
		TrustedConnectionManager manager = new TrustedConnectionManager(null, null);		
		
		Path fbtrust = this.inTempDir("fbtrust.jks");
		// file will be deleted after this test
		getFileKeeper().markForWatch(fbtrust);
		try (FileOutputStream fos = new FileOutputStream(fbtrust.toFile())) {
			manager.save(fos, "shrek".toCharArray());
		}
		
		// keytool.exe -list -v -keystore fbtrust.jks
		// will reurns
		// Keystore type: JKS
		// Keystore provider: SUN
		//
		// Your keystore contains 0 entries
	}
	
	@Test
	public void testSaveWithNull() {
		TrustedConnectionManager manager = new TrustedConnectionManager(null, null);		
		try {
			manager.save(null, "shrek".toCharArray());
			fail("NullPointerException expected. Risen inside original KeyStore class");
		} catch (NullPointerException e) {}
		
	}
}

