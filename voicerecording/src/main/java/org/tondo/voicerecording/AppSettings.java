package org.tondo.voicerecording;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 
 * @author TondoDev
 *
 */
public class AppSettings {

	private String adfLocation;
	private String exportLocation;
	private String srcLang;
	private String destLang;
	
	// TODO more properties
	
	
	public String getAdfLocation() {
		return adfLocation;
	}
	
	
	public void setAdfLocation(String adfLocation) {
		this.adfLocation = adfLocation;
	}
	
	public String getExportLocation() {
		return exportLocation;
	}
	
	public void setExportLocation(String exportLocation) {
		this.exportLocation = exportLocation;
	}
	
	public String getSrcLang() {
		return srcLang;
	}
	
	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}
	
	public String getDestLang() {
		return destLang;
	}
	
	public void setDestLang(String destLang) {
		this.destLang = destLang;
	}
	
	public static AppSettings load(Path configFile) {
		try (Reader reader = new InputStreamReader(new FileInputStream(configFile.toFile()), "UTF-8")) {
			Properties props = new Properties();
			props.load(reader);
			AppSettings settings = new AppSettings();
			initFromFile(settings, props);
			return settings;
		} catch (FileNotFoundException e) {
			System.err.println("Property file not found");
		} catch (IOException e) {
			System.err.println("IO exception reading property file!");
		}
		
		// empty settings instance when no settings file loaded
		return new AppSettings();
	}
	
	public static void save(Path configFile, AppSettings settings) {
		Properties props = new Properties();
		if (settings.destLang != null) {
			props.setProperty(DEST_LANG, settings.destLang);
		}
		
		if (settings.srcLang != null) {
			props.setProperty(SRC_LANG, settings.srcLang);
		}

		if (settings.adfLocation != null) {
			props.setProperty(ADF_LOCATION, settings.adfLocation);
		}

		if (settings.exportLocation != null) {
			props.setProperty(EXPORT_LOCATION, settings.exportLocation);
		}
		
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile.toFile()), "UTF-8")) {
			props.store(writer, "bla bla");
		} catch (IOException e) {
			System.err.println("Error during saving of application settings");
		} 
		
	}
	
	private static void initFromFile(AppSettings settings, Properties props) {
		settings.destLang = props.getProperty(DEST_LANG);
		settings.destLang = settings.destLang != null && settings.destLang.isEmpty() ? null : settings.destLang;
		
		settings.srcLang = props.getProperty(SRC_LANG);
		settings.srcLang = settings.srcLang != null && settings.srcLang.isEmpty() ? null : settings.srcLang;
		
		settings.adfLocation = props.getProperty(ADF_LOCATION);
		settings.adfLocation = settings.adfLocation != null && settings.adfLocation.isEmpty() ? null : settings.adfLocation;
		
		settings.exportLocation = props.getProperty(EXPORT_LOCATION);
		settings.exportLocation = settings.exportLocation != null && settings.exportLocation.isEmpty() ? null : settings.exportLocation;
	}
	
	private static final String SRC_LANG = "SRC_LANG";
	private static final String DEST_LANG = "DEST_LANG";
	private static final String EXPORT_LOCATION = "EXPORT_LOCATION";
	private static final String ADF_LOCATION = "ADF_LOCATION";
}
