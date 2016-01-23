package org.tondo.certimport;

public class CertimportException extends RuntimeException{
	
	public CertimportException(String message) {
		super(message);
	}
	
	public CertimportException(String message, Throwable cause) {
		super(message,cause);
	}

}
