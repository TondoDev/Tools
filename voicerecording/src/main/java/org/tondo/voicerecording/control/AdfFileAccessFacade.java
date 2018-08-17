package org.tondo.voicerecording.control;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.tondo.voicerecording.adf.AdfFile;
import org.tondo.voicerecording.adf.io.AdfReader;
import org.tondo.voicerecording.adf.io.AdfWriter;

/**
 * 
 * @author TondoDev
 *
 */
public class AdfFileAccessFacade {

	private AdfWriter writer;
	private AdfReader reader;
	
	public AdfFileAccessFacade() {
		this.writer = new AdfWriter();
		this.reader = new AdfReader();
	}
	
	
	public boolean saveAdf(AdfFile file, Path location) {
		
		try (FileOutputStream fos = new FileOutputStream(location.toFile())) {
			this.writer.write(fos, file);
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	
	public AdfFile loadAdf(Path location) {
		
		try (FileInputStream fis = new FileInputStream(location.toFile())) {
			return reader.read(fis);
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		return null;
	}
}
