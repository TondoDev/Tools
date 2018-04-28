package org.tondo.voicerecording.adf;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfFile {

	private AdfHeader header;
	private List<AdfEntry> entries;
	
	
	public AdfFile(AdfHeader header) {
		this.header = header;
		this.entries = new ArrayList<>();
	}
	
	
	public List<AdfEntry> getEntries() {
		return entries;
	}
	
	public AdfHeader getHeader() {
		return header;
	}
}
