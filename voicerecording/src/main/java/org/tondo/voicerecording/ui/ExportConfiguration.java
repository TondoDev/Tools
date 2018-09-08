package org.tondo.voicerecording.ui;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.audio.AdfStreamer.Sequence;

/**
 * 
 * @author TondoDev
 *
 */
public class ExportConfiguration {
	
	private List<AdfEntry> entries;
	private Path outFile;
	private Sequence playSequence;
	
	
	public ExportConfiguration(List<AdfEntry> entries, Sequence sequence, Path outFile) {
		this.entries = Collections.unmodifiableList(entries);
		this.outFile = outFile;
		this.playSequence = sequence;
	}
	
	
	public List<AdfEntry> getEntries() {
		return entries;
	}
	
	public Path getOutFile() {
		return outFile;
	}
	
	public Sequence getPlaySequence() {
		return playSequence;
	}
}
