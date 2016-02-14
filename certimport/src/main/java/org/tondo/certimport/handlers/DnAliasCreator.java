package org.tondo.certimport.handlers;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.tondo.certimport.CertimportException;

/**
 * 
 * Create certificate alias based on Distinguished name
 * By default maximum 20 characters are taken from certificate subject DN and no prefix is used.
 * 
 * @author TondoDev
 *
 */
public class DnAliasCreator implements AliasCreator {
	
	private static enum State {
		KEY, VAL, DELIM, STRING_VAL
	}
	
	private String prefix;
	private int maxChars;
	
	public DnAliasCreator(String prefix, Integer maxChars) {
		this.maxChars = maxChars == null ? 20 : maxChars.intValue();
		this.prefix = prefix == null ? "" : prefix;
	}
	
	public DnAliasCreator() {
		this(null, null);
	}

	@Override
	public String createAlias(X509Certificate certificate) {
		String alias = null;
		
		String[] examineKeyOrder = new String[] {"CN", "OU", "O"};
		
		Map<String, String> subjectEntries = parseCertificateEntry(certificate.getSubjectDN());
		for (String key : examineKeyOrder) {
			String value = subjectEntries.get(key);
			if (value != null) {
				alias = value.substring(0, value.length() < this.maxChars ? value.length() : this.maxChars);
				break;
			}
		}
	
		// aliases converted to lowercase by keystore
		return (this.prefix + alias).toLowerCase();
	}
	
	
	public Map<String, String> parseCertificateEntry(Principal entry) {
		Map<String, String> parsed = new HashMap<>();
		String name = entry.getName();
		int nameLen = name.length();
		
		StringBuilder keyBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();
		State s = State.KEY;
		for (int i =0; i < nameLen; i++) {
			char c = name.charAt(i);
			if (s == State.KEY) {
				if (c == '=') {
					s = State.DELIM;
				} else if (!Character.isWhitespace(c)) {
					keyBuilder.append(c);
				}
			} else if (s == State.DELIM) {
				if (c == '"') {
					s = State.STRING_VAL;
				} else {
					s = State.VAL;
					valueBuilder.append(c);
				}
			} else if (s == State.VAL) {
				if (c == ',') {
					parsed.put(keyBuilder.toString(), valueBuilder.toString());
					keyBuilder = new StringBuilder();
					valueBuilder = new StringBuilder();
					s = State.KEY;
				} else {
					valueBuilder.append(c);
				}
			} else if (s == State.STRING_VAL) {
				if (c == '"') {
					s = State.VAL;
				} else {
					valueBuilder.append(c);
				}
			}
		}
		
		if (s == State.VAL &&  valueBuilder.length() != 0) {
			parsed.put(keyBuilder.toString(), valueBuilder.toString());
		} else if (s != State.KEY || valueBuilder.length() != 0 || keyBuilder.length() != 0) {
			throw new CertimportException("Can't parse certificate data for alias construction!");
		}
		
		return parsed;
	}
}
