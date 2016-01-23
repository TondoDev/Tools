package org.tondo.testutils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Helper class for test which can capture console standard output to internal buffer.
 * Can be useful when tested classes prints something to System.out for reporting that
 * something was called.
 * 
 * @author TondoDev
 *
 */
public class ConsoleOutputCapturer {
	private final String[] EMPTY_ARRAY = new String[]{};
	
	private PrintStream originlaOutput;
	private ByteArrayOutputStream buffer;
	private PrintStream capturingStream;
	
	public void capture() {
		if (capturingStream == null) {
			// store original print stream
			this.originlaOutput = System.out;
			this.buffer = new ByteArrayOutputStream();
			this.capturingStream = new PrintStream(buffer);
			System.out.flush();
			System.setOut(capturingStream);
		}
	}
	
	public  void stopCapturing() {
		if (capturingStream != null) {
			System.out.flush();
			System.setOut(originlaOutput);
			this.capturingStream = null;
		}
	}
	
	public String[] getLines() {
		if (this.capturingStream != null) {
			this.capturingStream.flush();
		} else if (this.buffer == null) {
			return EMPTY_ARRAY;
		}
		
		String capturedData = this.buffer.toString();
		if (capturedData == null || capturedData.isEmpty()) {
			return EMPTY_ARRAY;
		}
		return capturedData.split(System.getProperty("line.separator"));
	}

}
