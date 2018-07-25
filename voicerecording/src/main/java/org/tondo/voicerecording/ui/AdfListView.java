package org.tondo.voicerecording.ui;

import org.tondo.voicerecording.adf.AdfEntry;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfListView extends ListView<AdfEntry> {
	
	private class AdfCell extends ListCell<AdfEntry> {
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
