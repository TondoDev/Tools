package org.tondo.voicerecording;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
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
	

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.controller = new MainContext();
		
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
		this.tbNewEntry.setOnAction(e -> onNewEntry());
		this.tbEditEntry.setOnAction(e -> onEditEntry());
		
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
}
