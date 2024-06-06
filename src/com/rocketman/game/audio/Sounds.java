package com.rocketman.game.audio;

public enum Sounds {

    THEME("theme", "audio/soundtrack.wav", .84, true, false),
    ASTEROID_DESTROY("asteroid_destroy", "audio/asteroid_explosion.wav", .88),
    SHIP_BOOST("ship_boost", "audio/ship_boost.wav", .62),
    SHIP_DESTROY("ship_destroy", "audio/ship_destroy.wav", .88),
    BLASTER("blaster", "audio/blaster.wav", .63, false, true),
    SCORE_MULTIPLE("score_multiple", "audio/score_multiple.wav", .66),
    NEAR_DEATH("near_death", "audio/near_death.wav", .85),
    LOOSE_1("loose1", "audio/loose_1.wav", .86),
    LOOSE_2("loose2", "audio/loose_2.wav", .83);

    private String key;
    private String path;
    private double gain;
    private boolean loop;
    private boolean overridable;

    Sounds(String _key, String _path, double _gain) {
        this(_key, _path, _gain, false, false);
    }

    Sounds(String _key, String _path, double _gain, boolean _loop, boolean _overridable) {
        key = _key;
        path = _path;
        gain = _gain;
        loop = _loop;
        overridable = _overridable;
    }

    public String getKey() { return key; }

    public String getPath() { return path; }

    public double getGain() { return gain; }

    public boolean isLoop() { return loop; }

    public boolean isOverridable() { return overridable; }

    public static Sounds getByName(String name) {
        for(Sounds s : values()) {
            if(s.getKey().equals(name)) { return s; }
        }
        return null;
    }
}
