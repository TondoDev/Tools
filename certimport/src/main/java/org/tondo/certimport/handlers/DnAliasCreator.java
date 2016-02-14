package org.tondo.certimport.handlers;

import java.security.cert.X509Certificate;

/**
 * 
 * Create certificate alias based on Distinguished name
 * By default maximum 10 characters are taken from certificate subject DN and no prefix is used.
 * 
 * @author TondoDev
 *
 */
public class DnAliasCreator implements AliasCreator{
	
	private String prefix;
	private int maxChars;
	
	public DnAliasCreator(String prefix, Integer maxChars) {
		this.maxChars = maxChars == null ? 10 : maxChars.intValue();
		this.prefix = prefix == null ? "" : prefix;
	}
	
	public DnAliasCreator() {
		this(null, null);
	}

	@Override
	public String createAlias(X509Certificate certificate) {
		return this.prefix + certificate.getSubjectDN().getName().substring(0, this.maxChars);
	}
}
