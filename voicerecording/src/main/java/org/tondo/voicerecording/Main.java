package org.tondo.voicerecording;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.control.DataAreaController;
import org.tondo.voicerecording.control.ListController;
import org.tondo.voicerecording.control.MainContext;
import org.tondo.voicerecording.ui.AdfListView;
import org.tondo.voicerecording.ui.DataArea;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
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
		this.tbDeleteEntry = new Button("Delete");
		toolbar.getItems().addAll(this.tbNew, this.tbLoad, this.tbSave, new Separator(), this.tbNewEntry, this.tbDeleteEntry);
		
		this.tbNew.setOnAction(e -> onButtonNewAdf());
		this.tbNewEntry.setOnAction(e -> onNewEntry());
		
		return toolbar;
	}
	
	// ============ TOOLBAR HANDLERS
	/**
	 * Creates new ADF file structure and begin edit of first ADF entry
	 */
	private void onButtonNewAdf() {
		AdfFile adf = this.controller.createNewAdfFile();
		this.listController.setEntries(adf.getEntries());
		AdfEntry entry = new AdfEntry();
		entry.setSrcWord("spinas");
		entry.setDestWord("kkt");
		entry.setDestSoundRaw(new byte[5]);
		this.dataAreaCtr.setLanguages(adf.getHeader().getSrcLoc(), adf.getHeader().getDestLoc());
		this.dataAreaCtr.setAdfContext(entry);
		this.dataAreaCtr.setEditable(true);
	}
	
	private void onNewEntry() {
		AdfEntry entry = new AdfEntry();
		this.dataAreaCtr.setAdfContext(entry);
		this.dataAreaCtr.setEditable(true);
		this.adfListEntries.setDisable(true);
		this.controller.setLastShownEntry(this.adfListEntries.getSelectionModel().getSelectedItem());
	}
	
	
	// ------------- TOOLBAR HANDLERS 
	
	
	// ============ DATA AREA HANDLERS
	private void onChangesDiscard() {
		AdfEntry lastEntry = this.controller.getLastShownEntry();
		if (lastEntry == null) {
			this.dataAreaCtr.clearState();
		} else {
			this.dataAreaCtr.setAdfContext(lastEntry);
		}
		this.dataAreaCtr.setEditable(false);
		this.adfListEntries.setDisable(false);
	}
	
	private void onChangeConfirm() {
		AdfEntry entry = this.dataAreaCtr.confirmChanges();
		this.dataAreaCtr.setEditable(false);
		this.adfListEntries.getItems().add(entry);
		this.adfListEntries.setDisable(false);
		this.adfListEntries.getSelectionModel().select(entry);
	}
	
	// ------------ DATA AREA HANDLERS
	
	
	/* ==================================================*/
	// adfListEntries
	private void onListSelectedItemChanged(AdfEntry newValue) {
		this.dataAreaCtr.setAdfContext(newValue);
	}
}
