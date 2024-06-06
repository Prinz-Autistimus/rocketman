package com.rocketman.game.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

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


        for(Sounds k : Sounds.values()) {
            try {
                URL url = ClassLoader.getSystemResource(k.getPath());
                audioStreams.put(k.getKey(), AudioSystem.getAudioInputStream(url));

                Clip c = AudioSystem.getClip();
                c.open(audioStreams.get(k.getKey()));

                FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gain.getMaximum() - gain.getMinimum();
                gain.setValue((float) ((range * k.getGain()) + gain.getMinimum()));


                if(k.isLoop()) { c.loop(Clip.LOOP_CONTINUOUSLY); }
                audioClips.put(k.getKey(), c);

            }catch(Exception e) {
                JOptionPane.showMessageDialog(null, "Error while loading audiofile: " + k.getKey() + " from " + k.getPath(), "Asset Loading Error", JOptionPane.ERROR_MESSAGE);
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
        if(c != null && (!c.isActive() || Sounds.getByName(name).isOverridable())) {
            c.setFramePosition(0);
            c.start();
            System.getLogger("AudioManager").log(System.Logger.Level.INFO, "Playing sound: " + name);
        }
    }

    public static void playSound(Sounds s) {
        playSound(s.getKey());
    }

    public static void stopSound(String name) {
        Clip c = audioClips.get(name);
        if(c != null && c.isActive()) {
            c.stop();
            c.setMicrosecondPosition(0);
            System.getLogger("AudioManager").log(System.Logger.Level.INFO, "Stopping sound: " + name);
        }
    }

    public static void stopSound(Sounds s) {
        stopSound(s.getKey());
    }

    public static void stopAll() {
        audioClips.values().forEach(n -> {n.stop(); n.setFramePosition(0);});
    }

}
