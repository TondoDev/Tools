package org.tondo.voicerecording.adf.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.tondo.voicerecording.adf.AdfEntry;
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
		Charset wordsEncoding = determineStringEncoding(encodingCode);
		if (wordsEncoding == null) {
			throw new IllegalStateException("Unsuported text encoding");
		}
		header.setTextEncoding((short) encodingCode);
		
		// source localization
		byte[] srcLoc = new byte[3];
		int readLen = input.read(srcLoc);
		if (readLen != 3) {
			throw new IllegalStateException("Malformed localization");
		}
		header.setSrcLoc(determineLocalization(srcLoc));
		
		// target localization
		byte[] destLoc = new byte[3];
		readLen = input.read(destLoc);
		if (readLen != 3) {
			throw new IllegalStateException("Malformed localization");
		}
		header.setDestLoc(determineLocalization(destLoc));
		
		AudioFormat format = readAudiFormat(input);
		if (format == null) {
			throw new IllegalArgumentException("Malfomed audio format structure");
		}
		header.setAudioFormat(format);
		
		AdfEntry entry = new AdfEntry();
		int readEntryStatus;
		while ((readEntryStatus = readEntry(input, entry, wordsEncoding)) > 0) {
			result.getEntries().add(entry);
			entry = new AdfEntry();
		}
		
		if (readEntryStatus < 0) {
			throw new IllegalStateException("Maformed word entries");
		}
		
		return result;
	}


	private AudioFormat readAudiFormat(InputStream input) throws IOException {
		DataInputStream dataInput = new DataInputStream(input);
		
		float sampleRate;
		try {
			 sampleRate = dataInput.readFloat();
		} catch(EOFException e) { return null; }
		
		int sampleSizeInBits = input.read();
		int channels = input.read();
		int flagByte = input.read();
		
		if (sampleSizeInBits == -1 || channels == -1 || flagByte == -1) {
			return null;
		}
		
		boolean isBigEndian = (flagByte & 1) > 0;
		Encoding audioEncoding = determineAudioEncoding(flagByte);
		if (audioEncoding == null) {
			return null;
		}
		
		AudioFormat format = new AudioFormat(
					audioEncoding, 
					sampleRate, 
					sampleSizeInBits, 
					channels, ((sampleSizeInBits + 7) / 8) * channels, 
					sampleRate, 
					isBigEndian);
		return format;
	}
	
	
	private boolean isAdf(InputStream input) throws IOException {
		
		byte[] buff = new byte[3];
		input.read(buff, 0, 3);
		
		String magic = new String(buff, Charset.forName("UTF-8"));
		return "ADF".equals(magic);
	}
	
	private Charset determineStringEncoding(int encodingCode) {
		if (encodingCode != 0) {
			return null;
		}
		
		return StandardCharsets.UTF_8;
	}
	
	private String determineLocalization(byte[] raw) {
		int i = 0;
		while (i < raw.length && raw[i] != 0) {
			i++;
		}
		
		return new String(Arrays.copyOfRange(raw, 0, i), StandardCharsets.US_ASCII);
	}
	
	
	private Encoding determineAudioEncoding(int flagByte) {
		if ((flagByte & 2) > 0) return Encoding.PCM_SIGNED;
		if ((flagByte & 4) > 0) return Encoding.PCM_UNSIGNED;
		if ((flagByte & 8) > 0) return Encoding.PCM_FLOAT;
		if ((flagByte & 16) > 0) return Encoding.ALAW;
		if ((flagByte & 32) > 0) return Encoding.ULAW;
		
		return null;
	}
	
	// 0 - EOF
	// 1 - OK
	// -1 - ERROR
	private int readEntry(InputStream input, AdfEntry entry, Charset charset) throws IOException {
		int srcStrSize = input.read();
		// no other entry exists so this is EOF for us
		if (srcStrSize < 0) {
			return 0;
		} else if(srcStrSize == 0) {
			entry.setSrcWord("");
		} else {
			byte[] srcText = new byte[srcStrSize];
			if (input.read(srcText) != srcStrSize) {
				return -1;
			}
			entry.setSrcWord(new String(srcText, charset));
		}
		
		DataInputStream dataInput = new DataInputStream(input);
		byte[] srcRaw = readRawAudio(dataInput);
		if (srcRaw == null) {
			return -1;
		}
		entry.setSrcSoundRaw(srcRaw);
		
		
		int destStrSize = input.read();
		if (destStrSize < 0) {
			return -1;
		} else if (destStrSize == 0) {
			entry.setSrcWord(""); 
		} else {
			byte[] destText = new byte[destStrSize];
			if (input.read(destText) != destStrSize) {
				return -1;
			}
			entry.setDestWord(new String(destText, charset));
		}
		
		byte[] destRaw = readRawAudio(dataInput);
		if (destRaw == null) {
			return -1;
		}
		entry.setDestSoundRaw(destRaw);
		
		
		return 1;
	}
	
	/**
	 * 
	 * @param dataInput
	 * @return a read byte array with size marked before a actual data. null in case of EOF
	 * @throws IOException
	 */
	private byte[] readRawAudio(DataInputStream dataInput) throws IOException {
		int size;
		try {
			size = dataInput.readInt();
		} catch (EOFException e) {
			return null;
		}
		// quick exit
		if (size == 0) {
			return new byte[]{};
		}
		
		byte[] buff = new byte[size];
		
		try {
			dataInput.readFully(buff);
		} catch (EOFException e) {
			return null;
		}
		
		return buff;
	}
}
