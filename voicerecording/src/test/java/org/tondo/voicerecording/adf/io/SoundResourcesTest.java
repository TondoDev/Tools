package org.tondo.voicerecording.adf.io;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.junit.Test;

public class SoundResourcesTest {
	
	
	@Test
	public void testMixerInfo(){
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		
		for (Mixer.Info mi : mixerInfo) {
			System.out.println("" + mi.getClass().getCanonicalName()+ " - " + mi);
		}
	}

}
