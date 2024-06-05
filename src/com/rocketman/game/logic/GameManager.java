package com.rocketman.game.logic;

import com.rocketman.game.audio.AudioManager;
import com.rocketman.game.audio.Sounds;
import com.rocketman.game.objects.Asteroid;
import com.rocketman.game.objects.Bullet;
import com.rocketman.game.objects.Player;
import com.rocketman.gui.GuiClass;
import com.rocketman.math.Vector2;

import java.awt.*;
import java.util.Vector;

public class GameManager {


    //--Attributes-------------------------------------------------

    //Gui
    private GuiClass gui;
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int WIDTH = 1920, HEIGHT = 1080;

    //Objects
    private Player player;
    private Vector<Asteroid> asteroids;
    private Vector<Bullet> bullets;
    private  Thread currentUpdaterInstance;
    private  Thread currentInterfaceUpdaterInstance;

    //Settings
    private static final int TICK_SPEED = 10;
    private static final int INTERFACE_SPEED = 17;
    private static final long INITIAL_ASTEROID_PAUSE_TIME = 5000;
    private long asteroidPauseTime = INITIAL_ASTEROID_PAUSE_TIME;
    private long lastAsteroidGeneratedTime = 0;
    private boolean updaterRunning = false;
    private boolean interfaceRunning = false;

    public GameManager(){
        start();
    }

    public void start() {
        //initialize AdioManager
        AudioManager.initialize();
        AudioManager.playSound(Sounds.THEME);

        //initialize GameObjects
        player = new Player(new Vector2(SCREEN_SIZE.getWidth()/2 - 50, SCREEN_SIZE.getHeight()/2 - 50), this::onPlayerDeath);
        asteroids = new Vector<>();
        bullets = new Vector<>();

        //initialize GUI
        gui = new GuiClass(WIDTH, HEIGHT, player, asteroids, bullets, this::stopUpdater, this::onRotationInput, this::onAccelerationInput, this::onShootInput);

        //Start Game Logic
        startUpdater();
        startInterface();

        //Open Window
        gui.show();
    }

    public void stop() {
        reset();
        AudioManager.stopAll();
        stopUpdater();
        stopInterface();
        gui.hide();
    }

    private void reset() {
        new Thread(() -> {
            asteroidPauseTime = INITIAL_ASTEROID_PAUSE_TIME;
            lastAsteroidGeneratedTime = System.currentTimeMillis();
            bullets.clear();
            asteroids.clear();
            player.getPosition().set(SCREEN_SIZE.getWidth()/2 - 50, SCREEN_SIZE.getHeight()/2 - 50);
            player.resetRotationSpeed();
            player.setHealth(1);
            stopUpdater();

            try {
                AudioManager.stopAll();
                AudioManager.playSound(Sounds.SHIP_DESTROY);

                Thread.sleep(2000);

                AudioManager.playSound(Sounds.LOOSE_1);
                AudioManager.playSound(Sounds.LOOSE_2);

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            player.resetScore();
            AudioManager.playSound(Sounds.THEME);
            startUpdater();
        }).start();
    }

    private void startUpdater() {
        updaterRunning = true;

        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Starting Updater");
        currentUpdaterInstance = updaterInstance();
        currentUpdaterInstance.start();
    }

    private void startInterface() {
        interfaceRunning = true;

        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Starting Interface");
        currentInterfaceUpdaterInstance = interfaceUpdaterInstance();
        currentInterfaceUpdaterInstance.start();
    }

    private void stopUpdater() {
        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Stopping Updater");
        updaterRunning = false;
        currentUpdaterInstance.stop();
    }

    private void stopInterface() {
        System.getLogger("GameManager").log(System.Logger.Level.INFO, "Stopping Interface");
        interfaceRunning = false;
        currentInterfaceUpdaterInstance.stop();
    }

    private void onRotationInput(int dir) {
        player.accelerateRotation(dir);
    }

    private void onAccelerationInput() {
        player.accelerate();
    }

    private void onShootInput() {
        if(!player.canShoot()) { return; }
        bullets.add(new Bullet(player.getPosition().copy().add(Player.SIZE/2-Bullet.SIZE/2, Player.SIZE/2-Bullet.SIZE/2), player.getDirection().scaled(1/player.getDirection().len()).scaled(Bullet.SPEED), player.getRotation(), this::onBulletDelete));
        player.shoot();
        AudioManager.playSound(Sounds.BLASTER);
    }

    private void checkRocketCollision() {
        for(int i = 0; i < asteroids.size(); ++i) {
            Asteroid a = asteroids.get(i);
            double dist = a.getPos().copy().add(Asteroid.SIZE/2, Asteroid.SIZE/2).dist(player.getPosition().copy().add(Player.SIZE/2, Player.SIZE/2));
            if(dist < 50) {
                a.collision();
                if(!player.isImmune()) {
                    player.setHealth(player.getHealth()-.05);
                    player.increaseScore();
                    asteroidPauseTime = Math.max(asteroidPauseTime-Asteroid.SPAWN_DELAY_DECREASE, Asteroid.MINIMUM_SPAWN_DELAY);
                }
                player.setImmune();
                AudioManager.playSound(Sounds.ASTEROID_DESTROY);
            }
        }
    }

    private void checkBulletCollision() {
        for(int i = 0; i < bullets.size(); ++i) {
            Bullet b = bullets.get(i);

            if(asteroids.size() == 0) { return; }

            Asteroid a = asteroids.get(0);
            Asteroid nearestAsteroid = asteroids.get(0);
            double nearestDistance = b.getPos().copy().add(Bullet.SIZE/2, Bullet.SIZE/2).dist(a.getPos().copy().add(Asteroid.SIZE/2, Asteroid.SIZE/2));

            for(int j = 1; j < asteroids.size(); ++j) {
                a = asteroids.get(j);
                double nextDistance = b.getPos().copy().add(Bullet.SIZE/2, Bullet.SIZE/2).dist(a.getPos().copy().add(Asteroid.SIZE/2, Asteroid.SIZE/2));
                if(nextDistance < nearestDistance) {
                    nearestDistance = nextDistance;
                    nearestAsteroid = a;
                }
            }

            if(nearestDistance < 50) {
                b.collision();
                nearestAsteroid.collision();
                player.increaseScore();
                asteroidPauseTime = Math.max(asteroidPauseTime-Asteroid.SPAWN_DELAY_DECREASE, Asteroid.MINIMUM_SPAWN_DELAY);
                AudioManager.playSound(Sounds.ASTEROID_DESTROY);
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
        return new Thread(() -> {
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
                    Thread.sleep(TICK_SPEED);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.getLogger("GameManager").log(System.Logger.Level.INFO, "Updater Stopped");
        });
    }

    private Thread interfaceUpdaterInstance() {
        return new Thread(() -> {
            while(interfaceRunning) {
                gui.repaint();
                try {
                    Thread.sleep(INTERFACE_SPEED);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
