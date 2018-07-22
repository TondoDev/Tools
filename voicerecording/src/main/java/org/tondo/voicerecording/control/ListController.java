package org.tondo.voicerecording.control;

import java.util.List;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.ui.AdfListView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 * @author TondoDev
 *
 */
public class ListController {
	
	private ObservableList<AdfEntry> entries;
	private AdfListView listView;
	
	public ListController (AdfListView listView) {
		this.listView = listView;
	}
	
	public void setEntries(List<AdfEntry> entries) {
		this.listView.getItems().clear();
		if (entries != null) {
			this.entries = FXCollections.observableList(entries);
			this.listView.getItems().addAll(this.entries);
		} else {
			this.entries = null;
		}
	}

}
