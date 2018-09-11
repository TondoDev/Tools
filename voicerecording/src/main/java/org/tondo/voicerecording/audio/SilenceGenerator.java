package org.tondo.voicerecording.audio;

import javax.sound.sampled.AudioFormat;

public class SilenceGenerator {
	
	private AudioFormat format;
	
	public SilenceGenerator(AudioFormat format) {
		this.format = format;
	}

	public byte[] generateSilence (int milis)  {
		double multiplier = milis/1000.0;
		final int FS = format.getFrameSize();
		int size = (int)(FS * format.getFrameRate() * multiplier);
		int reminder = size % FS;
		int alignment = reminder == 0 ? 0 : (FS - reminder);
		return new byte[size + alignment];
	}
}
