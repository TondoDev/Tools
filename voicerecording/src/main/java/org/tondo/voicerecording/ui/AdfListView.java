package org.tondo.voicerecording.ui;

import org.tondo.voicerecording.adf.AdfEntry;

import javafx.collections.ObservableList;
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
		
		private static final String DND_COLOR = "-fx-background-color: rgb(153, 187, 255)";
		public AdfCell() {
			setOnDragDetected(e -> {
				// don't start for empty cells
				if (getItem() == null) {
					return;
				}
				Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString(String.valueOf(getIndex()));
				db.setContent(content);
				e.consume();
			});
			
			
			setOnDragOver(e -> {
				if  (e.getGestureSource() != this) {
					e.acceptTransferModes(TransferMode.MOVE);
				}
				e.consume();
			});
			
			
			setOnDragEntered(e -> {
				if (this == e.getGestureSource()) {
					return;
				}
				
				int size = this.getListView().getItems().size();
				int current = this.getIndex();
				if (current <= size) {
					this.setStyle(DND_COLOR);
				}
			
				e.consume();
			});
			
			setOnDragExited(e -> {
				if (this == e.getGestureSource()) {
					return;
				}
				this.setStyle("");
				e.consume();
			});
			
			
			
			// this is called after DragDropped
			setOnDragDone(e -> {
				if (e.getTransferMode() == TransferMode.MOVE) {
					Dragboard db = e.getDragboard();
					String contentString = db.getString();
					int sourceIndex = getIndex();
					int selectedIndex;
					if (contentString != null) {
						Integer targetIndex = Integer.valueOf(contentString);
						if (targetIndex < sourceIndex) {
							sourceIndex++;
							selectedIndex = targetIndex;
						} else {
							selectedIndex = targetIndex - 1;
						}
					}  else {
						// one lower because of zero based indexing, and one for 
						// the one entry which will be removed
						selectedIndex = this.getListView().getItems().size() - 2;
					}
					this.getListView().getItems().remove(sourceIndex);
					this.getListView().getSelectionModel().select(selectedIndex);
				}
				
				e.consume();
			});
			
			setOnDragDropped(e -> {
				AdfEntry source = ((AdfCell)e.getGestureSource()).getItem();
				int sourceIndex = ((AdfCell)e.getGestureSource()).getIndex();
				
				ClipboardContent content = new ClipboardContent();
				// add at the end
				if (this.getItem() == null) {
					this.getListView().getItems().add(source);
					content.putString(null);
				} else {
					// put on target location and targerd is moved by one
					int index = this.getIndex();
					if (index - sourceIndex == 1) {
						index++;
					}
					// we mark index of target, because dragDropped event will need it, while 
					// releasing source
					
					content.putString(String.valueOf(index));
					this.getListView().getItems().add(index, source);
				}
				
				Dragboard db = e.getDragboard();
				db.setContent(content);
				e.setDropCompleted(true);
				
				e.consume();
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
		
		private  void debugPrint(String heading) {
			System.out.println("== START " + heading);
			
			ObservableList<AdfEntry>  items =  this.getListView().getItems();
			for (int index = 0; index < items.size();  index++) {
				System.out.println(" Index " + index + ": " + items.get(index));
			}
			System.out.println("== END " + heading);
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "["+getItem().getSrcWord() + " - " + getItem().getDestWord()+"]";
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
