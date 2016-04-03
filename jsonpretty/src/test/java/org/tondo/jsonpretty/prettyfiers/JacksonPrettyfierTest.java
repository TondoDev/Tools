package org.tondo.jsonpretty.prettyfiers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

public class JacksonPrettyfierTest {

	@Test
	public void testPrettyMap() {
		String inputData = "{\"meno\" : \"Tondo\",\"vek\" : 15}";
		// Expected output
//		[ {
//			  "meno" : "Tondo",
//			  "vek" : 15
//		} ]
		StringBuilder expected = new StringBuilder()
				.append("{").append("\r\n")
				.append("  \"meno\" : \"Tondo\",").append("\r\n")
				.append("  \"vek\" : 15").append("\r\n")
				.append("}"); // jackons appends user CRLF
		
		InputStream input = new ByteArrayInputStream(inputData.getBytes());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		JacksonMemmoryPrettyfier pp = new JacksonMemmoryPrettyfier();
		// configuration not important
		pp.prettyfy(input, output, null);
		String outData = new String(output.toByteArray());
		//debug(expected.toString(), outData);
		assertEquals(expected.toString(), outData);
	}
	
	private void debug(String expected, String actual) {
		int exLen = expected.length();
		int acLen = actual.length();
		
		System.err.println("Length expected: " + exLen);
		System.err.println("Length actual: " + acLen);
		
		int lowerLen = exLen > acLen ? acLen : exLen;
		int index = 0;
		for (; index < lowerLen; index++) {
			System.err.println("" + index + ". Expected: " + (int)expected.charAt(index) + "  Actual: " +  (int)actual.charAt(index));
		}
	}
} 
