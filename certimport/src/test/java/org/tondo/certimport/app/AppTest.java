package org.tondo.certimport.app;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Test;
import org.tondo.certimport.Certimport;
import org.tondo.testutils.ConsoleOutputCapturer;
import org.tondo.testutils.StandardTestBase;

/**
 * 
 * @author TondoDev
 *
 */
public class AppTest extends StandardTestBase{
	
	@After
	public void resetCapturer() {
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.stopCapturing();
		capturer.capture();
		capturer.stopCapturing();
	}

	@Test
	public void testCheckExistancePositive() {
		Path truststore = inResourceDir("fbtrust");
		
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", "https://www.facebook.com"
		});
		capturer.stopCapturing();
		assertArrayEquals("stdout contains positive message", 
					new String[] {
							"Trusted: true", 
							"Alias: facebook"},
					capturer.getLines());
		assertArrayEquals("nothing put on stderr", new String[] {}, capturer.getErrLines());
	}
	
	
	@Test
	public void testCheckExistanceNegative() {
		Path truststore = inResourceDir("trustempty");
		
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", "https://www.facebook.com"
		});
		capturer.stopCapturing();
		assertArrayEquals("stdout contains negative message", 
					new String[] {
							"Trusted: false"},
					capturer.getLines());
		assertArrayEquals("nothing put on stderr", new String[] {}, capturer.getErrLines());
	}
	
	// probably depends on system setting
	@Test
	public void testCheckExistanceNotExistingURL() {
		Path truststore = inResourceDir("trustempty");
		
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", "https://www.guguggg.com"
		});
		capturer.stopCapturing();
		assertArrayEquals("stdout is empty", new String[] {}, capturer.getLines());
		assertArrayEquals("stderr contains message", 
				new String[] {"Connection refused: connect"},
				capturer.getErrLines());
	}
	
	@Test
	public void testAddLeaf() throws IOException {
		Path truststore = getFileKeeper().copyResource(inResourceDir("fbtrust"));
		assertTrue("Trustore file exists", Files.exists(truststore));
		
		String site = "https://github.com/";
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", site
		});
		capturer.stopCapturing();
		assertArrayEquals("Site is not trusted so far", 
				new String[] {
						"Trusted: false"},
				capturer.getLines());
		
		assertArrayEquals("stderr has nothing", 
				new String[] {},
				capturer.getErrLines());
		
		// now add leaf certificate for site
		capturer.capture();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-al",
				"-url", site
		});
		
		// TODO assert when result handler is completed
		//
		//
		//
		capturer.stopCapturing();
		
		
		// check again if certificate is trusted after add into trustore
		capturer.capture();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", site
		});
		capturer.stopCapturing();
		
		assertArrayEquals("Site is trusted after adding", 
				new String[] {
						"Trusted: true", 
						"Alias: github.com"}, // alias created by default algo
				capturer.getLines());
		
		assertArrayEquals("stderr has nothing", 
				new String[] {},
				capturer.getErrLines());
	}
	
	
	@Test
	public void testAddRoot() throws IOException {
		Path truststore = getFileKeeper().copyResource(inResourceDir("fbtrust"));
		assertTrue("Trustore file exists", Files.exists(truststore));
		
		String site = "https://github.com/";
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", site
		});
		capturer.stopCapturing();
		assertArrayEquals("Site is not trusted so far", 
				new String[] {
						"Trusted: false"},
				capturer.getLines());
		
		assertArrayEquals("stderr has nothing", 
				new String[] {},
				capturer.getErrLines());
		
		capturer.capture();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-ar",
				"-url", site
		});
		
		// TODO assert when result handler is completed
		//
		//
		//
		capturer.stopCapturing();
		
		
		// check again if certificate is trusted after add into trustore
		capturer.capture();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", site
		});
		capturer.stopCapturing();
		
		assertArrayEquals("Site is trusted after adding", 
				new String[] {
						"Trusted: true", 
						// alias created by default algo (looks bad in this case)
						// now is take CA certifiate
						"Alias: digicert sha2 extend"}, 
				capturer.getLines());
		
		assertArrayEquals("stderr has nothing", 
				new String[] {},
				capturer.getErrLines());
	}
	
	
	@Test
	public void testAddAll() throws IOException {
		Path truststore = getFileKeeper().copyResource(inResourceDir("fbtrust"));
		assertTrue("Trustore file exists", Files.exists(truststore));
		
		String site = "https://github.com/";
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-al",
				"-url", site
		});
		
		// TODO assert when result handler is completed
		//
		//
		//
		capturer.stopCapturing();
		
		
		// check again if certificate is trusted after add into trustore
		capturer.capture();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", site
		});
		capturer.stopCapturing();
		
		assertArrayEquals("Site is trusted after adding", 
				new String[] {
						"Trusted: true", 
						// alias created by default algo (looks bad in this case)
						// now is take CA certifiate
						"Alias: github.com"}, 
				capturer.getLines());
		
		assertArrayEquals("stderr has nothing", 
				new String[] {},
				capturer.getErrLines());
	}
	
	@Test
	public void testAddToNotExistingTrustoreWithotCreateOption() throws IOException {
		Path truststore = inTempDir("notexistsing");
		assertFalse("Trustore file doesn't exists", Files.exists(truststore));
		
		String site = "https://github.com/";
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-al",
				"-url", site
		});
		
		assertArrayEquals("Stdout is empty", 
				new String[] {}, // alias created by default algo
				capturer.getLines());
		
		assertArrayEquals("stderr contains error message", 
				new String[] {"Truststore file not found. Run with -tc if you want create empty."},
				capturer.getErrLines());
	}
	
	// trustore file will be created
	@Test
	public void testAddToNotExistingTrustoreWithCreateOption() throws IOException {
		Path truststore = inTempDir("notexistsing");
		getFileKeeper().markForWatch(truststore);
		
		assertFalse("Trustore file doesn't exists", Files.exists(truststore));
		
		String site = "https://github.com/";
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		Certimport app = new Certimport();
		app.run(new String[] {
				"-tc", truststore.toString(),
				"-pw", "trusted",
				"-al",
				"-url", site
		});
		
		capturer.stopCapturing();
		
		assertTrue("Trustore file created", Files.exists(truststore));
		
		// TODO assert when result handler is completed
		//
		//
		//
				
		
//		assertArrayEquals("Stdout is empty", 
//				new String[] {}, // alias created by default algo
//				capturer.getLines());
//		
		assertArrayEquals("stderr is empty", 
				new String[] {},
				capturer.getErrLines());
		
		
		capturer.capture();
		// check if trusted
		app.run(new String[] {
				"-t", truststore.toString(),
				"-pw", "trusted",
				"-c",
				"-url", site
		});
		
		assertArrayEquals("Site is trusted after adding", 
				new String[] {
						"Trusted: true", 
						// alias created by default algo (looks bad in this case)
						// now is take CA certifiate
						"Alias: github.com"}, 
				capturer.getLines());
		
		assertArrayEquals("stderr has nothing", 
				new String[] {},
				capturer.getErrLines());
		
		
	}
}
