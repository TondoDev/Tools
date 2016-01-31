package org.tondo.certimport;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Naive implementation of extracting host and port from URL. 
 * @author TondoDev
 *
 */
public class HostParser {
	
	private static Map<String, Integer> protocolToPortMap = null;
	static {
		protocolToPortMap = new HashMap<>();
		protocolToPortMap.put("HTTPS", 443);
		protocolToPortMap.put("HTTP", 80);
	}

	/**
	 * Port number is taken from explicit definition in URL or use default one from
	 * specified protocol. Explicit definition overrides protocol defaults. <br />
	 * 
	 * Host can be returned as string host name, or string representation of IP address exactli
	 * as it was provided in URL
	 * 
	 * @param location
	 * 	location of resource from where host infomation is parsed
	 * @return
	 * 	structure with filled host and port, both of these have valid values (port range, host is not null).
	 */
	public HostResult parserHost(URL location) {
		String host = location.getHost();
		checkHostName(host);
		int port = location.getPort();
		
		// not provided explicitly
		if (port == -1) {
			Integer tmpPort = protocolToPortMap.get(location.getProtocol().toUpperCase());
			if (tmpPort == null) {
				throw new HostParserException("Port can't be determined for URL");
			}
			port = tmpPort;
		}
		
		return new HostResult(host, port);
	}
	
	/**
	 * Check if hostname looks like IP address and then validate number ranges.
	 * In RFC is something written about that Top Level Domain must be alphabetic, but this algo
	 * so is much simplified. If hostname contains four gorups of digits separated by dot, then is considered
	 * as IP address and check against range.
	 * 
	 * @param hostName
	 * 	hostname to check
	 */
	private void checkHostName(String hostName) {
		String[] parts = hostName.split("\\.");
		
		if (parts.length != 4) {
			return;
		}
		
		int[] ipParts = new int[4];
		for (int i = 0; i < parts.length; i++) {
			try {
				ipParts[i] = Integer.parseInt(parts[i]);
			} catch (NumberFormatException e) {
				// not number -> not IP -> valid
				return;
			}
		}
		
		for (int i = 0; i < parts.length; i++) {
			if(ipParts[i] < 0 || ipParts[i] > 255) {
				throw new HostParserException(hostName + " is considered as IP address with bad format!");
			}
		}
	}
}
