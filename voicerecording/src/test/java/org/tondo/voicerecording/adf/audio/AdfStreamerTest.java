package org.tondo.voicerecording.adf.audio;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

import org.junit.Assert;
import org.junit.Test;
import org.tondo.voicerecording.Voicerecording;
import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;
import org.tondo.voicerecording.adf.io.AdfReader;
import org.tondo.voicerecording.audio.AdfStreamer;
import org.tondo.voicerecording.audio.AdfStreamer.Sequence;
import org.tondo.voicerecording.audio.SilenceGenerator;
import org.tondo.voicerecording.audio.SoundPlayer;

public class AdfStreamerTest {
	
	private static AudioFormat FORMAT = Voicerecording.getAudioFormat();
	
	private static SilenceGenerator SILENCE = new SilenceGenerator(FORMAT);
	
	@Test
	public void testEmptyAdf() {
		
		AdfEntry entry = new AdfEntry();
		entry.setDestSoundRaw(new byte[] {});
		
		final int BUFF_SIZE = 64;
		byte[] buff = new byte[BUFF_SIZE];
		
		AdfStreamer streamer = new AdfStreamer(FORMAT);
		
		// nothing from entry used
		streamer.initPlayback(null, Arrays.asList(entry));
		
		Assert.assertEquals("immediatelly end of stream", -1, streamer.stream(buff));
		Assert.assertArrayEquals("buffer remains unchanged", new byte[BUFF_SIZE], buff);
		
		
		buff = new byte[BUFF_SIZE];
		// user null soruce and empty destination entry
		Sequence sequence = AdfStreamer.createSequence()
			.source()
			.destination();
		streamer.initPlayback(sequence, Arrays.asList(entry));
		
		Assert.assertEquals("immediatelly end of stream", -1, streamer.stream(buff));
		Assert.assertArrayEquals("buffere remains unchanged", new byte[BUFF_SIZE], buff);
	}
	
	
	@Test
	public void testStreamSilenceOnly() {
		AdfEntry entry = new AdfEntry();
		AdfStreamer streamer = new AdfStreamer(FORMAT);
		final int BUFF_SIZE = 2000;
		byte[] buff = new byte[BUFF_SIZE];
		
		
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		try {
			expected.write(SILENCE.generateSilence(256));
			expected.write(SILENCE.generateSilence(1000));
		} catch (IOException e) {
			Assert.assertTrue("Something got wrong while generating silence", false);
		}
		
		Sequence seqBuilder = AdfStreamer.createSequence()
				.silence(256)
				.silence(1000);
		
		streamer.initPlayback(seqBuilder, Arrays.asList(entry));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		int len;
		while ((len = streamer.stream(buff)) > -1) {
			output.write(buff, 0, len);
		}

		Assert.assertArrayEquals(expected.toByteArray(), output.toByteArray());
	}
	
	
	@Test
	public void testSmallerBuffer() {
		byte[] src = new byte[2000];
		Arrays.fill(src, (byte)0xff);
		byte[] dest = new byte[1358];
		Arrays.fill(dest, (byte)0x55);
		
		AdfEntry entry = new AdfEntry();
		entry.setSrcSoundRaw(src);
		entry.setDestSoundRaw(dest);
		
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		
		try {
			// start silence
			expected.write(SILENCE.generateSilence(200));
			expected.write(src);
			expected.write(SILENCE.generateSilence(500));
			expected.write(dest);
			expected.write(dest);
		} catch (IOException e) {
			Assert.assertTrue("Something got wrong while generating sample data", false);
		}
		
		
		AdfStreamer streamer = new AdfStreamer(FORMAT);
		Sequence sequence = AdfStreamer.createSequence()
				.silence(200)
				.source()
				.silence(500)
				.destination()
				.destination();
		streamer.initPlayback(sequence, Arrays.asList(entry));
		
		final int BUFF_SIZE = 2000;
		byte[] buff = new byte[BUFF_SIZE];
		
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int len;
		while ((len = streamer.stream(buff)) > -1) {
			output.write(buff, 0, len);
		}
		
		Assert.assertArrayEquals(expected.toByteArray(), output.toByteArray());
	}
	
	@Test
	public void testBiggerBuffer() {
		byte[] src = new byte[10];
		Arrays.fill(src, (byte)0xff);
		byte[] dest = new byte[20];
		Arrays.fill(dest, (byte)0x55);
		
		AdfEntry entry = new AdfEntry();
		entry.setSrcSoundRaw(src);
		entry.setDestSoundRaw(dest);
		
		
		ByteArrayOutputStream expected = new ByteArrayOutputStream();
		
		try {
			expected.write(src);
			expected.write(dest);
		} catch (IOException e) {
			Assert.assertTrue("Something got wrong while generating sample data", false);
		}
		
		AdfStreamer streamer = new AdfStreamer(FORMAT);
		Sequence sequence = AdfStreamer.createSequence()
				.source()
				.destination();
		streamer.initPlayback(sequence, Arrays.asList(entry));
		
		final int BUFF_SIZE = 2000;
		byte[] buff = new byte[BUFF_SIZE];
		int len = streamer.stream(buff);
		
		Assert.assertEquals("Everything is readed in one step", src.length + dest.length, len);
		Assert.assertArrayEquals("data content is OK", expected.toByteArray(), Arrays.copyOf(buff, len));
		Assert.assertEquals("Subsequent stream attemtp returns end of stream", -1, streamer.stream(buff));
	}
	
	
	@Test
	public void testPlaybackMoreEntries() throws LineUnavailableException, IOException, InterruptedException {
		AdfReader reader = new AdfReader();
		AdfFile file = null;
		try (InputStream stream = new FileInputStream("outputs/test.adf")) {
			file = reader.read(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Assert.assertNotNull("2rror reading file", file);
		AudioFormat loadedFormat =  file.getHeader().getAudioFormat();
		AdfStreamer streamer = new AdfStreamer(loadedFormat);
		// TODO read from GUI 
				Sequence sequence = AdfStreamer.createSequence()
						.destination()
						.silence(352)
						.source()
						.silence(350)
						.destination()
						.silence(400); // problem robi so 700?
		
//		Sequence sequence = AdfStreamer.createSequence()
//				.destination()
//				.silence(700)
//				.source();
		
				
		streamer.initPlayback(sequence, file.getEntries().stream().limit(1).collect(Collectors.toList()));
		SoundPlayer player = new SoundPlayer(loadedFormat);
		//player.play(streamer);
		player.play(streamer);
		System.out.println("OK");
		Thread.sleep(7000);
	}
	
	
	private void modifySilence(byte[] data) {
		
		int value = 0;
		int dir = 1;
		
		int len = data.length;
		for (int i = 0; i < len; i++) {
			if (value >= 127 || value <= -128) {
				dir *= -1;
			}
			
			value += dir;
			data[i] = (byte) value;
		}
		
	}
	

}
