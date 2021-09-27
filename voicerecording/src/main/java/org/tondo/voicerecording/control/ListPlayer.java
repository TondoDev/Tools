package org.tondo.voicerecording.control;

import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.audio.AdfStreamer;
import org.tondo.voicerecording.audio.AdfStreamer.Sequence;
import org.tondo.voicerecording.audio.SoundPlayer;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * 
 * @author TondoDev
 * 
 * 
 *
 */
public class ListPlayer {
	
	private SoundPlayer player;
	private AdfStreamer streamer;
	
	
	public ListPlayer(MainContext context, SoundPlayer player, int startIndex) {
		this.player = player;
		
		Sequence sequence = AdfStreamer.createSequence()
				.silence(400)
				.destination()
				.silence(400)
				.source()
				.silence(400)
				.destination()
				.silence(400);
		
		List<AdfEntry> entries = context.getAdfFile().getEntries();
		this.streamer = new AdfStreamer(context.getAdfFile().getHeader().getAudioFormat());
		this.streamer.initPlayback(sequence, 
				entries, 
				true, // loop
				entries.get(startIndex));
	}
	
	
	
	public void play() {
		// run thread with player 
		Thread playbackThread = new Thread(new PlaybackTask(this.player, this.streamer));
		playbackThread.start();
	
		
		// modal dialog blocking the rest of UI
		Alert a = new Alert(AlertType.NONE);
		a.setContentText("Playing");
		a.getButtonTypes().clear();
		a.getButtonTypes().add(ButtonType.CANCEL);
		a.showAndWait();
		
		// modal dialog is finished, so terminate playing thread
		this.player.stopPlayback();
		try {
			playbackThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	private static class PlaybackTask implements Runnable {
		
		private SoundPlayer player;
		private AdfStreamer streamer;
		
		public PlaybackTask(SoundPlayer player, AdfStreamer streamer) {
			this.player = player;
			this.streamer = streamer;
		}

		@Override
		public void run() {
			try {
				player.play(streamer);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
		
	}
}


