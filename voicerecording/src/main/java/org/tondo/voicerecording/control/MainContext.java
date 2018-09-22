package org.tondo.voicerecording.control;

import java.nio.file.Path;

import org.tondo.voicerecording.AppSettings;
import org.tondo.voicerecording.Voicerecording;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;

/**
 * 
 * @author TondoDev
 *
 */
public class MainContext {
	
	private AdfFile currentAdfFile;
	private EditingState editState;
	private Path fileLocation;
	private AppSettings settings;
	
	public AdfFile createNewAdfFile () {
		AdfHeader header = new AdfHeader();
		header.setSrcLoc("SK");
		header.setDestLoc("DE");
		header.setAudioFormat(Voicerecording.getAudioFormat());
		return createNewAdfFile(header);
	}
	
	public AdfFile createNewAdfFile(AdfHeader header) {
		this.currentAdfFile = new AdfFile(header);
		return this.currentAdfFile;
	}
	
	public EditingState getEditState() {
		return editState;
	}
	
	public void setEditState(EditingState editState) {
		this.editState = editState;
	}
	
	public AdfFile getAdfFile() {
		return this.currentAdfFile;
	}
	
	public void setAdfFile(AdfFile adfFile) {
		this.currentAdfFile = adfFile;
	}
	
	public void setFileLocation(Path location) {
		this.fileLocation = location;
	}
	
	public Path getFileLocation() {
		return this.fileLocation;
	}
	
	public AppSettings getSettings() {
		return settings;
	}
	
	public void setSettings(AppSettings settings) {
		this.settings = settings;
	}
}
