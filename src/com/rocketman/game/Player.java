package com.rocketman.game;

import com.rocketman.math.Vector2;

import java.awt.*;

public class Player {

    private Vector2 pos;
    private Vector2 speedDir = new Vector2(0, 0);
    private double posAcc = 0.034;
    private boolean isAccelerating = false;

    private double rotation = 0;
    private double rotSpeed = 0;
    private double rotAcc = .0015;

    private double health = 1;

    private boolean immune = false;
    private boolean canShoot = true;
    private int score = 0;
    private int initialAmmo = 8;
    private int ammo = initialAmmo;

    private Runnable onPlayerDeath;

    public Player(Runnable _onPlayerDeath) {
        this(new Vector2(), _onPlayerDeath);
    }
    public Player(Vector2 _pos, Runnable _onPlayerDeath) {
        pos = _pos;
        onPlayerDeath = _onPlayerDeath;
    }

    public Vector2 getPosition() {
        return pos;
    }


    public double getRotation() {
        return rotation;
    }

    public void setRotation(double _rotation) {
        rotation = _rotation % 360;
    }

    public void accelerateRotation(int dir) {
        rotSpeed += rotAcc * dir;
    }

    public void updateRotation() {
        rotation += rotSpeed;
    }

    public void setPosition(Vector2 newPos) {
        pos.set(newPos);
    }

    public void move(double distX, double distY) {
        pos.add(distX, distY);
    }

    public void move(Vector2 dist) {
        pos.add(dist);
    }

    public Vector2 getDirection() {
        return new Vector2(0, -1).rotate(rotation);
    }

    public void updatePosition() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        if(pos.x() <= 0 || pos.x() >= size.width) {
            speedDir.setX(speedDir.x()* (-1));
        }
        if(pos.y() <= 0 || pos.y() >= size.height) {
            speedDir.setY(speedDir.y()* (-1));
        }
        move(speedDir);
    }
    public boolean canShoot() { return canShoot && ammo > 0; }

    public void shoot() {
        if(!canShoot) { return; }
        canShoot = false;
        --ammo;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                canShoot = true;
            }
        }).start();
    }

    public boolean isImmune() { return immune; }

    public void setImmune() {
        if(immune) { return; }

        immune = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                immune = false;
            }
        }).start();
    }

    public void accelerate() {
        speedDir.add(getDirection().scaled(posAcc));
        isAccelerating = true;
    }

    public boolean isAccelerating() { return isAccelerating; }
    public void resetAcceleration() { isAccelerating = false; }

    public double getHealth() { return health; }

    public void setHealth(double x) {
        health = Math.max(0, Math.min(1, x));
        if(health <= .2) {
            AudioManager.playSound("near_death");
        }
        if(health <= .001) {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            speedDir = new Vector2(0, 0);
            pos = new Vector2(d.getWidth()/2, d.getHeight()/2);
            AudioManager.stopSound("near_death");
            onPlayerDeath.run();
        }


    }

    public void increaseScore() {
        ++score;
        resetAmmo();
        if(score % 10 == 0) {
            AudioManager.playSound("score_multiple");
        }
    }

    public void resetScore() {
        score = 0;
    }

    private void resetAmmo() {
        ammo = initialAmmo;
    }

    public int getAmmo() { return ammo; }
    public int getMaxAmmo() { return initialAmmo; }

    public int getScore() { return score; }

}
