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
}
