package com.rocketman.game;

import com.rocketman.math.Vector2;

import java.awt.*;
import java.util.function.Consumer;

public class Bullet {

    private Vector2 pos;
    private Vector2 dir;
    private double rot;
    private Consumer<Bullet> onDelete;

    public Bullet(Vector2 _position, Vector2 _dir, double _rot, Consumer<Bullet> _onDelete) {
        pos = _position;
        dir = _dir;
        rot = _rot;
        onDelete = _onDelete;
    }

    public Vector2 getPos() { return pos; }
    public Vector2 getDirection() { return dir; }
    public double getRot() { return rot; }

    public void move() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        if(pos.x() <= -200 || pos.x() >= size.width+200 || pos.y() <= -200 || pos.y() >= size.height+200) {
            onDelete.accept(this);
        }
        pos.add(dir);
    }

    public void collision() {
        onDelete.accept(this);
    }

}
