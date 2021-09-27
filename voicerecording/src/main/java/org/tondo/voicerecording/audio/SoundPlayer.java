package org.tondo.voicerecording.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundPlayer {
	
	private AudioFormat audioFormat;
	private Clip clip;

	private volatile boolean playing;
	
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
			
			playing = true;
			byte[] buff = new byte[dataFormat.getFrameSize() * 1000];
			int len;
			while ((len = streamer.stream(buff)) > -1 && this.playing) {
				output.write(buff, 0, len);
			}

			output.drain();
			output.stop();
		}
	}
	
	// dont case about thread safety
	public void stopPlayback() {
		this.playing = false;
	}
	
	public boolean isActive() {
		return this.clip != null && this.clip.isActive();
	}
	
}
