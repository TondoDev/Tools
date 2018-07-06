package org.tondo.voicerecording.control;

import org.tondo.voicerecording.Voicerecording;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;

public class MainController {
	
	private AdfFile currentAdfFile;
	
	public AdfFile createNewAdfFile () {
		AdfHeader header = new AdfHeader();
		header.setSrcLoc("SK");
		header.setDestLoc("DE");
		header.setAudioFormat(Voicerecording.getAudioFormat());
		this.currentAdfFile = new AdfFile(header);
		return this.currentAdfFile;
	}
}
