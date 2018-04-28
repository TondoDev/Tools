package org.tondo.voicerecording.adf.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;

public class AdfWriter {
	

	
	public void write(OutputStream out, AdfFile data) throws IOException {
		AdfHeader header = data.getHeader();
		
		// MAGIC SIGNATURE
		out.write(toBytes("ADF"));
		
		// source localization
		out.write(toBytes(header.getSrcLoc()));
		
		// destination localization
		out.write(toBytes(header.getDestLoc()));
		
		// 0
		out.write(header.getAudioFormatType());
		
		// 0
		out.write(header.getTextEncoding());
		
		// audio format
		AudioFormat format = header.getAudioFormat();
		DataOutputStream dataOS = new DataOutputStream(out);
		dataOS.writeFloat(format.getSampleRate());
		
		out.write(format.getSampleSizeInBits());
		
		out.write(format.getChannels());
		
		out.write(createFlagByte(format));
		
		for (AdfEntry entry : data.getEntries()) {
			writeEntry(entry, dataOS);
		}
	}
	
	
	private void writeEntry(AdfEntry entry, DataOutputStream out) throws IOException {
		writeWord(out,  entry.getSrcWord());
		byte[] srcData = entry.getSrcSoundRaw();
		if ( srcData != null) {
			out.writeInt(srcData.length);
			out.write(srcData);
		} else {
			out.write(0);
		}
		
		writeWord(out,  entry.getDestWord());
		byte[] destData = entry.getDestSoundRaw();
		if (destData != null) {
			out.writeInt(destData.length);
			out.write(destData);
		} else {
			out.write(0);
		}
	}


	private void writeWord(OutputStream out, String word) throws IOException {
		if (word != null && !word.isEmpty()) {
			byte[] srcData = null;
			srcData = toBytes(word);
			int len = srcData.length > 255 ? 255 : srcData.length;
			out.write(len);
			out.write(srcData, 0, len);
		} else {
			out.write(0);
		}
	}
	
	private int createFlagByte(AudioFormat format) {
		
		int flags = 0;
		if (format.isBigEndian()) flags = flags | 1;
		
		int encodingMask = 0;
		if (Encoding.PCM_SIGNED.equals(format.getEncoding())) encodingMask = (1 << 1);
		else if (Encoding.PCM_UNSIGNED.equals(format.getEncoding())) encodingMask = (1 << 2);
		else if (Encoding.PCM_FLOAT.equals(format.getEncoding())) encodingMask = (1 << 3);
		else if (Encoding.ALAW.equals(format.getEncoding())) encodingMask = (1 << 4);
		else if (Encoding.ULAW.equals(format.getEncoding())) encodingMask = (1 << 5);
		
		return flags | encodingMask;
	}
	
	
	private static byte[] toBytes(String str) {
		if(str == null) {
			return null;
		}
		return str.getBytes(Charset.forName("UTF-8"));
	}
}
