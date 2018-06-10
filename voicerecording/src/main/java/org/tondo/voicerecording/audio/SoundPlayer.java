package org.tondo.voicerecording.audio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundPlayer {
	
	private AudioFormat audioFormat;
	private Clip clip;

	public SoundPlayer(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
	}
	
	
	public void play(byte[] data) {
		try {
			this.clip = AudioSystem.getClip();
			this.clip.open(this.audioFormat, data, 0, data.length);
			this.clip.addLineListener(e -> {
				if (e.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
					this.clip.close();
				}
			});
			this.clip.start();
			System.out.println("==");
			this.clip.drain();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} 
	}

	// TODO AdfStremer by vobec nemal vedeiet o aky AudioFormat ide
	// Treba vyriesit, ako bude generovat ticho - napr ze SilenceGEn bude
	// predany ako parameter pri vytvoren9 builderu akcii
	public void play(AdfStreamer streamer) throws LineUnavailableException {
		AudioFormat dataFormat = streamer.getAudioFormat();
		try (SourceDataLine output = AudioSystem.getSourceDataLine(dataFormat)) {
			output.open(dataFormat);
			output.start();
			
			
			try (OutputStream file = new FileOutputStream("outputs/slovicko.raw")) {
				byte[] buff = new byte[dataFormat.getFrameSize() * 1000];
				int len;
				while ((len = streamer.stream(buff)) > -1) {
					output.write(buff, 0, len);
					file.write(buff, 0, len);
				}

				output.drain();
				output.stop();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	public boolean isActive() {
		return this.clip != null && this.clip.isActive();
	}
	
}
