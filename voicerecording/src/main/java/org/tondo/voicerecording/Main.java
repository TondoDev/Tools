package org.tondo.voicerecording;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application{
	
	
	private Label srcLabel;
	private TextField srcWord;
	private Button srcSoundRec;
	private Button srcSoundPlay;
	private Button srcSoundRemove;
	
	private Label destlabel;
	private TextField destWord;
	private Button destSoundRec;
	private Button destSoundPlay;
	private Button destSoundRemove;
	
	
	private Button tbLoad;
	private Button tbSave;
	private Button tbNew;
	private Button tbNewEntry;
	private Button tbDeleteEntry;
	
	private ListView<String> listEntries;
	

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Layout");
		//primaryStage.initStyle(StageStyle.TRANSPARENT);

		
		
		BorderPane layout = new BorderPane();
		layout.setTop(createToolbar());
		layout.setCenter(createDataArea());
		
		this.listEntries = new ListView<>();
		this.listEntries.getItems().addAll("kkt", "cck");
		layout.setLeft(listEntries);
		
		primaryStage.setScene(new Scene(layout));
		
		
		primaryStage.show();
	}
	
	
	private VBox createDataArea() {
		this.srcLabel = new Label("SK");
		this.srcWord = new TextField();
		this.srcSoundRec = new Button("Rec");
		this.srcSoundPlay = new Button("Play");
		this.srcSoundRemove = new Button("Remove");
		
		
		HBox srcHbox = new HBox();
		srcHbox.setSpacing(10);
		srcHbox.setPadding(new Insets(5));
		srcHbox.getChildren().addAll(this.srcLabel, this.srcWord, this.srcSoundRec, this.srcSoundPlay, this.srcSoundRemove);
		srcHbox.setAlignment(Pos.CENTER_LEFT);
		
		
		this.destlabel = new Label("DE");
		this.destWord = new TextField();
		this.destSoundRec = new Button("Rec");
		this.destSoundPlay = new Button("Play");
		this.destSoundRemove = new Button("Remove");
		
		HBox destHbox = new HBox();
		destHbox.setSpacing(10);
		destHbox.setPadding(new Insets(5));
		destHbox.setAlignment(Pos.CENTER_LEFT);

		destHbox.getChildren().addAll(this.destlabel, this.destWord,  this.destSoundRec, this.destSoundPlay, this.destSoundRemove);
		
		HBox specialCharsHBox = new HBox();
		specialCharsHBox.setPadding(new Insets(5));
		specialCharsHBox.setSpacing(10);
		Button upperA = createSpecialCharButton("\u00c4");
		Button upperO = createSpecialCharButton("\u00d6");
		Button upperU = createSpecialCharButton("\u00dc");
		Button lowerA = createSpecialCharButton("\u00e4");
		Button lowerU = createSpecialCharButton("\u00fc");
		Button lowerO = createSpecialCharButton("\u00f6");
		Button stringS = createSpecialCharButton("\u00df");
		
		
		specialCharsHBox.getChildren().addAll(upperA, upperO, upperU, lowerA, lowerO, lowerU, stringS);
		
		
		VBox dataAreaBox = new VBox();
		dataAreaBox.setSpacing(4);
		dataAreaBox.getChildren().addAll(srcHbox, destHbox, specialCharsHBox);
		
		return dataAreaBox;
	}
	
	
	private ToolBar createToolbar() {
		ToolBar toolbar = new ToolBar();
		
		this.tbNew = new Button("NEW");
		this.tbLoad = new Button("LOAD");
		this.tbSave = new Button("SAVE");
		
		this.tbNewEntry = new Button("New Entry");
		this.tbDeleteEntry = new Button("Delete");
		
		toolbar.getItems().addAll(this.tbNew, this.tbLoad, this.tbSave, new Separator(), this.tbNewEntry, this.tbDeleteEntry);
		
		return toolbar;
	}
	
	
	private Button createSpecialCharButton(String specialChar) {
		Button btn = new Button(specialChar);
		// toto zabrani, aby tlacitko s pismenom dostalo focus pri stlaceni
		// a ten ostal na textfielde, kde sa maju doplnit znaky
		btn.setFocusTraversable(false);
		btn.setOnAction(e -> {
			Node focusOwner = this.destlabel.getScene().getFocusOwner();
			if (focusOwner instanceof TextField) {
				TextField f = (TextField)focusOwner;
				int carret = f.getCaretPosition();
				f.insertText(carret, specialChar);
			}
		});
		
		
		return btn;
	}
	
	
}
