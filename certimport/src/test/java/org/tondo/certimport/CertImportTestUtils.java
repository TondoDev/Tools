package org.tondo.certimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

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
}
