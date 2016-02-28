package org.tondo.testutils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Helper class for test which can capture console standard output to internal buffer.
 * Can be useful when tested classes prints something to System.out for reporting that
 * something was called.
 * 
 * This is not thread safe.
 * 
 * @author TondoDev
 *
 */
public class ConsoleOutputCapturer {
	
	// stdout
	private PrintStream originlaOutput;
	private ByteArrayOutputStream buffer;
	private PrintStream capturingStream;
	
	// stderr
	private PrintStream originlaErrOutput;
	private ByteArrayOutputStream errBuffer;
	private PrintStream capturingErrStream;
	
	public void capture() {
		if (capturingStream == null) {
			// store original print stream
			this.originlaOutput = System.out;
			this.buffer = new ByteArrayOutputStream();
			this.capturingStream = new PrintStream(buffer);
			System.out.flush();
			System.setOut(capturingStream);
		}
		
		if (capturingErrStream == null) {
			this.originlaErrOutput = System.err;
			this.errBuffer = new ByteArrayOutputStream();
			this.capturingErrStream = new PrintStream(errBuffer);
			System.err.flush();
			System.setErr(capturingErrStream);
		}
	}
	
	public  void stopCapturing() {
		if (capturingStream != null) {
			System.out.flush();
			System.setOut(originlaOutput);
			this.capturingStream = null;
		}
		
		if (capturingErrStream != null) {
			System.out.flush();
			System.setErr(originlaErrOutput);
			this.capturingErrStream = null;
		}
	}
	
	public String[] getLines() {
		if (this.capturingStream != null) {
			this.capturingStream.flush();
		} else if (this.buffer == null) {
			return new String[]{};
		}
		
		String capturedData = this.buffer.toString();
		if (capturedData == null || capturedData.isEmpty()) {
			return new String[]{};
		}
		return capturedData.split(System.getProperty("line.separator"));
	}
	
	/**
	 * Return captured lines from standerd error output
	 */
	public String[] getErrLines() {
		if (this.capturingErrStream != null) {
			this.capturingErrStream.flush();
		} else if (this.errBuffer == null) {
			return new String[]{};
		}
		
		String capturedData = this.errBuffer.toString();
		if (capturedData == null || capturedData.isEmpty()) {
			return new String[]{};
		}
		return capturedData.split(System.getProperty("line.separator"));
	}

}
