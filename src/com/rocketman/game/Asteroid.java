package com.rocketman.game;

import com.rocketman.math.Vector2;

import java.awt.*;
import java.util.function.Consumer;

public class Asteroid {

    private Vector2 pos;
    private Vector2 speedDir;

    private double rotation;

    private Consumer<Asteroid> onDelete;

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
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        if(pos.x() <= -200 || pos.x() >= size.width+200 || pos.y() <= -200 || pos.y() >= size.height+200) {
            onDelete.accept(this);
        }
        pos.add(speedDir);
    }

    public double getRotation() { return rotation; }

}
