package org.tondo.voicerecording.control;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.ui.DataArea;

public class DataAreaController {
	
	private DataArea dataArea;
	private AdfEntry adfContext;
	
	private byte[] srcSoundBuffer;
	private byte[] destSoundBuffer;
	
	private boolean editable;
	
	public DataAreaController(DataArea area) {
		this.dataArea = area;
	}
	
	
	public void setAdfContext(AdfEntry ctx) {
		this.adfContext = ctx;
		if (this.adfContext != null) {
			populateFromAdf(this.adfContext);
			setControlsDisabledState();
		}
	}
	
	
	public void setEditable(boolean editable) {
		if (this.editable != editable) {
			this.editable = editable;
			if (this.adfContext != null && this.editable) {
				this.initBuffers(this.adfContext);
			}
			setControlsDisabledState();
		}
	}
	
	
	protected void setControlsDisabledState() {
		this.dataArea.getSrcWord().setDisable(this.adfContext == null);
		this.dataArea.getDestWord().setDisable(this.adfContext == null);
		this.dataArea.getSrcWord().setEditable(this.editable);
		this.dataArea.getDestWord().setEditable(this.editable);
		
		this.dataArea.getSrcSoundRec().setDisable(!this.editable || this.adfContext == null);
		this.dataArea.getDestSoundRec().setDisable(!this.editable || this.adfContext == null);
		
		this.dataArea.getSrcSoundPlay().setDisable(this.srcSoundBuffer == null);
		this.dataArea.getSrcSoundRemove().setDisable(this.srcSoundBuffer == null || !this.editable);
		this.dataArea.getDestSoundPlay().setDisable(this.destSoundBuffer == null);
		this.dataArea.getDestSoundRemove().setDisable(this.destSoundBuffer == null || !this.editable);
		
		this.dataArea.getSpecialCharsHBox().setDisable(!this.editable || this.adfContext == null);
		
		this.dataArea.getOkButton().setDisable(!this.editable || this.adfContext == null);
		this.dataArea.getCancelButton().setDisable(!this.editable || this.adfContext == null);
	}
	
	protected void populateFromAdf(AdfEntry entry) {
		this.dataArea.getSrcWord().setText(entry.getSrcWord());
		this.dataArea.getDestWord().setText(entry.getDestWord());
		this.srcSoundBuffer = entry.getSrcSoundRaw();
		this.destSoundBuffer = entry.getDestSoundRaw();
	}
	
	protected void initBuffers(AdfEntry entry) {
		this.srcSoundBuffer = copyBuffer(entry.getSrcSoundRaw());
		this.destSoundBuffer = copyBuffer(entry.getDestSoundRaw());
	}
	
	private static byte[] copyBuffer(byte[] src) {
		byte[] retval;
		if (src == null || src.length == 0) {
			retval = null;
		} else {
			retval = new byte[src.length];
			System.arraycopy(src, 0, retval, 0, src.length);
		}
		
		return retval;
	}
	
	public void setLanguages(String srcLanguage, String destLanguage) {
		this.dataArea.getSrcLabel().setText(srcLanguage);
		this.dataArea.getDestlabel().setText(destLanguage);
	}
	
	
	public AdfEntry confirmChanges() {
		// populate adf entry from buffers and texts
		this.adfContext.setSrcWord(this.dataArea.getSrcWord().getText());
		this.adfContext.setDestWord(this.dataArea.getDestWord().getText());
		this.adfContext.setSrcSoundRaw(copyBuffer(this.srcSoundBuffer));
		this.adfContext.setDestSoundRaw(copyBuffer(this.destSoundBuffer));
		return this.adfContext;
	}
	
	public void discardChanges() {
		if (this.adfContext != null) {
			this.dataArea.getSrcWord().setText(this.adfContext.getSrcWord());
			this.dataArea.getDestWord().setText(this.adfContext.getDestWord());
			this.srcSoundBuffer = copyBuffer(this.adfContext.getSrcSoundRaw());
			this.destSoundBuffer = copyBuffer(this.adfContext.getDestSoundRaw());
		}
	}
	
	public void clearState() {
		this.adfContext = null;
		this.srcSoundBuffer = null;
		this.destSoundBuffer = null;
		this.dataArea.getSrcWord().setText(null);
		this.dataArea.getDestWord().setText(null);
		//setControlsDisabledState();
	}
}
