package org.tondo.certimport;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import static org.junit.Assert.*;

import org.junit.Test;
import org.tondo.certimport.handlers.DnAliasCreator;

public class DnAliasCreatorTest {
	
	private static class PrincipalForParsing implements Principal {
		private String entryString;
		public  PrincipalForParsing(String entry) {
			this.entryString = entry;
		}

		@Override
		public String getName() {
			return this.entryString;
		}
		
	}

	@Test
	public void testParsingEntry() {
		String input = "CN=*.facebook.com, O=\"Facebook, Inc.\", L=Menlo Park, ST=CA, C=US";
		String input2 = "CN=DigiCert SHA2 High Assurance Server CA, OU=www.digicert.com, O=DigiCert Inc, C=US";
		
		DnAliasCreator creator = new DnAliasCreator();
		Map<String, String> result1 = creator.parseCertificateEntry(new PrincipalForParsing(input));
		Set<String> expectedKeys1 = new HashSet<>(Arrays.asList("CN", "O", "L", "ST", "C"));
		assertEquals("Keys matching", expectedKeys1, result1.keySet());
		assertEquals("CN", result1.get("CN"), "*.facebook.com");
		assertEquals("O", result1.get("O"), "Facebook, Inc.");
		assertEquals("L", result1.get("L"), "Menlo Park");
		assertEquals("ST", result1.get("ST"), "CA");
		assertEquals("C", result1.get("C"), "US");
		
		Map<String, String> result2 = creator.parseCertificateEntry(new PrincipalForParsing(input2));
		Set<String> expectedKeys2 = new HashSet<>(Arrays.asList("CN", "OU", "O", "C"));
		assertEquals("Keys matching", expectedKeys2, result2.keySet());
		assertEquals("CN", result2.get("CN"), "DigiCert SHA2 High Assurance Server CA");
		assertEquals("OU", result2.get("OU"), "www.digicert.com");
		assertEquals("O", result2.get("O"), "DigiCert Inc");
		assertEquals("C", result2.get("C"), "US");
	}
	
	@Test
	public void testParsingBadInput() {
		// missing value of last entry
		String input = "CN=*.facebook.com, O=\"Facebook, Inc.\", L=Menlo Park, ST=CA, C=";
		try {
			new DnAliasCreator().parseCertificateEntry(new PrincipalForParsing(input));
			fail("CertimportException expected");
		} catch (CertimportException e) {}
		
		// missing equal sign after key
		String input2 = "CN=*.facebook.com, O=\"Facebook, Inc.\", L=Menlo Park, ST";
		try {
			new DnAliasCreator().parseCertificateEntry(new PrincipalForParsing(input2));
			fail("CertimportException expected");
		} catch (CertimportException e) {}
		
		// missing terminating quotation marks
		String input3 = "CN=*.facebook.com, O=\"Facebook, Inc.";
		try {
			new DnAliasCreator().parseCertificateEntry(new PrincipalForParsing(input3));
			fail("CertimportException expected");
		} catch (CertimportException e) {}
	}
	
}
