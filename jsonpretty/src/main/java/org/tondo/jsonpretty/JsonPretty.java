package org.tondo.jsonpretty;

import org.tondo.jsonpretty.prettyfiers.JacksonMemmoryPrettyfier;
import org.tondo.jsonpretty.prettyfiers.Prettyfier;

/**
 * 
 * @author TondoDev
 *
 * Class is instantiable because of easier testability.
 */
public class JsonPretty {
	
	
	public JsonPretty() {
		
	}
	
	public void prettyPrint(String[] args) {
		Prettyfier prettyfier = new JacksonMemmoryPrettyfier();
		prettyfier.prettyfy(System.in, System.out, null);
	}

	public static void main(String[] args) {
		new JsonPretty().prettyPrint(args);
	}
}
