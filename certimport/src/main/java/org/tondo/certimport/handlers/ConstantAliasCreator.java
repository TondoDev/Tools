package org.tondo.certimport.handlers;

import java.security.cert.X509Certificate;

/**
 * Alias creator returning single constant provided into constructor
 * 
 * @author TondoDev
 *
 */
public class ConstantAliasCreator implements AliasCreator{
	
	private String alias;
	
	/***
	 * Create alias creator
	 * @param alias
	 * 	constant alias which will be returned by {@link #createAlias(X509Certificate)} call
	 */
	public ConstantAliasCreator(String alias) {
		
		if (alias == null) {
			throw new NullPointerException("Alias cant be null!");
		}
		
		this.alias = alias;
	}

	/**
	 * Returns constant alias string <br />
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String createAlias(X509Certificate certificate) {
		return this.alias;
	}
}
