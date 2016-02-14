package org.tondo.certimport.handlers;

import java.security.cert.X509Certificate;

/**
 * 
 * Provides algorithm for generating alias for trustore entry
 * @author TondoDev
 *
 */
public interface AliasCreator {

	/**
	 * Creates alias string used in trustore entry for given certificate.
	 * Implementations can use X509Certificate fields for generating alias
	 * @param certificate
	 * 	certificate for which alais should be generated
	 * @return
	 * 	alias string for truststore
	 */
	public String createAlias(X509Certificate certificate);
}
