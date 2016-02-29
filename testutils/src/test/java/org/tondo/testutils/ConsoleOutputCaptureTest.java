package org.tondo.testutils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tondo.testutils.ConsoleOutputCapturer.CapturingOption;

/**
 * 
 * @author TondoDev
 * 
 * Basic test of console output capturing capturing
 *
 */
public class ConsoleOutputCaptureTest {

	private static ConsoleOutputCapturer capturer;
	
	@BeforeClass
	public static void createCapturer() {
		capturer = ConsoleOutputCapturer.getInstance();
	}
	
	@Test
	public void testCaptureStdOut() {
		System.out.println("Hallo!");
		assertArrayEquals("Capturing was not started", new String[] {}, capturer.getLines());
		
		// let's get party started
		capturer.capture();
		System.out.println("Friday! Friday!");
		System.out.println();
		System.out.println("Love");
		capturer.stopCapturing();
		
		// this should not be captured, because we dont like mondays
		System.out.println("Monday");
		assertArrayEquals("Captured lines exept of monday", 
				new String[] {"Friday! Friday!", "", "Love"}, capturer.getLines());
		
		assertArrayEquals("Stderr was not touched", new String[] {}, capturer.getErrLines());
		
		capturer.capture();
		// when capturing is not stopped lines are appended
		System.out.println("I am back");
		assertArrayEquals(new String[]{"I am back"}, capturer.getLines());
		System.out.println("Again");
		assertArrayEquals(new String[]{"I am back", "Again"}, capturer.getLines());
		
		capturer.capture();
		assertArrayEquals("Capturing already captured has no effect", new String[]{"I am back", "Again"}, capturer.getLines());
		
		// but stop and start again erase previous buffer
		capturer.stopCapturing();
		capturer.capture();
		assertArrayEquals("Buffere is empty", new String[] {}, capturer.getLines());
		
	}
	
	@Test
	public void testCaptureStdErr() {
		System.err.println("Hallo!");
		assertArrayEquals("Capturing stderr was not started", new String[] {}, capturer.getErrLines());
		
		// let's get party started
		capturer.capture();
		System.err.println("Friday! Friday!");
		System.err.println();
		System.err.println("Love");
		capturer.stopCapturing();
		
		// this should not be captured, because we dont like mondays
		System.err.println("Monday");
		assertArrayEquals("Captured lines exept of monday", 
				new String[] {"Friday! Friday!", "", "Love"}, capturer.getErrLines());
		
		assertArrayEquals("Stdout was not touched", new String[] {}, capturer.getLines());
		
		capturer.capture();
		// when capturing is not stopped lines are appended
		System.err.println("I am back");
		assertArrayEquals(new String[]{"I am back"}, capturer.getErrLines());
		System.err.println("Again");
		assertArrayEquals(new String[]{"I am back", "Again"}, capturer.getErrLines());
		
		capturer.capture();
		assertArrayEquals("Capturing already captured has no effect", new String[]{"I am back", "Again"}, capturer.getErrLines());
	}
	
	@Test
	public void testPartialCaptureSwitch() {
		capturer.capture(CapturingOption.STDOUT);
		System.out.println("First STDOUT");
		System.err.println("First STDERR");
		
		assertArrayEquals("First Stdout captured", new String[] {"First STDOUT"}, capturer.getLines());
		assertArrayEquals("First Stderr captured", new String[] {}, capturer.getErrLines());
		
		capturer.capture(CapturingOption.STDERR);
		System.out.println("Second STDOUT");
		System.err.println("Second STDERR");
		
		assertArrayEquals("Second Stdout captured", new String[] {"First STDOUT", "Second STDOUT"}, capturer.getLines());
		assertArrayEquals("Second Stderr captured", new String[] {"Second STDERR"}, capturer.getErrLines());
		
		capturer.stopCapturing(CapturingOption.STDOUT);
		System.out.println("Third STDOUT");
		System.err.println("Third STDERR");
		
		assertArrayEquals("Third Stdout captured", new String[] {"First STDOUT", "Second STDOUT"}, capturer.getLines());
		assertArrayEquals("Third Stderr captured", new String[] {"Second STDERR", "Third STDERR"}, capturer.getErrLines());
		
		capturer.stopCapturing(CapturingOption.STDERR);
		System.out.println("Fourth STDOUT");
		System.err.println("Fourth STDERR");
		// nothing was added
		assertArrayEquals("Fourth Stdout captured", new String[] {"First STDOUT", "Second STDOUT"}, capturer.getLines());
		assertArrayEquals("Fourth Stderr captured", new String[] {"Second STDERR", "Third STDERR"}, capturer.getErrLines());
		
		capturer.capture(CapturingOption.BOTH);
		System.out.println("Fifth STDOUT");
		System.err.println("Fifth STDERR");
		
		// Buffers reinitialized
		assertArrayEquals("Fifth Stdout captured", new String[] {"Fifth STDOUT"}, capturer.getLines());
		assertArrayEquals("Fifth Stderr captured", new String[] {"Fifth STDERR"}, capturer.getErrLines());
		
		capturer.stopCapturing(CapturingOption.STDERR);
		capturer.capture(CapturingOption.STDERR);
		System.out.println("Seventh STDOUT");
		// only stderr was reset
		assertArrayEquals("Stdout captured", new String[] {"Fifth STDOUT", "Seventh STDOUT"}, capturer.getLines());
		assertArrayEquals("Stderr captured", new String[] {}, capturer.getErrLines());
	}
	
	@Test
	public void testCaptureBoth() {
		capturer.capture();
		System.out.println("Today");
		System.err.println("is");
		System.out.println("very");
		System.err.println("nice");
		System.out.println("day");
		capturer.stopCapturing();
		
		assertArrayEquals(new String[] {"Today", "very", "day"}, capturer.getLines());
		assertArrayEquals(new String[] {"is", "nice"}, capturer.getErrLines());
	}
	
	@Before
	public void reset() {
		capturer.capture();
		capturer.stopCapturing();
	}
}

