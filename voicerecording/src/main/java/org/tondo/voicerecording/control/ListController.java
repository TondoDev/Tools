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
			this.listView.setItems(this.entries);
		} else {
			this.entries = null;
		}
	}
	
	
	public void addEntry(AdfEntry entry) {
		this.listView.getItems().add(entry);
		this.listView.setDisable(false);
		this.listView.getSelectionModel().select(entry);
	}
	
	
	public void updateEntry(AdfEntry entry) {
		this.listView.setDisable(false);
		int idx = findEntry(entry);
		if (idx >= 0) {
			this.entries.set(idx, entry);
		}
		//this.listView.refresh();
	}
	
	public AdfEntry getSelected() {
		return this.listView.getSelectionModel().getSelectedItem();
	}
	
	
	
	private int findEntry(AdfEntry entry) {
		int len = this.entries.size();
		for (int i = 0; i < len; i++) {
			if (this.entries.get(i) == entry) {
				return i;
			}
		}
		
		return -1;
	}
}
