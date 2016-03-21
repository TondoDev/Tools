package org.tondo.certimport.app;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.tondo.certimport.Certimport;
import org.tondo.testutils.ConsoleOutputCapturer;
import org.tondo.testutils.StandardTestBase;

public class CmdTest extends StandardTestBase {
	

	@Before
	public void resetCapturer() {
		clearCapturer();
	}
	
	private void clearCapturer() {
		ConsoleOutputCapturer capturer = ConsoleOutputCapturer.getInstance();
		capturer.capture();
		capturer.stopCapturing();
		capturer.capture();
		capturer.stopCapturing();
	}
	
	@Test
	public void testEmptyCmd() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		assertTrue("Returs as OK",  importer.run(new String[] {}));
		assertEquals(
				"Standard help is printed", 
				"usage: certimport -url <https_url> -(aa|al|ar|c[-f]) -t <trustore_path>", 
				ConsoleOutputCapturer.getInstance().getLines()[0]);
	}
	
	@Test
	public void testWithHelp() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		assertTrue("Returs as OK",  importer.run(new String[] {"-h"}));
		assertEquals(
				"Standard help is printed", 
				"usage: certimport -url <https_url> -(aa|al|ar|c[-f]) -t <trustore_path>", 
				ConsoleOutputCapturer.getInstance().getLines()[0]);
		
		clearCapturer();
		
		// help and another option, which is ignored
		ConsoleOutputCapturer.getInstance().capture();
		assertTrue("Returs as OK",  importer.run(new String[] {"-h", "-aa"}));
		assertEquals(
				"Standard help is printed", 
				"usage: certimport -url <https_url> -(aa|al|ar|c[-f]) -t <trustore_path>", 
				ConsoleOutputCapturer.getInstance().getLines()[0]);
		
		clearCapturer();
		
		// but help must be provided first, otherwise other not complete arguments return error
		// because  in help parsing option set is first unknown parameter and all other following  considered as argument
		ConsoleOutputCapturer.getInstance().capture();
		assertFalse("Returs as OK", importer.run(new String[] { "-aa", "-h" }));
		assertEquals("Line with error printed at first",
				"Options parsing error: Missing required options: [-t path to trustore used for authenticate server, -tc path to trustore used for authenticate server, if not exist file is created], url",
				ConsoleOutputCapturer.getInstance().getLines()[0]);
		
		assertEquals("Standard help is printed",
				"usage: certimport -url <https_url> -(aa|al|ar|c[-f]) -t <trustore_path>",
				ConsoleOutputCapturer.getInstance().getLines()[1]);
	}
	
	@Test
	public void testParsingCheckCmd() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		assertNotNull("Returs as OK",  importer.parseCmdArgs(new String[] {"-c", "-t", "trustore", "-url", "https://www.something.com"}));
		assertEquals("Stdout empty", 0, ConsoleOutputCapturer.getInstance().getLines().length) ;
		assertEquals("Stderr empty", 0, ConsoleOutputCapturer.getInstance().getErrLines().length) ;
	}
	
	@Test
	public void testAddRootCmd() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		assertNotNull("Returs as OK",  importer.parseCmdArgs(new String[] {"-ar", "-t", "trustore", "-url", "https://www.something.com"}));
		assertEquals("Stdout empty", 0, ConsoleOutputCapturer.getInstance().getLines().length) ;
		assertEquals("Stderr empty", 0, ConsoleOutputCapturer.getInstance().getErrLines().length) ;
	}
	
	@Test
	public void testAddLeafWithCreateTrustoreCmd() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		assertNotNull("Returs as OK",  importer.parseCmdArgs(new String[] {"-al", "-tc", "trustore", "-url", "https://www.something.com"}));
		assertEquals("Stdout empty", 0, ConsoleOutputCapturer.getInstance().getLines().length) ;
		assertEquals("Stderr empty", 0, ConsoleOutputCapturer.getInstance().getErrLines().length) ;
	}
	
	@Test
	public void testMoreExclusiveActionsCmd() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		// -al is exclusive to -c
		assertNull("NUll means parsing failure",  importer.parseCmdArgs(new String[] {"-al", "-c", "-tc", "trustore", "-url", "https://www.something.com"}));

		// commons CLI parsing erros are printed with help to stdout
		assertEquals("First is Error message: ", 
				"Options parsing error: The option 'c' was specified but an option from this group has already been selected: 'al'",
				ConsoleOutputCapturer.getInstance().getLines()[0]);
		
		assertEquals("And help is printed to stdout", 
				"usage: certimport -url <https_url> -(aa|al|ar|c[-f]) -t <trustore_path>", // only first line examined 
				ConsoleOutputCapturer.getInstance().getLines()[1]) ;
		
		assertEquals("Stderr empty", 0, ConsoleOutputCapturer.getInstance().getErrLines().length) ;
	}
	
	@Test
	public void testAddAllWithForcedCmd() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		assertNotNull("Returs as OK",  importer.parseCmdArgs(new String[] {"-aa", "-f", "-tc", "trustore", "-url", "https://www.something.com"}));
		assertEquals("Stdout empty", 0, ConsoleOutputCapturer.getInstance().getLines().length) ;
		assertEquals("Stderr empty", 0, ConsoleOutputCapturer.getInstance().getErrLines().length) ;
	}
	
	@Test
	public void testForcedWithCheck() {
		ConsoleOutputCapturer.getInstance().capture();
		Certimport importer = new Certimport();
		// -al is exclusive to -c
		assertNull("NUll means parsing failure",  importer.parseCmdArgs(new String[] {"-f", "-c", "-tc", "trustore", "-url", "https://www.something.com"}));

		// this is program post validation so it is printed to stderr without help
		assertEquals("Error message: ", 
				"Forced options is not aplicable to check operation!",
				ConsoleOutputCapturer.getInstance().getErrLines()[0]);
		
		assertEquals("Stdout is empty", 0, ConsoleOutputCapturer.getInstance().getLines().length) ;
	}
}
