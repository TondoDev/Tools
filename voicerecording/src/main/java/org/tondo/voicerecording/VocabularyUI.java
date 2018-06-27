package org.tondo.voicerecording;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tondo.voicerecording.adf.AdfEntry;
import org.tondo.voicerecording.audio.AdfStreamer;
import org.tondo.voicerecording.audio.AdfStreamer.Sequence;
import org.tondo.voicerecording.audio.FfmpegMp3Convertor;
import org.tondo.voicerecording.audio.SoundPlayer;
import org.tondo.voicerecording.audio.SoundRecorder;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VocabularyUI extends Application {
	
	public static void main(String[] args) {
		// blocking
		launch(args);
		System.out.println("Ahoj: " + Thread.currentThread().getId());
	}
	
	
	private RadioButton rbSrc;
	private AdfEntry adfEntry = new AdfEntry();
	
	


	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Vocabulary");
		
		Button btn = new Button();
        btn.setText("Say 'Hello World'");
        
        
        SoundRecorder recorder = new SoundRecorder(Voicerecording.getAudioFormat());
        SoundPlayer player = new SoundPlayer(Voicerecording.getAudioFormat());
        btn.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				System.out.println("Pressed down");
				recorder.start();
			}
		});
      
        btn.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				recorder.stop();
				setBuffer(recorder.getRecordedData());
			}
        });
        
        Button save = new Button("Save");
        save.setOnAction(e -> {
        	//byte[] data = recorder.getRecordedData();
        	byte[] data = getBuffer();
        	if (data == null) {
        		System.out.println("No data recorded!");
        	} else {
        		AudioFormat format = Voicerecording.getAudioFormat();
        		try ( AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length/format.getFrameSize())) {
        			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("outputs/ulozene.wav"));
        		} catch (IOException ex) {
        			ex.printStackTrace();
        			throw new IllegalStateException("Error during save", ex);
        		}
        	}
        });
        
        Button play = new Button("Play");
        play.setOnAction(e -> {
//        	EventHandler<ActionEvent> evtHandler = (x) -> {x.consume(); System.out.println("consumed");};
//        	play.addEventFilter(ActionEvent.ACTION, evtHandler);
        	System.out.println("From BTN play: " + Thread.currentThread().getId() + " clicks: " + e.getEventType());
        	byte[] data = getBuffer();
        	if (data == null) {
        		System.out.println("No data recorded!");
        	} else if(player.isActive()) {
        		System.out.println("Already playing!");
        	} else {
        		player.play(data);
        	}
        	
        	//play.removeEventFilter(ActionEvent.ACTION, evtHandler);
        });
        
        this.rbSrc = new RadioButton("Source");
        RadioButton rbDest = new RadioButton("Destination");
        ToggleGroup toggle = new ToggleGroup();
        rbSrc.setToggleGroup(toggle);
        rbDest.setToggleGroup(toggle);
        rbSrc.setSelected(true);
        
        
        Button playAll = new Button("Play all");
        playAll.setOnAction(e -> {
        	
        	AdfStreamer streamer = prepareStream();
        	
        	try {
				player.play(streamer);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        });
        
        Button convert = new Button("Convert");
        convert.setOnAction(e -> {
        	AdfStreamer streamer = prepareStream();
        	
        	FfmpegMp3Convertor convertor = new FfmpegMp3Convertor("bin/ffmpeg/bin/ffmpeg.exe");
        	convertor.convert(streamer, "outputs/ulozene.mp3");
        	
        });
        
		
        VBox root = new VBox();
        root.getChildren().addAll( btn, save, play, rbSrc, rbDest, playAll, convert);
        
        primaryStage.setScene(new Scene(root, 100, 150));
        
        
		System.out.println("UI: " +  + Thread.currentThread().getId());
		primaryStage.show();
	}
	
	private void setBuffer(byte [] data) {
		if (this.rbSrc.isSelected()) {
			this.adfEntry.setSrcSoundRaw(data);
		} else {
			this.adfEntry.setDestSoundRaw(data);
		}
	}
	
	private byte[] getBuffer() {
		if (this.rbSrc.isSelected()) {
			return this.adfEntry.getSrcSoundRaw();
		} else {
			return this.adfEntry.getDestSoundRaw();
		}
	}
	
	private AdfStreamer prepareStream() {
		AdfStreamer streamer = new AdfStreamer(Voicerecording.getAudioFormat());
		Sequence sequence = AdfStreamer.createSequence()
	    		.destination()
	    		.silence(500)
	    		.source()
	    		.silence(500)
	    		.destination();
		streamer.initPlayback(sequence, Arrays.asList(this.adfEntry));
    	return streamer;
	}

}
