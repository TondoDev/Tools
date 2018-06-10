package org.tondo.voicerecording.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * 
 * @author TondoDev
 *
 */
public class SoundRecorder {
	
	private static class AudioReadJob implements Runnable {
		
		private ByteArrayOutputStream buffer;
		private AudioInputStream ais;
		
		
		public AudioReadJob(ByteArrayOutputStream buffer, AudioInputStream inputStream) {
			this.buffer = buffer;
			this.ais = inputStream;
		}
		

		@Override
		public void run() {
			System.out.println("Running");
			byte[] data = new byte[64000];
			int dataLen;
			try {
				while ((dataLen = ais.read(data)) > 0) {
					System.out.println("thread: " + dataLen);
					this.buffer.write(data, 0, dataLen);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private AudioFormat audioFormat;
	private TargetDataLine line;
	private Thread thread;
	private ByteArrayOutputStream byteBuffer;

	
	
	public SoundRecorder(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
		this.byteBuffer = new ByteArrayOutputStream();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.audioFormat);

		// checks if system supports the data line
		if (!AudioSystem.isLineSupported(info)) {
			throw new IllegalStateException("Line not supported");
		}
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new IllegalStateException("Line not supported", e);
		}
	}
	
	public void  start() {
		if (this.thread != null && this.thread.isAlive()) {
			System.err.println("Thread still running!");
			return;
		}
		
		this.byteBuffer.reset();
		try {
			this.line.open(this.audioFormat);
			this.line.start();
			AudioInputStream ais = new AudioInputStream(line);
			this.thread = new Thread(new AudioReadJob(byteBuffer, ais));
			thread.start();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() {
		this.line.stop();
		this.line.close();
		try {
			this.thread.join();
			System.out.println("joined");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getRecordedData() {
		if (this.thread == null || this.thread.isAlive()) {
			return null;
		}
		return this.byteBuffer.toByteArray();
	}

}
