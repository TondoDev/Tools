package org.tondo.certimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Commonly used code in tests
 * 
 * @author TondoDev
 *
 */
public class CertImportTestUtils {

	/**
	 * Reads data from  opened connection to ensure if connection is properly opened
	 * 
	 * @throws IOException
	 */
	public static void checkReadabilityFromConnection(URLConnection connection) throws IOException {
		byte[] buff = new byte[1024];
		// downloading 
		try (InputStream is = connection.getInputStream()) {
			while (is.read(buff) != -1) {}
		}
	}
	
	public static TrustManager[] getTrustManagers(File trustFile, String pwd) throws NoSuchAlgorithmException, KeyStoreException {
		KeyStore keyStore = KeyStore.getInstance("jks");
		try (FileInputStream fis = new FileInputStream(trustFile)) {
			keyStore.load(fis, pwd.toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException  e) {
			throw new CertimportException("Error during loading keystore", e);
		} 
		
		String defaultStrustAlgo  = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(defaultStrustAlgo);
		trustFactory.init(keyStore);
		return trustFactory.getTrustManagers();
	}
}
