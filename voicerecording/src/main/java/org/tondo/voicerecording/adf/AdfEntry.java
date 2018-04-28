package org.tondo.voicerecording.adf;

public class AdfEntry {
	private String srcWord;
	private String destWord;
	
	private byte[] srcSoundRaw;
	private byte[] destSoundRaw;
	
	
	public String getSrcWord() {
		return srcWord;
	}
	
	public void setSrcWord(String srcWord) {
		this.srcWord = srcWord;
	}
	
	public String getDestWord() {
		return destWord;
	}
	
	public void setDestWord(String destWord) {
		this.destWord = destWord;
	}
	
	public byte[] getSrcSoundRaw() {
		return srcSoundRaw;
	}
	
	public void setSrcSoundRaw(byte[] srcSoundRaw) {
		this.srcSoundRaw = srcSoundRaw;
	}
	
	public byte[] getDestSoundRaw() {
		return destSoundRaw;
	}
	
	public void setDestSoundRaw(byte[] destSoundRaw) {
		this.destSoundRaw = destSoundRaw;
	}
}