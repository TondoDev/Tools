package org.tondo.voicerecording.ui;

import org.tondo.voicerecording.Voicerecording;
import org.tondo.voicerecording.adf.AdfHeader;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfPropertiesDialog extends Dialog<AdfHeader> {
	
	public AdfPropertiesDialog() {
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.setTitle("Select ADF properties");
		
		this.setResultConverter(btn -> {
			if (btn == ButtonType.OK) {
				return createResult();
			} else {
				return null;
			}
		});
	}
	
	
	protected AdfHeader createResult() {
		AdfHeader header = new AdfHeader();
		header.setAudioFormat(Voicerecording.getAudioFormat());
		header.setSrcLoc("SK");
		header.setDestLoc("DE");
		return header;
	}
}
