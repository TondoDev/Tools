package org.tondo.voicerecording.audio;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

public class FfmpegMp3Convertor {
	
	private String ffmpegLocation;
	
	public FfmpegMp3Convertor(String exeLocation) {
		this.ffmpegLocation = exeLocation;
	}
	
	// FFMPEG: cat slovicko.raw | ./ffmpeg.exe -f s16be -ar 22.1K -ac 2 -i - -f mp3 - > pes.mp3
	// ffmpeg -formats - show supported fsample format
	public void convert(AdfStreamer streamer, String outFile) {
		List<String> params = new ArrayList<>(); 
		params.add(this.ffmpegLocation);
		params.add("-y"); // override output file if exists
		params.addAll(createFormatParameters(streamer.getAudioFormat()));
		// input from stdin
		params.add("-i");
		params.add("-");
		// output
		params.add("-f");
		params.add("mp3");
		params.add(outFile);
		
		
		
		ProcessBuilder processBuilder = new ProcessBuilder(params);
		
		System.out.println("Executing: " + params + " in ");
	
		processBuilder.redirectOutput(Redirect.INHERIT);
		processBuilder.redirectError(Redirect.INHERIT);
		try {
			Process process = processBuilder.start();
			
			// from my application point of view is this output, because I am writing to it,
			// but from process point of view is its input
			OutputStream processInput = process.getOutputStream();
			if (process.isAlive()) {
			
				int len = 0;
				byte[] buff = new byte[32000];
				while ((len = streamer.stream(buff)) > 0) {
					processInput.write(buff, 0, len);
				}
			} else {
				System.out.println("Something went wrong!");
			}
			// we must close process input because it will wait forever
			processInput.close();
			System.out.println("Write finish!");
			process.waitFor();
			System.out.println("process finish");
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	protected List<String> createFormatParameters(AudioFormat format) {
		List<String> retVal = new ArrayList<String>();
		
		// sample
		retVal.add("-f");
		String sample = "s" +format.getSampleSizeInBits();
		sample += format.isBigEndian() ? "be" : "le";
		retVal.add(sample);
		
		// channels
		retVal.add("-ac");
		retVal.add(String.valueOf(format.getChannels()));
		
		//rate
		retVal.add("-ar");
		retVal.add(String.valueOf(format.getSampleRate()));
		
		return retVal;
	}
	
	

}
