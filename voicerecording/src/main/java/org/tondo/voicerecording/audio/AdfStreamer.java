package org.tondo.voicerecording.audio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;

import org.tondo.voicerecording.adf.AdfEntry;

public class AdfStreamer {
	
	private static final Pattern SILENCE_PARSER = Pattern.compile("^SIL:([0-9]+)$");
	
	public static class Sequence {
		
		private List<String> steps = new ArrayList<>();
		
		public Sequence destination() {
			steps.add("DEST");
			return this;
		}
		
		public Sequence source() {
			steps.add("SRC");
			return this;
		}
		
		public Sequence silence(int durationInMS) {
			steps.add("SIL:"+durationInMS);
			return this;
		}
		
		private List<String> getSteps() {
			return this.steps;
		}
		
	}
	
	private SilenceGenerator silenceGen;
	private List<Supplier<byte[]>> commands;
	private List<AdfEntry> entries;
	private AdfEntry context;
	private AudioFormat format;
	private boolean inLoop;
	
	public AdfStreamer(AudioFormat format) {
		this.format = format;
		this.silenceGen = new SilenceGenerator(format);
	}
	
	public AudioFormat getAudioFormat() {
		return this.format;
	}
	
	public static Sequence createSequence() {
		return new Sequence();
	}
	
	/**
	 * 
	 * @param sequence if null nothing is streamed
	 * @param entries
	 */
	public void initPlayback(Sequence sequence, List<AdfEntry> entries) {
		
		this.context = null;
		this.processedBuffer = null;
		this.processedBufferOffset = -1;
		
		this.entries = entries;
		this.entryIter = this.entries.iterator();
		
		this.prepareCommands(sequence);
	}
	
	public void initPlayback(Sequence sequence, List<AdfEntry> entries, boolean loop, AdfEntry initialEntry ) {
		
		this.context = null;
		this.processedBuffer = null;
		this.processedBufferOffset = -1;
		
		this.entries = entries;
		this.entryIter = initIterator(entries, initialEntry);
		
		this.inLoop = loop;
		
		this.prepareCommands(sequence);
	}
	
	private Iterator<AdfEntry> initIterator(List<AdfEntry> entries, AdfEntry initialEntry) {
		Iterator<AdfEntry> iter = entries.iterator();
		if (initialEntry == null) {
			return iter;
		}
		
		// how many entries to skip
		int skips = 0;
		for (AdfEntry next : entries) {
			// initial entry is expected from the list, so checking references is OK
			if (next == initialEntry) {
				break;
			}
			skips++;
		}
		
		// entries not from list (should never happen) then beginning of the list will be used 
		if (skips == entries.size()) {
			return iter;
		}
		
		for (int i = 0; i < skips; i++) {
			iter.next();
		}
		
		return  iter;
	}
	
	
	private void prepareCommands(Sequence sequence) {
		this.commands = new ArrayList<>();
		this.commandIter = null;
		// for null sequence nothing will be streamed
		if (sequence == null) {
			return;
		}
		
		for (String code : sequence.getSteps()) {
			Matcher matcher = SILENCE_PARSER.matcher(code);
			if (matcher.find()) {
				int ms = Integer.parseInt(matcher.group(1));
				commands.add(()->  this.silenceGen.generateSilence(ms));
			}
			else if ("DEST".equals(code)) {
				commands.add(() -> {
					return this.context.getDestSoundRaw();
					});
			} else if ("SRC".equals(code)) {
				commands.add(() -> {
					return this.context.getSrcSoundRaw();
				});
			} else {
				throw new IllegalStateException("Unknown sequence \"" + code + "\"");
			}
		}
	}
	
	
	private int processedBufferOffset = -1;
	private byte[] processedBuffer = null;
	private Iterator<AdfEntry> entryIter;
	private Iterator<Supplier<byte[]>> commandIter;
	
	public int stream(byte[] buffer) {
		
		if (buffer == null || buffer.length == 0) {
			return 0;
		}
		
		int currentLen = 0;
		while (currentLen < buffer.length) {
			if (processedBuffer != null) {
				currentLen += copyFromCurrentBuffer(buffer, currentLen);
			} else if (this.commandIter != null && commandIter.hasNext()) {
				currentLen += copyBuffer(commandIter.next().get(), buffer, currentLen);
			} else if (entryIter.hasNext()) {
				this.context = entryIter.next();
				this.commandIter = this.commands.iterator();
			} else if (this.inLoop) {
				this.entryIter = this.entries.iterator();
			}
			else {
				// end of stream no other data available
				return currentLen > 0 ? currentLen : -1;
			}
		}
		
		return currentLen;
	}
	
	
	private int copyBuffer(byte[] src, byte[] target, int offsetInTarget) {
		if (src == null) {
			return 0;
		}
		int rem = target.length - offsetInTarget;
		if (rem >= src.length) {
			System.arraycopy(src, 0, target, offsetInTarget, src.length);
			return src.length;
		} else {
			System.arraycopy(src, 0, target, offsetInTarget, rem);
			this.processedBufferOffset = rem;
			this.processedBuffer = src;
			return rem;
		}
	}
	
	
	private int copyFromCurrentBuffer(byte[] target, int offsetInTarget) {
		int rem = target.length - offsetInTarget;
		int sizeToProcess = this.processedBuffer.length - this.processedBufferOffset;
		if (rem >=  sizeToProcess) {
			System.arraycopy(this.processedBuffer, this.processedBufferOffset, target, offsetInTarget, sizeToProcess);
			this.processedBuffer = null;
			this.processedBufferOffset = -1;
			return sizeToProcess;
		} else {
			System.arraycopy(this.processedBuffer, this.processedBufferOffset, target, offsetInTarget, rem);
			this.processedBufferOffset += rem;
			return rem;
		}
	}
}
