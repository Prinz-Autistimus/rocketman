package com.rocketman.game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class AudioManager {

    private static AudioManager instance;
    private static HashMap<String, AudioInputStream> audioStreams = new HashMap<>();
    private static HashMap<String, Clip> audioClips = new HashMap<>();

    private AudioManager() {
        if(!grabAudio()) {
            return;
        }

        audioClips.get("theme").loop(Clip.LOOP_CONTINUOUSLY);

        System.getLogger("AudioManager").log(System.Logger.Level.INFO, "Audio Loaded");
        instance = this;
    }

    private boolean grabAudio() {
        HashMap<String, String> initializer = new HashMap<>();
        HashMap<String, Float> gains = new HashMap<>();

        initializer.put("theme", "audio/soundtrack.wav");

        initializer.put("asteroid_destroy", "audio/asteroid_explosion.wav");
        gains.put("asteroid_destroy", 4f);


        for(String k : initializer.keySet()) {
            try {
                URL url = ClassLoader.getSystemResource(initializer.get(k));
                audioStreams.put(k, AudioSystem.getAudioInputStream(url));

                Clip c = AudioSystem.getClip();
                c.open(audioStreams.get(k));

                if(gains.containsKey(k)) {
                    FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(gains.get(k));
                }
                audioClips.put(k, c);

            }catch(Exception e) {
                JOptionPane.showMessageDialog(null, "Error while loading audiofile: " + initializer.get(k), "Asset Loading Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static void initialize() {
        instance = new AudioManager();
    }

    public static void playSound(String name) {
        Clip c = audioClips.get(name);
        if(c != null) {
            c.setMicrosecondPosition(0);
            c.start();
        }
    }

    public static void stopSound(String name) {
        Clip c = audioClips.get(name);
        if(c != null && c.isActive()) {
            c.stop();
            c.setMicrosecondPosition(0);
        }
    }

}
