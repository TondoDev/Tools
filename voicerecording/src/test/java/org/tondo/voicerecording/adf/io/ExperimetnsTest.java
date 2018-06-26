package org.tondo.voicerecording.adf.io;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;
import org.tondo.voicerecording.Voicerecording;
import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.audio.AdfStreamer;
import org.tondo.voicerecording.audio.FfmpegMp3Convertor;

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
	
	
	@Test
	public void testFfmpeg() {
		AdfStreamer streamer = new AdfStreamer(Voicerecording.getAudioFormat());
    	streamer.start()
    		//.silence(200)
    		.destination()
    		.silence(500)
    		.source()
    		.silence(500)
    		.destination()
    		//.silence(200)
    	.setEntries(Arrays.asList(new AdfEntry()));
    	
    	FfmpegMp3Convertor convertor = new FfmpegMp3Convertor("bin/ffmpeg/bin/ffmpeg.exe");
    	convertor.convert(streamer, "outputs/ulozene.mp3");
	}
	
	@Test
	public void testPRocess() throws InterruptedException, IOException {
		ProcessBuilder pb = new ProcessBuilder("bin/ffmpeg/bin/ffmpeg.exe", "-y", "-f", "s16be", "-ac", "2", "-ar", "22100", "-i", "outputs/slovicko.raw", "-f", "mp3", "outputs/ulozene.mp3");

		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		
		pb.start().waitFor();
		
		
		
	}
}
