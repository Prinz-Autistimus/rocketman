package com.rocketman.game.objects;

import com.rocketman.game.audio.AudioManager;
import com.rocketman.game.audio.Sounds;
import com.rocketman.game.logic.GameManager;
import com.rocketman.math.Vector2;

public class Player {

    //--Settings---------------------------------------------------
    private static final int SHOT_DELAY = 100;

    private static final int IMMUNE_TIME = 2500;

    private static final double LOW_HEALTH = .2;
    private static final double DEATH_LIMIT = .001;
    public static final int SIZE = 100;
    public static final int HEALTHBAR_SIZE = 50;

    //--Attributes-------------------------------------------------

    //Position
    private Vector2 pos;
    private Vector2 speedDir = new Vector2(0, 0);
    private static final double posAcc = 0.034;
    private boolean isAccelerating = false;

    //Rotation
    private double rotation = 0;
    private double rotSpeed = 0;
    private static final double rotAcc = .0015;

    //Game Mechanics
    private static final int INITIAL_AMMO = 8;
    private int ammo = INITIAL_AMMO;
    private int score = 0;
    private double health = 1;
    private boolean immune = false;
    private boolean canShoot = true;

    //Events
    private final Runnable onPlayerDeath;

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

    public void accelerateRotation(int dir) {
        rotSpeed += rotAcc * dir;
    }

    public void updateRotation() {
        rotation += rotSpeed;
    }

    public void move(Vector2 dist) {
        pos.add(dist);
    }

    public Vector2 getDirection() {
        return new Vector2(0, -1).rotate(rotation);
    }

    public void updatePosition() {
        if(isOutOfBoundsX()) { speedDir.setX(speedDir.x()* (-1)); }
        if(isOutOfBoundsY()) { speedDir.setY(speedDir.y()* (-1)); }
        move(speedDir);
    }

    private boolean isOutOfBoundsX() {
        return pos.x() <= 0 || pos.x() >= GameManager.SCREEN_SIZE.getWidth();
    }

    private boolean isOutOfBoundsY() {
        return pos.y() <= 0 || pos.y() >= GameManager.SCREEN_SIZE.getHeight();
    }
    public boolean canShoot() { return canShoot && ammo > 0; }

    public void shoot() {
        if(!canShoot) { return; }
        canShoot = false;
        --ammo;
        new Thread(() -> {
            try {
                Thread.sleep(SHOT_DELAY);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            canShoot = true;
        }).start();
    }

    public boolean isImmune() { return immune; }

    public void setImmune() {
        if(immune) { return; }

        immune = true;
        new Thread(() -> {
            try {
                Thread.sleep(IMMUNE_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            immune = false;
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
        if(health <= LOW_HEALTH) {
            AudioManager.playSound(Sounds.NEAR_DEATH);
        }
        if(isDead()) {
            speedDir = new Vector2(0, 0);
            AudioManager.stopSound(Sounds.NEAR_DEATH);
            onPlayerDeath.run();
        }
    }

    public void increaseScore() {
        ++score;
        resetAmmo();
        if(score % 10 == 0) {
            AudioManager.playSound(Sounds.SCORE_MULTIPLE);
        }
    }

    public void resetScore() {
        score = 0;
    }

    private void resetAmmo() {
        ammo = INITIAL_AMMO;
    }

    public void resetRotationSpeed() { rotation = 0; }

    public int getAmmo() { return ammo; }
    public int getMaxAmmo() { return INITIAL_AMMO; }

    public int getScore() { return score; }

    public boolean isDead() {
        return health<=DEATH_LIMIT;
    }

}
