package org.tondo.certimport;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.tondo.certimport.tehnology.HostNamesTest;

/**
 * Basic tests of HostParser to ensure its basic functionality.
 * For more info how URL names works {@link HostNamesTest}
 * @author TondoDev
 *
 */
public class HostParserTest {
	
	@Test
	public void testHostParsing() throws MalformedURLException {
		HostParser parser = new HostParser();
		HostResult fbDefaultResult = parser.parserHost(new URL("https://www.facebook.com"));
		assertNotNull("Host result cant't be null".equals(fbDefaultResult));
		assertEquals("Port number is standard https", 443, fbDefaultResult.getPort());
		assertEquals("host is without procotol", "www.facebook.com", fbDefaultResult.getHost());
				
		// port override
		HostResult fbOverridenPortResult = parser.parserHost(new URL("https://www.facebook.com:4585"));
		assertNotNull("Host result cant't be null".equals(fbOverridenPortResult));
		assertEquals("Port number is overriden", 4585, fbOverridenPortResult.getPort());
		assertEquals("host is without procotol", "www.facebook.com", fbOverridenPortResult.getHost());
		
		// some query
		HostResult fbWithQuery = parser.parserHost(new URL("https://www.facebook.com:4585/hallo/world/?nice.html"));
		assertNotNull("Host result cant't be null".equals(fbWithQuery));
		assertEquals("host is separated from path and query", "www.facebook.com", fbWithQuery.getHost());
		
		
		// http and IP as host
		HostResult ipResult = parser.parserHost(new URL("http://192.168.1.1/hallo/world/?nice.html"));
		assertNotNull("Host result cant't be null".equals(ipResult));
		assertEquals("host is extract as IP address", "192.168.1.1", ipResult.getHost());
		assertEquals("Default http port is used", 80, ipResult.getPort());
		
		// similar to IP but not iP is considered as valid hostname
		HostResult ipSimilarResult = parser.parserHost(new URL("http://192.168.10.1.1/hallo/world/?nice.html"));
		assertNotNull("Host result cant't be null".equals(ipSimilarResult));
		assertEquals("Parser cosiders this as valid hostname", "192.168.10.1.1", ipSimilarResult.getHost());
		assertEquals("Default http port is used", 80, ipSimilarResult.getPort());
		
		try {
			parser.parserHost(new URL("http://192.256.10.1/hallo/world/?nice.html"));
			fail("HostParserException expected because ip address component is out of range");
		} catch (HostParserException e) {}
 		
		//unknown protocol is handled by URL class
		try {
			parser.parserHost(new URL("bla://www.faceboo.com/"));
			fail("MalformedURLException expected");
		} catch (MalformedURLException e) {}
		
		// globally valid protocol, but not supported by this parser
		try {
			parser.parserHost(new URL("ftp://www.faceboo.com/"));
			fail("HostParserException expected");
		} catch (HostParserException e) {}
		
		
		// valid protocol, not supported by parser but explicit port
		HostResult unssuportedProtocolPort = parser.parserHost(new URL("ftp://www.faceboo.com:21/"));
		assertNotNull("Host result cant't be null".equals(unssuportedProtocolPort));
		assertEquals("Host is extracted correctly", "www.faceboo.com", unssuportedProtocolPort.getHost());
		assertEquals("Even protocol is unsupported by this parser port is provided explicitly", 21, unssuportedProtocolPort.getPort());
		
	}

}
