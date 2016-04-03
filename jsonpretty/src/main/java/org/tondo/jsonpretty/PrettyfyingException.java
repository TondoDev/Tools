package org.tondo.jsonpretty;

/**
 * @author TondoDev
 * 
 * Runtime exception class for wrapping checked exception.
 *
 */
public class PrettyfyingException extends RuntimeException {

	public PrettyfyingException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PrettyfyingException(String message) {
		super(message);
	}
}
