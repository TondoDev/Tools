package org.tondo.voicerecording;

import java.util.List;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.control.MainController;
import org.tondo.voicerecording.ui.DataArea;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{
	
	private DataArea dataArea;
	
	
	private Button tbLoad;
	private Button tbSave;
	private Button tbNew;
	private Button tbNewEntry;
	private Button tbDeleteEntry;
	
	private ListView<AdfEntry> adfListEntries;
	
	private MainController controller;
	

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.controller = new MainController();
		
		primaryStage.setTitle("Layout");
		
		BorderPane layout = new BorderPane();
		layout.setTop(createToolbar());
		this.dataArea = new DataArea();
		layout.setCenter(this.dataArea);
		this.dataArea.setDisable(true);
		
		this.adfListEntries = new ListView<>();
		layout.setLeft(adfListEntries);
		
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
		
		return toolbar;
	}
	
	// LIST CONTROLLER
	private void onButtonNewAdf() {
		AdfFile adf = this.controller.createNewAdfFile();
		this.initListWithAdf(adf.getEntries());
	}
	
	/* ==================================================*/
	// dataArea
	
	
	
	/* ==================================================*/
	// adfListEntries
	private void initListWithAdf(List<AdfEntry> entries) {
		this.adfListEntries.getItems().clear();
		this.adfListEntries.getItems().addAll(entries);
	}
	
	
}
