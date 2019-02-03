package org.tondo.voicerecording.ui;

import org.tondo.voicerecording.adf.AdfEntry;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfListView extends ListView<AdfEntry> {
	
	private class AdfCell extends ListCell<AdfEntry> {
		
		public AdfCell() {
			setOnDragDetected(e -> {
				System.out.println("Drag detected!");
				Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(this.getText());
				db.setContent(content);
				e.consume();
			});
			
			
			setOnDragOver(e -> {
				if  (e.getGestureSource() != this) {
					e.acceptTransferModes(TransferMode.MOVE);
					//System.out.println(e.getDragboard().getString());
				}
				e.consume();
			});
			
			
			setOnDragEntered(e -> {
				System.out.println("Enter " + this.getText());
			});
			
			setOnDragExited(e -> {
				System.out.println("exit " + this.getText());
			});
		}
		@Override
		protected void updateItem(AdfEntry item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty && item != null) {
				// TODO implement correctly
				setText(item.getSrcWord() + " - " + item.getDestWord());
			} else {
				// because listView reuses ListCells
				setText(null);
			}
			
		}
	}

	public AdfListView() {
		super();
		
		setCellFactory(new Callback<ListView<AdfEntry>, ListCell<AdfEntry>>() {
			
			@Override
			public ListCell<AdfEntry> call(ListView<AdfEntry> param) {
				return new AdfCell();
			}
		});
	}
}
