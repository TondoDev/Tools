package org.tondo.voicerecording.adf.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfReader {

	
	public AdfFile read(InputStream input) throws IOException {
		AdfHeader header = new AdfHeader();
		AdfFile result = new AdfFile(header);
		
		// signature
		if (!isAdf(input)) {
			throw new IllegalStateException("Not recognized ADF format!");
		}
		
		// audioFormat
		int audioFormatType = input.read();
		if (audioFormatType != 0) {
			throw new IllegalStateException("Unsuported audio type");
		}
		header.setAudioFormatType((short)audioFormatType);
		
		// text encoding
		int encodingCode = input.read();
		String encoding = determineEncoding(encodingCode);
		if (encoding == null) {
			throw new IllegalStateException("Unsuported text encoding");
		}
		header.setTextEncoding((short) encodingCode);
		
		// source localization
		byte[] srcLoc = new byte[3];
		int readLen = input.read(srcLoc);
		if (readLen != 3) {
			throw new IllegalStateException("Malformed localization");
		}
		header.setSrcLoc(determineLocalization(srcLoc, encoding));
		
		// target localization
		byte[] destLoc = new byte[3];
		readLen = input.read(srcLoc);
		if (readLen != 3) {
			throw new IllegalStateException("Malformed localization");
		}
		header.setDestLoc(determineLocalization(destLoc, encoding));
		
		
		
		
		return result;
	}
	
	
	private boolean isAdf(InputStream input) throws IOException {
		
		byte[] buff = new byte[3];
		input.read(buff, 0, 3);
		
		String magic = new String(buff, Charset.forName("UTF-8"));
		return "ADF".equals(magic);
	}
	
	private String determineEncoding(int encodingCode) {
		if (encodingCode != 0) {
			return null;
		}
		
		return "UTF-8";
	}
	
	private String determineLocalization(byte[] raw, String textEncoding) {
		return null;
	}
}
