package org.tondo.voicerecording.adf.io;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

public class ExperimetnsTest {

	@Test
	public void testStringEncoding() {
		String english = "EN";
		
		byte[] asciiBytes = english.getBytes(StandardCharsets.US_ASCII);
		System.out.println("asciiBytes: " + Arrays.toString(asciiBytes));
		
		byte[] utf16Bytes = english.getBytes(StandardCharsets.UTF_16);
		System.out.println("utf16Bytes: " + Arrays.toString(utf16Bytes));
		
		byte[] utf8Bytes = english.getBytes(StandardCharsets.UTF_8);
		System.out.println("utf8Bytes: " + Arrays.toString(utf8Bytes));
		
		
		String slovak = "ÉŇ";
		
		byte[] skAsciiBytes = slovak.getBytes(StandardCharsets.US_ASCII);
		System.out.println("skAsciiBytes: " + Arrays.toString(skAsciiBytes));
		
		byte[] skAtf16Bytes = slovak.getBytes(StandardCharsets.UTF_16);
		System.out.println("skAtf16Bytes: " + Arrays.toString(skAtf16Bytes));
		
		byte[] skUtf8Bytes = slovak.getBytes(StandardCharsets.UTF_8);
		System.out.println("skUtf8Bytes: " + Arrays.toString(skUtf8Bytes));
	}
	
	@Test
	public void testConvertFromBytesToString() {
		byte[] full = new byte[] {69, 78};
		String strFull = new String(full, StandardCharsets.US_ASCII);
		System.out.println("" + strFull + " " + strFull.length());
		
		byte[] withSpace = new byte[] {69, 78, 0};
		String strSpace = new String(withSpace, StandardCharsets.US_ASCII);
		System.out.println("" + strSpace + " " + strSpace.length());
		
		
	}
}
