package com.rocketman.game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiConsumer;

public class AudioManager {

    private static AudioManager instance;
    private static HashMap<String, AudioInputStream> audioStreams = new HashMap<>();
    private static HashMap<String, Clip> audioClips = new HashMap<>();

    private AudioManager() {
        if(!grabAudio()) {
            return;
        }

        System.getLogger("AudioManager").log(System.Logger.Level.INFO, "Audio Loaded");
        instance = this;
    }

    private boolean grabAudio() {
        HashMap<String, String> initializer = new HashMap<>();
        HashMap<String, Double> gains = new HashMap<>();
        HashSet<String> loopsAutoStart = new HashSet<>();

        initializer.put("theme", "audio/soundtrack.wav");
        gains.put("theme", .85);
        loopsAutoStart.add("theme");

        initializer.put("asteroid_destroy", "audio/asteroid_explosion.wav");
        gains.put("asteroid_destroy", .88);

        initializer.put("blaster", "audio/blaster.wav");
        gains.put("blaster", .63);

        initializer.put("score_multiple", "audio/score_multiple.wav");
        gains.put("score_multiple", .65);

        initializer.put("near_death", "audio/near_death.wav");
        gains.put("near_death", .85);


        for(String k : initializer.keySet()) {
            try {
                URL url = ClassLoader.getSystemResource(initializer.get(k));
                audioStreams.put(k, AudioSystem.getAudioInputStream(url));

                Clip c = AudioSystem.getClip();
                c.open(audioStreams.get(k));

                if(gains.containsKey(k)) {
                    FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                    float range = gain.getMaximum() - gain.getMinimum();
                    gain.setValue((float) ((range * gains.get(k)) + gain.getMinimum()));
                }

                if(loopsAutoStart.contains(k)) { c.loop(Clip.LOOP_CONTINUOUSLY); }
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
            c.setFramePosition(0);
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

    public static void stopAll() {
        audioClips.values().forEach(n -> {n.stop(); n.setMicrosecondPosition(0);});
    }

}
