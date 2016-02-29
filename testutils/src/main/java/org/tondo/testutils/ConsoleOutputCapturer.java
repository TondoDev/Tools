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
	/**
	 * Encapsulates common functionality for capturing stdout and std err streams.
	 */
	private static class CaptureBuffer {
		private PrintStream originlOutput;
		private ByteArrayOutputStream buffer;
		private PrintStream capturingStream;
		
		public CaptureBuffer(PrintStream original) {
			if (original == null) {
				throw new IllegalArgumentException("Original stream can't be null!");
			}
			this.originlOutput = original;
		}
		
		public void init() {
			this.buffer = new ByteArrayOutputStream();
			this.capturingStream = new PrintStream(buffer);
			this.originlOutput.flush();
		}
		
		public void stop() {
			this.capturingStream.flush();
			this.capturingStream = null;
			
		}
		
		public boolean isCapturing() {
			return this.capturingStream != null;
		}
		
		public PrintStream getStream() {
			if (this.capturingStream == null) {
				throw new IllegalStateException("CApturing stream is not initialized!");
			}
			
			return this.capturingStream;
		}
		
		public PrintStream getOriginal() {
			return this.originlOutput;
		}
		
		public String[] getData() {
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
	}
	
	
	public static enum CapturingOption {
		STDERR,
		STDOUT,
		BOTH
	}
	
	
	private CaptureBuffer stdOutBuffer;
	private CaptureBuffer stdErrBuffer;
	
	public ConsoleOutputCapturer() {
		this.stdOutBuffer = new CaptureBuffer(System.out);
		this.stdErrBuffer = new CaptureBuffer(System.err);
	}
	
	public void capture() {
		capture(CapturingOption.BOTH);
	}
	
	public void capture(CapturingOption option) {
		if (isOut(option) && !stdOutBuffer.isCapturing()) {
			stdOutBuffer.init();
			System.setOut(stdOutBuffer.getStream());
		}
		
		if (isErr(option) && !stdErrBuffer.isCapturing()) {
			stdErrBuffer.init();
			System.setErr(stdErrBuffer.getStream());
		}
	}
	
	public void stopCapturing() {
		stopCapturing(CapturingOption.BOTH);
	}
	
	public void stopCapturing(CapturingOption option) {
		if (isOut(option) && this.stdOutBuffer.isCapturing()) {
			this.stdOutBuffer.stop();
			System.setOut(this.stdOutBuffer.getOriginal());
		}
		
		if (isErr(option) && this.stdErrBuffer.isCapturing()) {
			this.stdErrBuffer.stop();
			System.setErr(stdErrBuffer.getOriginal());
		}
	}
	
	public String[] getLines() {
		return this.stdOutBuffer.getData();
	}
	
	/**
	 * Return captured lines from standerd error output
	 */
	public String[] getErrLines() {
		return this.stdErrBuffer.getData();
	}
	
	private boolean isOut(CapturingOption opt) {
		return opt == CapturingOption.BOTH || opt == CapturingOption.STDOUT;
	}
	
	private boolean isErr(CapturingOption opt) {
		return  opt == CapturingOption.BOTH || opt == CapturingOption.STDERR;
	}

}
