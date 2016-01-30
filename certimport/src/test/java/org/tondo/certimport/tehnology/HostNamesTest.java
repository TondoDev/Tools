package org.tondo.certimport.tehnology;

import static org.junit.Assert.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Test;

/**
 * Experiments how URL and ssl sockets can handle parsing hostanmes
 * 
 * @author TondoDev
 *
 */
public class HostNamesTest {

	@SuppressWarnings("unused")
	@Test
	public void testHostnamesFromURL() throws MalformedURLException {
		
		try {
			new URL("www.faceboo.com");
			fail("MalformedURLException expected");
		} catch (MalformedURLException e) {}
		
		// https
		URL fbUrl = new URL("https://www.facebook.com/");
		assertEquals("Host", "www.facebook.com", fbUrl.getHost());
		assertEquals("cant recognize default protocol https port", -1, fbUrl.getPort());
		
		
		// http
		URL pokecUrl = new URL("http://www.gugu.com/hi/how/are?you()");
		assertEquals("Host", "www.gugu.com", pokecUrl.getHost());
		assertEquals("cant recognize default protocol port", -1, pokecUrl.getPort());
		
		
		URL ipUrl = new URL("https://192.168.100.1:9090/index.html");
		assertEquals("Host", "192.168.100.1", ipUrl.getHost());
		assertEquals("explicit port", 9090, ipUrl.getPort());
		assertEquals("Protocol", "https", ipUrl.getProtocol());
	}
	
	
	@Test
	public void testIpAddressResolving() throws UnknownHostException {
		InetAddress address = Inet4Address.getByName("www.facebook.com");
		// DNS lookup occurs
		assertEquals("hostname", "www.facebook.com", address.getHostName());
		
		// not existint location
		// not sure why but can be created and some unknown address is returneds
		InetAddress invalid = Inet4Address.getByName("www.guwerergugop.sk");
		assertNotNull(invalid);
		//System.out.println(invalid);
		
		// from string ip
		InetAddress fromIp = Inet4Address.getByName("31.13.93.36");
		assertEquals("IP is valid", "31.13.93.36", fromIp.getHostAddress());
		// lookup occured
		assertNotEquals("Host is not empty", "", fromIp.getHostName());
		
		try {
			InetAddress.getByName("548.154.48.8");
			fail("UnknownHostException expected!");
		} catch (UnknownHostException e) {}
	}
}

