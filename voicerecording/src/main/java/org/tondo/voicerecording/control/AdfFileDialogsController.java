package org.tondo.voicerecording.control;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.AdfHeader;
import org.tondo.voicerecording.audio.AdfStreamer;
import org.tondo.voicerecording.audio.FfmpegMp3Convertor;
import org.tondo.voicerecording.ui.AdfPropertiesDialog;
import org.tondo.voicerecording.ui.ExportConfiguration;
import org.tondo.voicerecording.ui.ExportDialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfFileDialogsController {
	private AdfFileAccessFacade fileAccess = new AdfFileAccessFacade();
	private ExportDialog exportDialog;
	private FfmpegMp3Convertor convertor = new FfmpegMp3Convertor("bin/ffmpeg/bin/ffmpeg.exe");
	
	
	public static enum DialogResult {
		OK, 
		CANCEL,
		// used when one part of dialog conversation caused persistent changes 
		// but following operation was canceled
		NOT_COMPLETED
	}
	
	
	private Stage stage;
	
	public AdfFileDialogsController(Stage stage) {
		this.stage = stage;
	}
	
	
	public DialogResult newAdfDialog(MainContext context) {
		if (context.getAdfFile() != null) {
			ButtonData result = showYesNoCancel("You have already opened an ADF file. Save it?");
			if (result == ButtonData.CANCEL_CLOSE) {
				return DialogResult.CANCEL;
			} else if (result == ButtonData.YES) {
				DialogResult saveREsult =  this.saveAdfDialog(context);
				if (saveREsult == DialogResult.CANCEL) {
					return DialogResult.CANCEL;
				}
			}
		}
		
		Optional<AdfHeader> headerForNewAdf = new AdfPropertiesDialog().showAndWait();
		if (!headerForNewAdf.isPresent()) {
			System.out.println("== SAVE WITHOUT CREATE");
			context.setAdfFile(null);
			context.setFileLocation(null);
			return DialogResult.NOT_COMPLETED;
		}

		context.createNewAdfFile(headerForNewAdf.get());
		return DialogResult.OK;
	}
	
	public DialogResult saveAdfDialog(MainContext context) {
		Path location = context.getFileLocation();
		// ADF has its persistent state, so we need ask user if he wants overwrite it
		if (location != null) {
			ButtonData result = showOverwriteDialog("Overwrite file " + location.getFileName() + "?");
			 if (result == ButtonData.CANCEL_CLOSE) {
				 return DialogResult.CANCEL;
			 } else if (result == ButtonData.YES) {
				 // overwriting current file
				 this.fileAccess.saveAdf(context.getAdfFile(), context.getFileLocation());
			 } else if (result == ButtonData.NO) {
				 // select new file for saving 
				 if (!saveByDialog(context)) {
					 return DialogResult.CANCEL;
				 }
			 }
		} else if (!saveByDialog(context)) {
			return DialogResult.CANCEL;
		}
		
		return DialogResult.OK;
	}
	
	
	public DialogResult loadAdfDialog(MainContext context) {
		if (context.getAdfFile() != null) {
			ButtonData result = showYesNoCancel("You have already opened an ADF file. Save it?");
			if (result == ButtonData.CANCEL_CLOSE) {
				return DialogResult.CANCEL;
			} else if (result == ButtonData.YES) {
				DialogResult saveREsult =  this.saveAdfDialog(context);
				if (saveREsult == DialogResult.CANCEL) {
					return DialogResult.CANCEL;
				}
			}
		}
		
		return loadByDialog(context) ? DialogResult.OK : DialogResult.CANCEL;
	}
	
	public DialogResult exportAdfDialog(MainContext context) {
		
		Optional<ExportConfiguration> exportConfig = getExportDialog().showAndWait(context.getAdfFile());
		if (!exportConfig.isPresent()) {
			System.out.println("EXPORT NO");
			return DialogResult.CANCEL;
		}
		
		ExportConfiguration conf = exportConfig.get();
		AdfStreamer streamer = new AdfStreamer(context.getAdfFile().getHeader().getAudioFormat());
		streamer.initPlayback(conf.getPlaySequence(), conf.getEntries());
		
		this.convertor.convert(streamer, exportConfig.get().getOutFile().toString());
		return DialogResult.OK;
	}
	
	
	private boolean saveByDialog(MainContext context) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save ADF");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Adf files", "*.adf"));
		File fileToSave = fileChooser.showSaveDialog(this.stage);

		if (fileToSave == null) {
			return false;
		}

		Path pathToSave = fileToSave.toPath();
		this.fileAccess.saveAdf(context.getAdfFile(), pathToSave);
		context.setFileLocation(pathToSave);
		return true;
	}
	
	private boolean loadByDialog(MainContext context) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load ADF");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Adf files", "*.adf"));
		File fileToLoad = fileChooser.showOpenDialog(this.stage);
		
		if (fileToLoad == null) {
			
			return false;
		}
		
		Path pathToLoad = fileToLoad.toPath();
		AdfFile adfFile = this.fileAccess.loadAdf(pathToLoad);
		context.setAdfFile(adfFile);
		context.setFileLocation(pathToLoad);
		
		return true;
	}
	
	private ExportDialog getExportDialog() {
		if (this.exportDialog == null) {
			this.exportDialog = new ExportDialog();
		}
		
		return this.exportDialog;
	}
	
	
	private static ButtonData showOverwriteDialog(String text) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		ButtonType btnSaveAs = new ButtonType("Save as", ButtonData.NO); 
		alert.getButtonTypes().setAll(ButtonType.YES, btnSaveAs, ButtonType.CANCEL);
		alert.setContentText(text);
		return alert.showAndWait().get().getButtonData();
	}
	
	private static ButtonData showYesNoCancel(String text) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		alert.setContentText(text);
		return alert.showAndWait().get().getButtonData();
	}
}
