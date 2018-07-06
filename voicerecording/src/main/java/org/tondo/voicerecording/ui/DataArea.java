package org.tondo.voicerecording.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DataArea extends VBox {
	
	private Label srcLabel;
	private Button srcSoundRec;
	private Button srcSoundPlay;
	private Button srcSoundRemove;
	private TextField srcWord;
	
	private Label destlabel;
	private Button destSoundRec;
	private Button destSoundPlay;
	private Button destSoundRemove;
	private TextField destWord;
	
	private HBox specialCharsHBox;
	
	private Button ok;
	private Button cancel;
	
	
	public DataArea() {
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
		
		this.specialCharsHBox = new HBox();
		this.specialCharsHBox.setPadding(new Insets(5));
		this.specialCharsHBox.setSpacing(10);
		Button upperA = createSpecialCharButton("\u00c4");
		Button upperO = createSpecialCharButton("\u00d6");
		Button upperU = createSpecialCharButton("\u00dc");
		Button lowerA = createSpecialCharButton("\u00e4");
		Button lowerU = createSpecialCharButton("\u00fc");
		Button lowerO = createSpecialCharButton("\u00f6");
		Button strongS = createSpecialCharButton("\u00df");
		
		this.specialCharsHBox.getChildren().addAll(upperA, upperO, upperU, lowerA, lowerO, lowerU, strongS);
		
		HBox confirmBox = new HBox();
		confirmBox.setAlignment(Pos.BOTTOM_RIGHT);
		this.cancel = new Button("Cancel");
		this.ok = new Button("Ok");
		confirmBox.setSpacing(4);
		confirmBox.setPadding(new Insets(5));
		//System.out.println(confirmBox.getMaxHeight());
		//confirmBox.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(confirmBox, Priority.ALWAYS);
		confirmBox.getChildren().addAll(this.ok, this.cancel);
		
		this.setSpacing(4);
		this.getChildren().addAll(srcHbox, destHbox, specialCharsHBox, confirmBox);
	}
	
	
	private Button createSpecialCharButton(String specialChar) {
		Button btn = new Button(specialChar);
		// toto zabrani, aby tlacitko s pismenom dostalo focus pri stlaceni
		// a ten ostal na textfielde, kde sa maju doplnit znaky
		btn.setFocusTraversable(false);
		btn.setOnAction(e -> {
			Node focusOwner = this.getScene().getFocusOwner();
			if (focusOwner instanceof TextField) {
				TextField f = (TextField)focusOwner;
				int carret = f.getCaretPosition();
				f.insertText(carret, specialChar);
			}
		});
		return btn;
	}
	
	public Button getSrcSoundRec() {
		return srcSoundRec;
	}
	
	public Button getSrcSoundPlay() {
		return srcSoundPlay;
	}
	
	public Button getSrcSoundRemove() {
		return srcSoundRemove;
	}
	
	public TextField getSrcWord() {
		return srcWord;
	}
	
	public Button getDestSoundRec() {
		return destSoundRec;
	}
	
	public Button getDestSoundPlay() {
		return destSoundPlay;
	}
	
	public Button getDestSoundRemove() {
		return destSoundRemove;
	}
	
	public TextField getDestWord() {
		return destWord;
	}
	
	public Label getSrcLabel() {
		return srcLabel;
	}
	
	public Label getDestlabel() {
		return destlabel;
	}
	
	public HBox getSpecialCharsHBox() {
		return specialCharsHBox;
	}
	
	public Button getOkButton() {
		return this.ok;
	}
	
	public Button getCancelButton() {
		return this.cancel;
	}
}
