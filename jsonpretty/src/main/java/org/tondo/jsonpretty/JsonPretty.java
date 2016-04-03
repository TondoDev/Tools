package org.tondo.jsonpretty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author TondoDev
 *
 */
public class JsonPretty {

	
	public static void main(String[] args) {
		Map<String, Object> data = new HashMap<>();
		data.put("meno", "Tondo");
		data.put("vek", 15);
		String papuca = "{\"meno\":\"Tondo\",\"vek\":15}";
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		list.add(data);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			DefaultPrettyPrinter pp = new DefaultPrettyPrinter("");
			mapper.writer(pp).writeValue(System.out, list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
