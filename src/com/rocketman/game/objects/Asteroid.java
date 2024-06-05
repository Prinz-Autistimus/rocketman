package com.rocketman.game.objects;

import com.rocketman.game.logic.GameManager;
import com.rocketman.math.Vector2;
import java.util.function.Consumer;

public class Asteroid {

    //--Settings----------------------------------
    public static final int SIZE = 50;
    public static final int SPAWN_DELAY_DECREASE = 50;
    public static final int MINIMUM_SPAWN_DELAY = 500;

    //--Attributes--------------------------------

    //Position
    private final Vector2 pos;
    private Vector2 speedDir;

    //Rotation
    private final double rotation;

    //Events
    private final Consumer<Asteroid> onDelete;

    public Asteroid(Vector2 _pos, Vector2 _speedDir, double _rotation, Consumer<Asteroid> _onDelete) {
        pos = _pos;
        speedDir = _speedDir;
        onDelete = _onDelete;
        rotation = _rotation;
    }

    public Vector2 getPos() {
        return pos;
    }

    public void setSpeedDir(Vector2 _speed) {
        speedDir =_speed;
    }

    public void collision() {
        setSpeedDir(new Vector2());
        onDelete.accept(this);
    }

    public void move() {
        if(isOutOfBounds()) { onDelete.accept(this); }
        pos.add(speedDir);
    }

    private boolean isOutOfBounds() {
        return pos.x() <= -200 || pos.x() >= GameManager.SCREEN_SIZE.width+200 || pos.y() <= -200 || pos.y() >= GameManager.SCREEN_SIZE.height+200;
    }

    public double getRotation() { return rotation; }

}
