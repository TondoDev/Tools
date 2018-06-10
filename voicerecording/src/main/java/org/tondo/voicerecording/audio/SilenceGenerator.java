package org.tondo.voicerecording.audio;

import javax.sound.sampled.AudioFormat;

public class SilenceGenerator {
	
	private AudioFormat format;
	
	public SilenceGenerator(AudioFormat format) {
		this.format = format;
	}

	public byte[] generateSilence (int milis)  {
		double multiplier = milis/1000.0;
		int size = (int)(format.getFrameSize() * format.getFrameRate() * multiplier);
		return new byte[size];
	}
}
