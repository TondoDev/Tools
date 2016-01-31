package org.tondo.certimport;


/**
 * 
 * @author TondoDev
 *
 */
public class HostResult {

	private int port;
	private String host;
	
	public HostResult(String h, int p) {
		this.port = p;
		this.host = h;
	}
	
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
}
