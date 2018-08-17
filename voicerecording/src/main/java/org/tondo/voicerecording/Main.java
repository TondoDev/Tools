package org.tondo.voicerecording;

import java.io.File;
import java.nio.file.Path;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.control.AdfFileAccessFacade;
import org.tondo.voicerecording.control.DataAreaController;
import org.tondo.voicerecording.control.EditingState;
import org.tondo.voicerecording.control.ListController;
import org.tondo.voicerecording.control.MainContext;
import org.tondo.voicerecording.ui.AdfListView;
import org.tondo.voicerecording.ui.DataArea;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application{
	
	private DataArea dataArea;
	private DataAreaController dataAreaCtr;
	
	
	private Button tbLoad;
	private Button tbSave;
	private Button tbNew;
	private Button tbNewEntry;
	private Button tbEditEntry;
	private Button tbDeleteEntry;
	
	private AdfListView adfListEntries;
	private ListController listController;
	
	private MainContext controller;
	private AdfFileAccessFacade adfAccess;
	
	private Stage mainStage;
	

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.controller = new MainContext();
		this.mainStage = primaryStage;
		
		primaryStage.setTitle("Layout");
		
		BorderPane layout = new BorderPane();
		layout.setTop(createToolbar());
		this.dataArea = new DataArea();
		this.dataAreaCtr = new DataAreaController(this.dataArea);
		layout.setCenter(this.dataArea);
		this.dataArea.getCancelButton().setOnAction(e -> onChangesDiscard());
		this.dataArea.getOkButton().setOnAction(e -> onChangeConfirm());
		
		this.adfListEntries = new AdfListView();
		layout.setLeft(adfListEntries);
		this.adfListEntries.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AdfEntry>() {

			@Override
			public void changed(ObservableValue<? extends AdfEntry> observable, AdfEntry oldValue, AdfEntry newValue) {
				onListSelectedItemChanged(newValue);
				
			}
		});
		this.listController = new ListController(this.adfListEntries);
		
		this.adfAccess = new AdfFileAccessFacade();
		
		// this will disable controls
		this.dataAreaCtr.setAdfContext(null);
		
		primaryStage.setScene(new Scene(layout));
		primaryStage.show();
	}
	
	private ToolBar createToolbar() {
		ToolBar toolbar = new ToolBar();
		
		this.tbNew = new Button("NEW");
		this.tbLoad = new Button("LOAD");
		this.tbSave = new Button("SAVE");
		this.tbNewEntry = new Button("New Entry");
		this.tbEditEntry = new Button("Edit entry");
		this.tbDeleteEntry = new Button("Delete");
		toolbar.getItems().addAll(this.tbNew, this.tbLoad, this.tbSave, new Separator(), this.tbNewEntry, this.tbEditEntry, this.tbDeleteEntry);
		
		this.tbNew.setOnAction(e -> onButtonNewAdf());
		this.tbLoad.setOnAction(e -> onButtonLoadAdf());
		this.tbSave.setOnAction(e -> onButtonSaveAdf());
		this.tbNewEntry.setOnAction(e -> onNewEntry());
		this.tbEditEntry.setOnAction(e -> onEditEntry());
		this.tbDeleteEntry.setOnAction(e -> onDeleteEntry());
		
		return toolbar;
	}
	
	
	// ============ TOOLBAR HANDLERS
	/**
	 * Creates new ADF file structure and begin edit of first ADF entry
	 */
	private void onButtonNewAdf() {
		
		if (this.controller.getAdfFile() != null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("TODO: One file already in progress!!!");
			alert.showAndWait();
			return;
		}

		AdfFile adf = this.controller.createNewAdfFile();
		this.listController.setEntries(adf.getEntries());
		AdfEntry entry = new AdfEntry();
		entry.setSrcWord("spinas");
		entry.setDestWord("kkt");
		entry.setDestSoundRaw(new byte[5]);
		this.controller.setEditState(EditingState.NEW);
		this.dataAreaCtr.setLanguages(adf.getHeader().getSrcLoc(), adf.getHeader().getDestLoc());
		this.dataAreaCtr.setAdfContext(entry);
		this.dataAreaCtr.setEditable(true);
	}
	
	private void onNewEntry() {
		if (this.controller.getEditState() != null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("TODO: Entry edited!!!");
			alert.showAndWait();
			return;
		}
		
		AdfEntry entry = new AdfEntry();
		this.dataAreaCtr.setAdfContext(entry);
		this.dataAreaCtr.setEditable(true);
		this.adfListEntries.setDisable(true);
		this.controller.setEditState(EditingState.NEW);
	}
	
	private void onEditEntry() {
		if (this.controller.getEditState() != null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("TODO: Entry edited!!!");
			alert.showAndWait();
			return;
		}
		
		this.dataAreaCtr.setEditable(true);
		this.adfListEntries.setDisable(true);
		this.controller.setEditState(EditingState.EDIT);
	}
	
	private void onDeleteEntry() {
		if (this.controller.getEditState() == null) {
			this.listController.removeSelected();
		}
	}
	
	private void onButtonSaveAdf() {
		if (this.controller.getAdfFile() == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("TODO: No Adf to save!");
			alert.showAndWait();
			return;
		} else if (this.controller.getFileLocation() == null) {
			Path selectedFile = openSaveDialog(this.mainStage);
			if (selectedFile != null) {
				this.adfAccess.saveAdf(this.controller.getAdfFile(), selectedFile);
				this.controller.setFileLocation(selectedFile);
			}
		} else {
			// just save into loaded path
			Alert alert = new Alert(AlertType.CONFIRMATION);
			if(alert.showAndWait().get()== ButtonType.OK) {
				System.out.println("OK");
				this.adfAccess.saveAdf(this.controller.getAdfFile(), this.controller.getFileLocation());
			}
		}
	}
	
	public void onButtonLoadAdf() {
		// trigger saving procedure
		if (this.controller.getAdfFile() != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			System.out.println(alert.showAndWait().get());
		} else {
			Path file = openLoadDialog(this.mainStage);
			if (file != null) {
				AdfFile loadedAdf = this.adfAccess.loadAdf(file);
				this.listController.setEntries(loadedAdf.getEntries());
				this.controller.setFileLocation(file);
				this.controller.setAdfFile(loadedAdf);
			}
		}
	}
	
	
	// ------------- TOOLBAR HANDLERS 
	
	
	// ============ DATA AREA HANDLERS
	private void onChangesDiscard() {
		AdfEntry lastEntry = this.listController.getSelected();
		if (lastEntry == null) {
			this.dataAreaCtr.clearState();
		} else {
			this.dataAreaCtr.setAdfContext(lastEntry);
		}
		this.dataAreaCtr.setEditable(false);
		this.adfListEntries.setDisable(false);
		this.controller.setEditState(null);
	}
	
	private void onChangeConfirm() {
		AdfEntry entry = this.dataAreaCtr.confirmChanges();
		this.dataAreaCtr.setEditable(false);
		
		if (EditingState.NEW == this.controller.getEditState()) {
			this.listController.addEntry(entry);
		} else if (EditingState.EDIT == this.controller.getEditState()) {
			this.listController.updateEntry(entry);
		}
		this.controller.setEditState(null);
	}
	
	// ------------ DATA AREA HANDLERS
	
	
	/* ==================================================*/
	// adfListEntries
	private void onListSelectedItemChanged(AdfEntry newValue) {
		this.dataAreaCtr.setAdfContext(newValue);
	}
	
	
	private static Path openSaveDialog(Stage stage) {
		return openFileDialog(stage, true);
	}
	
	private static Path openLoadDialog(Stage stage) {
		return openFileDialog(stage, false);
	}
	
	private static Path openFileDialog(Stage stage, boolean save) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Adf files", "*.adf"));
		File selectedFile = save ? fileChooser.showSaveDialog(stage) : fileChooser.showOpenDialog(stage);
		
		return selectedFile == null ? null : selectedFile.toPath();
	}
}
