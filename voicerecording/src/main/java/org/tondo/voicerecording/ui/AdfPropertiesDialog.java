package org.tondo.voicerecording.ui;

import java.util.function.UnaryOperator;

import org.tondo.voicerecording.Voicerecording;
import org.tondo.voicerecording.adf.AdfHeader;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.GridPane;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfPropertiesDialog extends Dialog<AdfHeader> {
	
	private TextField srcLang;
	private TextField destLang;
	
	public AdfPropertiesDialog() {
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.setTitle("Select ADF properties");
		
		
		// this handle OK button action event, and according to validation result it can
		// cancel of the dialog closing
		((Button)this.getDialogPane().lookupButton(ButtonType.OK)).addEventFilter(ActionEvent.ACTION, event -> {
		     if (!validateDialog()) {
		         event.consume();
		     }
		 });
		
		this.createDialogControls();
		
		this.setResultConverter(btn -> {
			if (btn == ButtonType.OK) {
				return createResult();
			} else {
				return null;
			}
		});
	}
	
	
	private boolean validateDialog() {
		return !this.srcLang.getText().isEmpty() && !this.destLang.getText().isEmpty();
	}
	
	private void createDialogControls() {
		GridPane grid = new GridPane();
		
		this.srcLang = new TextField();
		this.destLang = new TextField();
		
		grid.setHgap(7);
		grid.setVgap(4);
		grid.add(new Label("Source Language"), 0, 0);
		grid.add(new Label("Destination language"), 0, 1);
		grid.add(this.srcLang, 1, 0);
		grid.add(this.destLang, 1, 1);
		
		this.srcLang.setMaxWidth(40.0);
		this.destLang.setMaxWidth(40.0);
		
		
		final int MAX_LANG_LEN = 3;
		
		UnaryOperator<Change> lenghtRestrictor = c -> {
			if (c.isContentChange() && c.getControlNewText().length() > MAX_LANG_LEN) {
				return null;
			}
			return c;
		};
		
		// this should restrict maximum characters in textfield
		this.srcLang.setTextFormatter(new TextFormatter<>(lenghtRestrictor));
		this.destLang.setTextFormatter(new TextFormatter<>(lenghtRestrictor));
		
		
		// margins inside cell
		//GridPane.setMargin(this.destLang, new Insets(2, 4, 6, 8));
		
		this.getDialogPane().setContent(grid);
	}
	
	
	protected AdfHeader createResult() {
		AdfHeader header = new AdfHeader();
		header.setAudioFormat(Voicerecording.getAudioFormat());
		header.setSrcLoc(this.srcLang.getText());
		header.setDestLoc(this.destLang.getText());
		return header;
	}
}
