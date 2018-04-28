package org.tondo.voicerecording.adf;

import javax.sound.sampled.AudioFormat;

/**
 * Header for Audio Dictionary format
 * @author TondoDev
 *
 */
public class AdfHeader {

	private String srcLoc;
	private String destLoc;
	
	// 0 - WAV
	private short audioFormatType;
	
	// 0 - UTF-8
	private short textEncoding;
	
	private AudioFormat audioFormat;

	public String getSrcLoc() {
		return srcLoc;
	}

	public void setSrcLoc(String srcLoc) {
		this.srcLoc = srcLoc;
	}

	public String getDestLoc() {
		return destLoc;
	}

	public void setDestLoc(String destLoc) {
		this.destLoc = destLoc;
	}

	public short getAudioFormatType() {
		return audioFormatType;
	}

	public void setAudioFormatType(short audioFormatType) {
		this.audioFormatType = audioFormatType;
	}

	public short getTextEncoding() {
		return textEncoding;
	}

	public void setTextEncoding(short textEncoding) {
		this.textEncoding = textEncoding;
	}

	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	public void setAudioFormat(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
	}
	
}
