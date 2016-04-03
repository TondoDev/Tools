package org.tondo.jsonpretty.prettyfiers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.tondo.jsonpretty.PrettyfyingException;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author TondoDev
 * 
 * Naive prettyfier which reads all string into memmory, marse it into
 * object, and t hen serialize it back to formatted json string.
 * 
 * Simples impl. using jackson parsers
 *
 */
public class JacksonMemmoryPrettyfier implements Prettyfier {

	@Override
	public void prettyfy(InputStream is, OutputStream os, FormattingOptions options) {
		//TODO handle options(encoding, oneliner...)
		ObjectMapper mapper = new ObjectMapper();
		Object parsed = null;
		try {
			parsed = mapper.readValue(is, Object.class);
		} catch (IOException e) {
			throw new PrettyfyingException("Error during reading input json!", e);
		}
		
		try {
			mapper.writer(new DefaultPrettyPrinter()).writeValue(os, parsed);
		} catch (IOException e) {
			throw new PrettyfyingException("Error during writing formatted json!", e);
		}
	}
}
