package org.tondo.voicerecording;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.control.AdfFileDialogsController;
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
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application{
	
	private Stage mainStage;
	private DataArea dataArea;
	private DataAreaController dataAreaCtr;
	
	
	private Button tbLoad;
	private Button tbSave;
	private Button tbNew;
	private Button tbExport;
	private Button tbNewEntry;
	private Button tbEditEntry;
	private Button tbDeleteEntry;
	
	
	private AdfListView adfListEntries;
	private ListController listController;
	
	private MainContext controller;
	private AdfFileDialogsController fileDialog;
	
	private static final Path SETTINGS_LOC = Paths.get("settings.properties");
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.controller = new MainContext();
		
		primaryStage.setTitle("Nothing loaded");
		this.mainStage = primaryStage;
		this.controller.setSettings(AppSettings.load(SETTINGS_LOC));
		
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
		this.fileDialog = new AdfFileDialogsController(primaryStage);
		
		// this will disable controls
		this.dataAreaCtr.setAdfContext(null);
		this.refreshToolbarState();
		
		primaryStage.setScene(new Scene(layout));
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			onMainWindowClosing(e);
		});
	}
	
	private ToolBar createToolbar() {
		ToolBar toolbar = new ToolBar();
		
		this.tbNew = new Button("NEW");
		this.tbLoad = new Button("LOAD");
		this.tbSave = new Button("SAVE");
		this.tbExport = new Button("EXPORT");
		this.tbNewEntry = new Button("New Entry");
		this.tbEditEntry = new Button("Edit entry");
		this.tbDeleteEntry = new Button("Delete");
		toolbar.getItems().addAll(this.tbNew, this.tbLoad, this.tbSave, this.tbExport, new Separator(), this.tbNewEntry, this.tbEditEntry, this.tbDeleteEntry);
		
		this.tbNew.setOnAction(e -> onButtonNewAdf());
		this.tbLoad.setOnAction(e -> onButtonLoadAdf());
		this.tbSave.setOnAction(e -> onButtonSaveAdf());
		this.tbExport.setOnAction(e -> onButtonExportAdf());
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
		this.fileDialog.newAdfDialog(this.controller);
		AdfFile adf = this.controller.getAdfFile();
		if (adf != null) {
			this.listController.setEntries(adf.getEntries());

			// TODO remove after tests
			AdfEntry entry = new AdfEntry();
			entry.setSrcWord("spinas");
			entry.setDestWord("kkt");
			entry.setDestSoundRaw(new byte[5]);
			
			
			this.controller.setEditState(EditingState.NEW);
			this.dataAreaCtr.init(adf.getHeader());
			this.dataAreaCtr.setAdfContext(entry);
			this.dataAreaCtr.setEditable(true);
		} else {
			this.listController.setEntries(null);
			this.controller.setEditState(null);
			this.dataAreaCtr.setAdfContext(null);
			this.dataAreaCtr.setEditable(false);
		}
		
		updateWindowTitle();
		refreshToolbarState();
	}
	
	private void onNewEntry() {
		if (this.controller.getEditState() != EditingState.BROWSE) {
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
		
		refreshToolbarState();
	}
	
	private void onButtonExportAdf() {
		this.fileDialog.exportAdfDialog(this.controller);
	}
	
	private void onEditEntry() {
		if (this.controller.getEditState() != EditingState.BROWSE) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setContentText("TODO: Entry edited!!!");
			alert.showAndWait();
			return;
		}
		
		this.dataAreaCtr.setEditable(true);
		this.adfListEntries.setDisable(true);
		this.controller.setEditState(EditingState.EDIT);
		
		refreshToolbarState();
	}
	
	private void onDeleteEntry() {
		if (this.controller.getEditState() == EditingState.BROWSE) {
			if (this.listController.removeSelected()) {
				this.controller.markChanged();
			}
		}
	}
	
	private void onButtonSaveAdf() {
		this.fileDialog.saveAdfDialog(this.controller);
		
		updateWindowTitle();
	}
	
	private void onButtonLoadAdf() {
		this.fileDialog.loadAdfDialog(this.controller);
		
		AdfFile loadedAdf = this.controller.getAdfFile();
		if (loadedAdf != null) {
			this.listController.setEntries(loadedAdf.getEntries());
			this.controller.setEditState(EditingState.BROWSE);
			this.dataAreaCtr.init(loadedAdf.getHeader());
		}
		
		updateWindowTitle();
		refreshToolbarState();
	}
	
	
	// ------------- TOOLBAR HANDLERS 
	private void refreshToolbarState() {
		boolean hasAdf = this.controller.getAdfFile() != null;
		boolean hasSelected = this.listController.getSelected() != null;
		boolean isEditing = this.controller.getEditState() == EditingState.NEW || this.controller.getEditState() == EditingState.EDIT;
		
		this.tbNew.setDisable(isEditing);
		this.tbLoad.setDisable(isEditing);
		this.tbSave.setDisable(!hasAdf || isEditing);
		this.tbExport.setDisable(!hasAdf || isEditing);
		this.tbNewEntry.setDisable(!hasAdf || isEditing);
		this.tbEditEntry.setDisable(isEditing || !hasSelected);
		this.tbDeleteEntry.setDisable(isEditing || !hasSelected);
	}
	
	
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
		this.controller.setEditState( EditingState.BROWSE);
		
		refreshToolbarState();
	}
	
	private void onChangeConfirm() {
		AdfEntry entry = this.dataAreaCtr.confirmChanges();
		this.dataAreaCtr.setEditable(false);
		
		if (EditingState.NEW == this.controller.getEditState()) {
			this.listController.addEntry(entry);
		} else if (EditingState.EDIT == this.controller.getEditState()) {
			this.listController.updateEntry(entry);
		}
		this.controller.setEditState(EditingState.BROWSE);
		this.controller.markChanged();
		refreshToolbarState();
	}
	
	// ------------ DATA AREA HANDLERS
	
	
	/* ==================================================*/
	// adfListEntries
	private void onListSelectedItemChanged(AdfEntry newValue) {
		this.dataAreaCtr.setAdfContext(newValue);
		refreshToolbarState();
	}
	
	private void onMainWindowClosing(WindowEvent e) {
		if(!this.fileDialog.closeApplication(this.controller)) {
			e.consume();
		} else {
			applicationShutdownRutines();
		}
	}
	
	private void applicationShutdownRutines() {
		AppSettings.save(SETTINGS_LOC, this.controller.getSettings());
	}
	
	private void updateWindowTitle() {
		Path file = this.controller.getFileLocation();
		if (file != null) {
			this.mainStage.setTitle("File: " + file.getFileName());
		} else if (this.controller.getAdfFile() != null) {
			this.mainStage.setTitle("File: not saved");
		} else {
			this.mainStage.setTitle("Nothing loaded");
		}
	}
}
