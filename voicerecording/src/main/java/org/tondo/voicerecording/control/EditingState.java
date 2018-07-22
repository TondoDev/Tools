package org.tondo.voicerecording.control;

/**
 * States of application regarding editing of ADF entries.
 * 
 * @author TondoDev
 *
 */
public enum EditingState {

	// read only browsing
	BROWSE,
	
	// existing item being edited
	EDIT,
	
	// new item is created
	NEW;
}
