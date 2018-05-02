package org.tondo.voicerecording.adf.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

import org.junit.After;
import org.junit.Test;
import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;

public class AdfReadWriteTest {
	
	private static final String FILE_NAME = "outputs/testfile.adf";
	
	@Test
	public void testWriteRead() throws IOException {
		AdfFile file = prepareAdfStructuree();
		AdfWriter writer = new AdfWriter();
		
		try (FileOutputStream fos = new FileOutputStream(FILE_NAME)) {
			writer.write(fos, file);
		}
	}
	

	@Test(expected = IllegalStateException.class)
	public void testNonsense() throws IOException {
		Files.write(Paths.get(FILE_NAME), genByteData(10, 0xff), StandardOpenOption.CREATE);
		
		AdfReader reader = new AdfReader();
		
		try (FileInputStream fis = new FileInputStream(FILE_NAME)) {
			AdfFile adf = reader.read(fis);
		}
	}
	
	@After
	public void cleanTestFile() {
		try {
			Files.deleteIfExists(Paths.get(FILE_NAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	protected AdfFile prepareAdfStructuree() {
		AdfHeader header = new AdfHeader();
		AudioFormat format = prepareFormat();
		
		header.setAudioFormat(format);
		header.setAudioFormatType((short)0);
		header.setTextEncoding((short)0);
		header.setSrcLoc("SK");
		header.setDestLoc("DE");
		
		AdfFile retval = new AdfFile(header);
		
		retval.getEntries().add(new AdfEntry("xxx", genByteData(10, 0xFF), "yyy", genByteData(15, 0xAA)));
		retval.getEntries().add(new AdfEntry("aaaaaa", genByteData(5, 0x11), "ooo", genByteData(1, 0xEE)));
		
		return retval;
	}
	
	protected AudioFormat prepareFormat() {
		float sampleRate = 8000;
		int sampleSizeInBits = 16;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}
	
	
	private static byte[] genByteData(int size, int value) {
		byte[] rv = new byte[size];
		Arrays.fill(rv, (byte)(value));
		
		return rv;
	}

}