package com.rocketman.game;

import com.rocketman.math.Vector2;
import java.util.function.Consumer;

public class Bullet {

    private final Vector2 pos;
    private final Vector2 dir;
    private final double rot;
    private final Consumer<Bullet> onDelete;

    public Bullet(Vector2 _position, Vector2 _dir, double _rot, Consumer<Bullet> _onDelete) {
        pos = _position;
        dir = _dir;
        rot = _rot;
        onDelete = _onDelete;
    }

    public Vector2 getPos() { return pos; }
    public double getRot() { return rot; }

    public void move() {
        if(isOutOfBounds()) { onDelete.accept(this); }
        pos.add(dir);
    }

    private boolean isOutOfBounds() {
        return pos.x() <= -200 || pos.x() >= GameManager.screenSize.width+200 || pos.y() <= -200 || pos.y() >= GameManager.screenSize.height+200;
    }

    public void collision() {
        onDelete.accept(this);
    }

}
