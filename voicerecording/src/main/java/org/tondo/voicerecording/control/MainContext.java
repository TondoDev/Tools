package org.tondo.voicerecording.control;

import org.tondo.voicerecording.Voicerecording;
import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;

public class MainContext {
	
	private AdfFile currentAdfFile;
	private AdfEntry lastShown;
	
	public AdfFile createNewAdfFile () {
		AdfHeader header = new AdfHeader();
		header.setSrcLoc("SK");
		header.setDestLoc("DE");
		header.setAudioFormat(Voicerecording.getAudioFormat());
		this.currentAdfFile = new AdfFile(header);
		return this.currentAdfFile;
	}
	
	
	public AdfEntry getLastShownEntry() {
		return this.lastShown;
	}
	
	public void setLastShownEntry(AdfEntry entry) {
		this.lastShown = entry;
	}
}
