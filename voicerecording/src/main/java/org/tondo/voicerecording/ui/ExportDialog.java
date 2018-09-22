package org.tondo.voicerecording.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.tondo.voicerecording.AppSettings;
import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.audio.AdfStreamer;
import org.tondo.voicerecording.audio.AdfStreamer.Sequence;
import org.tondo.voicerecording.control.MainContext;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;

/**
 * 
 * @author TondoDev
 *
 */
public class ExportDialog extends Dialog<ExportConfiguration> {
	
	private List<AdfEntry> entries;
	private Path path;
	
	private File initialFolder;
	
	public ExportDialog() {
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.setHeaderText("Export to MP3");
		
		
		((Button) this.getDialogPane().lookupButton(ButtonType.OK)).addEventFilter(ActionEvent.ACTION, event -> {

			FileChooser selectFileDialog =  new FileChooser();
			selectFileDialog.setTitle("Choose target file");
			selectFileDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 files", "*.mp3"));
			if (this.initialFolder != null) {
				selectFileDialog.setInitialDirectory(this.initialFolder);
			}
			File fileToSave = selectFileDialog.showSaveDialog(getOwner());
			if (fileToSave == null) {
				event.consume();
			} else {
				this.path = fileToSave.toPath();
			}
		});
		
			
		this.setResultConverter(btn -> {
			if (ButtonType.OK.equals(btn)) {
				return buildResult();
			}
			
			return null;
		});
	}
	
	
	public Optional<ExportConfiguration> showAndWait(MainContext context) {
		this.initDialogContent(context);
		Optional<ExportConfiguration> retval =  this.showAndWait();
		if (retval.isPresent()) {
			this.storeConfiguration(context.getSettings());
			context.getSettings().setExportLocation(retval.get().getOutFile().getParent().toString());
		}
		
		return retval;
	}
	
	
	private ExportConfiguration buildResult() {
		// TODO read from GUI 
		Sequence sequence = AdfStreamer.createSequence()
				.destination()
				.silence(400)
				.source()
				.silence(400)
				.destination()
				.silence(700);
		
		ExportConfiguration config = new ExportConfiguration(this.entries, sequence, this.path);
		return config;
	}
	
	private void initDialogContent(MainContext context) {
		AdfFile adfFile = context.getAdfFile();
		this.entries = adfFile.getEntries() == null ? new ArrayList<>() : new ArrayList<>(adfFile.getEntries());
		
		AppSettings settings = context.getSettings();
		String exportLocation =settings.getExportLocation();
		if (exportLocation != null) {
			this.initialFolder = new File(exportLocation);
		}
				
	}
	
	private void storeConfiguration(AppSettings settings) {
		
	}
}
