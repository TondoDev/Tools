package org.tondo.voicerecording;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;
import org.tondo.voicerecording.adf.io.AdfWriter;

// FFMPEG: cat slovicko.raw | ./ffmpeg.exe -f s16be -ar 22.1K -ac 2 -i - -f mp3 - > pes.mp3

public class Voicerecording {

	// record duration, in milliseconds
	static final long RECORD_TIME = 5000;
	
	private static AudioFormat FORMAT;
	
	/**
	 * Defines an audio format
	 */
	public static AudioFormat getAudioFormat() {
		if (FORMAT == null) {
			float sampleRate = 44200;
			int sampleSizeInBits = 16;
			int channels = 2;
			boolean signed = true;
			boolean bigEndian = true;
			FORMAT = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		}
		
		return FORMAT;
	}
	
	
	// path of the wav file
	File wavFile = new File("outputs/RecordAudio.wav");

	// format of audio file
	//AudioFileFormat.Type fileType = AudioFileFormat.Type.AIFC;

	// the line from which audio data is captured
	TargetDataLine line;
	
	private ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	
	public Voicerecording() {
		this.init();
	}

	
	public void init() {
		this.outputBuffer = new ByteArrayOutputStream();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, getAudioFormat());

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

	/**
	 * Captures the sound and record into a WAV file
	 */
	void start() {
		
		if (this.line == null) {
			throw new IllegalStateException("Line not initialized!");
		}
		
		try {
			line.open(getAudioFormat());
			line.start(); // start capturing

			System.out.println("Start capturing...");

			AudioInputStream ais = new AudioInputStream(line);


			System.out.println("Start recording...");
			this.outputBuffer.reset();
			byte[] buffer = new byte[32000];
			int dataLen;
			while ((dataLen = ais.read(buffer)) > 0) {
				System.out.println("written: " + dataLen);
				this.outputBuffer.write(buffer, 0, dataLen);
			}

			// start recording
			//AudioSystem.write(ais, fileType, wavFile);

		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public byte[] getData() {
		if (this.outputBuffer == null) {
			return null;
		}
		
		return this.outputBuffer.toByteArray();
	}

	/**
	 * Closes the target data line to finish capturing and recording
	 */
	void finish() {
		line.stop();
		line.close();
		System.out.println("Finished");
	}

	public static void main(String... args) throws IOException, InterruptedException {
		BufferedReader enterWait = new BufferedReader(new InputStreamReader(System.in));
		
		Voicerecording recorder = new Voicerecording();
		
		System.out.println("Press enter to begin...");
		enterWait.readLine();
		Thread.sleep(200);
		
		AdfEntry entry = new AdfEntry();
		
		
		
		System.out.println("Source word!");
		System.out.print("Write: ");
		String firstWord = enterWait.readLine();
		entry.setSrcWord(firstWord);
		readlineRecording(recorder, enterWait);
		entry.setSrcSoundRaw(recorder.getData());
				
		System.out.println("Press enter to begin secodn word...");
		enterWait.readLine();
		
		System.out.print("Write: ");
		String secondWord = enterWait.readLine();
		entry.setDestWord(secondWord);
		readlineRecording(recorder, enterWait);
		entry.setDestSoundRaw(recorder.getData());
		
		AdfHeader header = new AdfHeader();
		header.setAudioFormat(getAudioFormat());
		header.setSrcLoc("SK");
		header.setDestLoc("DE");
		header.setAudioFormatType((short) 0);
		header.setTextEncoding((short) 0);
		
		AdfFile adf = new AdfFile(header);
		adf.getEntries().add(entry);
		
		AdfWriter writer = new AdfWriter();
		
		try (FileOutputStream fos = new FileOutputStream("outputs/slovnik.adf")) {
			writer.write(fos, adf);
		}
		
		//recorder.save("outputs/superfile.wav");
	}
	
	public void save(String fileName) {
		
		AudioFormat format = getAudioFormat();
		try ( AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(this.outputBuffer.toByteArray()), format, this.outputBuffer.size()/format.getFrameSize())) {
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error during save", e);
		}
	}
	
	private static void saveInFile() throws IOException {
		try (FileInputStream fis = new FileInputStream("outputs/binarka")) {
			AudioInputStream ais = new AudioInputStream(fis, getAudioFormat(), 200000L);
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("outputs/slovicko.wav"));
			ais.close();
		}
	}
	
	
	
	
	private static void threadedRecording() {
		final Voicerecording recorder = new Voicerecording();

		// creates a new thread that waits for a specified
		// of time before stopping
		Thread stopper = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(RECORD_TIME);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				recorder.finish();
			}
		});

		stopper.start();
		// start recording
		recorder.start();
	}
	
	private static void readlineRecording() {
		final Voicerecording recorder = new Voicerecording();

		// creates a new thread that waits for a specified
		// of time before stopping
		Thread stopper = new Thread(new Runnable() {
			public void run() {
				try {
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
				recorder.finish();
			}
		});

		stopper.start();
		System.out.println("Something to stop...");
		// start recording
		recorder.start();
	}
	
	private static void readlineRecording(Voicerecording recorder, BufferedReader reader) {
		Thread stopper = new Thread(new Runnable() {
			public void run() {
				try {
					reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				recorder.finish();
			}
		});

		stopper.start();
		System.out.println("Something to stop...");
		// start recording
		recorder.start();
	}
	
	
	public void generateSilence (int milis) throws IOException {
		AudioFormat format = getAudioFormat();
		double multiplier = milis/1000.0;
		int size = (int)(format.getFrameSize() * format.getFrameRate() * multiplier);
		this.outputBuffer.write(new byte[size]);
	}
}
