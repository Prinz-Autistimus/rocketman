package com.rocketman.game;

import com.rocketman.gui.GuiClass;
import com.rocketman.math.Vector2;

import java.awt.*;
import java.util.Vector;

public class GameManager {

    private GuiClass gui;
    private Player player;
    private Vector<Asteroid> asteroids;
    private Vector<Bullet> bullets;
    private boolean updaterRunning = false;
    private boolean interfaceRunning = false;

    private long lastAsteroidGeneratedTime = 0;
    private long initialPauseTime = 5000;
    private long asteroidPauseTime = initialPauseTime;

    public GameManager(){

        AudioManager.initialize();
        AudioManager.playSound("theme");

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        player = new Player(new Vector2(d.getWidth()/2 - 50, d.getHeight()/2 - 50), this::onPlayerDeath);
        asteroids = new Vector<>();
        bullets = new Vector<>();

        gui = new GuiClass(1920, 1080, player, asteroids, bullets, this::stopUpdater, this::onRotationInput, this::onAccelerationInput, this::onShootInput);

        startUpdater();
        startInterface();
        gui.show();
    }

    private void reset() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        asteroidPauseTime = initialPauseTime;
        lastAsteroidGeneratedTime = System.currentTimeMillis();
        bullets.clear();
        asteroids.clear();
        player.getPosition().set(d.getWidth()/2 - 50, d.getHeight()/2 - 50);
        player.setHealth(1);
        player.resetScore();
        AudioManager.stopAll();
        AudioManager.playSound("theme");
    }

    private void startUpdater() {
        updaterRunning = true;

        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Starting Updater");
        updaterInstance().start();
    }

    private void startInterface() {
        interfaceRunning = true;

        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Starting Interface");
        interfaceUpdaterInstance().start();
    }

    private void stopUpdater() {
        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Stopping Updater");
        stopInterface();
        updaterRunning = false;
    }

    private void stopInterface() {
        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Stopping Interface");
        interfaceRunning = false;
    }

    private void onRotationInput(int dir) {
        player.accelerateRotation(dir);
    }

    private void onAccelerationInput() {
        player.accelerate();
    }

    private void onShootInput() {
        if(!player.canShoot()) { return; }
        bullets.add(new Bullet(player.getPosition().copy().add(50-15, 50-15), player.getDirection().scaled(1/player.getDirection().len()).scaled(20), player.getRotation(), this::onBulletDelete));
        player.shoot();
        AudioManager.playSound("blaster");
    }

    private void checkRocketCollision() {
        for(int i = 0; i < asteroids.size(); ++i) {
            Asteroid a = asteroids.get(i);
            double dist = a.getPos().copy().add(25, 25).dist(player.getPosition().copy().add(50, 50));
            if(dist < 50) {
                a.collision();
                if(!player.isImmune()) {
                    player.setHealth(player.getHealth()-.05);
                    player.increaseScore();
                    asteroidPauseTime = Math.max(asteroidPauseTime-50, 500);
                }
                player.setImmune();
                AudioManager.playSound("asteroid_destroy");
            }
        }
    }

    private void checkBulletCollision() {
        for(int i = 0; i < bullets.size(); ++i) {
            Bullet b = bullets.get(i);

            if(asteroids.size() == 0) { return; }

            Asteroid a = asteroids.get(0);
            Asteroid nearestAsteroid = asteroids.get(0);
            double nearestDistance = b.getPos().copy().add(15, 15).dist(a.getPos().copy().add(25, 25));

            for(int j = 1; j < asteroids.size(); ++j) {
                a = asteroids.get(j);
                double nextDistance = b.getPos().copy().add(15, 15).dist(a.getPos().copy().add(25, 25));
                if(nextDistance < nearestDistance) {
                    nearestDistance = nextDistance;
                    nearestAsteroid = a;
                }
            }

            if(nearestDistance < 50) {
                b.collision();
                nearestAsteroid.collision();
                player.increaseScore();
                asteroidPauseTime = Math.max(asteroidPauseTime-50, 500);
                AudioManager.playSound("asteroid_destroy");
            }
        }
    }

    private void onAsteroidDelete(Asteroid a) {
        asteroids.remove(a);
    }

    private void onBulletDelete(Bullet b) {
        bullets.remove(b);
    }

    private void onPlayerDeath() {
        reset();
    }

    private void tryGenerateAsteroid() {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAsteroidGeneratedTime >= asteroidPauseTime) {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

            Vector2 spawnPos = generateRandomStartPosition();
            double xSpeed = (d.getWidth()/2  - spawnPos.x()) * .002 + (((Math.random()*2)-1)*.4);
            double ySpeed = (d.getHeight()/2 - spawnPos.y()) * .002 + (((Math.random()*2)-1)*.4);
            Vector2 dir = new Vector2(xSpeed, ySpeed);
            double rotation = Math.random() * 2 * Math.PI;

            asteroids.add(new Asteroid(spawnPos, dir, rotation, this::onAsteroidDelete));
            lastAsteroidGeneratedTime = currentTime;
        }
    }

    private Vector2 generateRandomStartPosition() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        double x = Math.random() * (d.getWidth() + 200) - 100;
        double y = Math.random() * (d.getHeight() + 200) - 100;

        if(!(x <= -20 || x >= d.getWidth()+20 || y <= -20 || y >= d.getHeight()+20)) {
            double f = Math.random();
            if(f > .5) {
                double distance = x - d.getWidth()/2;
                x = distance > 0 ? d.getWidth()+20 : -20;
            }else {
                double distance = y-d.getHeight()/2;
                y = distance > 0 ? d.getHeight()+20 : -20;
            }
        }

        return new Vector2(x, y);
    }

    private Thread updaterInstance() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                System.getLogger("GameManager").log(System.Logger.Level.INFO, "Updater Started");

                while(updaterRunning) {
                    player.updateRotation();
                    player.updatePosition();

                    for(int i = 0; i < asteroids.size(); ++i) {
                        asteroids.get(i).move();
                    }

                    for(int i = 0; i < bullets.size(); ++i) {
                        bullets.get(i).move();
                    }

                    checkRocketCollision();
                    checkBulletCollision();
                    tryGenerateAsteroid();

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.getLogger("GameManager").log(System.Logger.Level.INFO, "Updater Stopped");
            }
        });
    }

    private Thread interfaceUpdaterInstance() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while(interfaceRunning) {
                    gui.repaint();
                    try {
                        Thread.sleep(17);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

}
