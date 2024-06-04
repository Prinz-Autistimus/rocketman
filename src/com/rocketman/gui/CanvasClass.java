package com.rocketman.gui;

import com.rocketman.game.Asteroid;
import com.rocketman.game.Bullet;
import com.rocketman.game.Player;
import com.rocketman.math.Vector2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

public class CanvasClass extends JPanel {

    private HashMap<String, BufferedImage> assets = new HashMap<>();
    private Player player;
    private boolean playerState = false;
    private final int stateFrameCountMax = 15;
    private int stateFrameCount = stateFrameCountMax;
    private Vector<Asteroid> asteroids;
    private Vector<Bullet> bullets;

    private long frameTime = 0;
    private long lastFrame = 0;

    public CanvasClass(Player _player, Vector<Asteroid> _asteroids, Vector<Bullet> _bullets) {
        if(!loadAssets()) { System.exit(1); }
        player = _player;
        asteroids = _asteroids;
        bullets = _bullets;
    }

    private boolean loadAssets() {
            HashMap<String, String> loadingRegister = new HashMap<>();
            loadingRegister.put("rocket_on", "sprites/rocket_256_on.png");
            loadingRegister.put("rocket_off", "sprites/rocket_256_off.png");
            loadingRegister.put("asteroid", "sprites/asteroid.png");
            loadingRegister.put("bg", "sprites/bg.jpg");
            loadingRegister.put("bullet", "sprites/bullet.png");

            for(String k : loadingRegister.keySet()) {
                try {
                    grabAsset(k, loadingRegister.get(k));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error while loading asset: " + k, "Asset Loading Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return false;
                }
            }
        return true;
    }

    private void grabAsset(String tag, String name) throws IOException{
        URL url = ClassLoader.getSystemResource(name);
        assets.put(tag, ImageIO.read(url));
    }

    @Override
    public void paintComponent(Graphics g) {
        frameTime = System.currentTimeMillis() - lastFrame;
        lastFrame = System.currentTimeMillis();
        drawBackground(g);
        drawBullets(g);

        if(player.isImmune()) {

            if(playerState) {
                drawRocket(g);
            }

            stateFrameCount--;

            if(stateFrameCount == 0) {
                playerState = !playerState;
                stateFrameCount = stateFrameCountMax;
            }

        }else {
            drawRocket(g);
            stateFrameCount = stateFrameCountMax;
        }

        drawHealthBar(g);
        drawAsteroids(g);
        drawScore(g);
        drawAmmo(g);

        //drawDebug(g);
    }

    private void drawDebug(Graphics g) {
        g.setColor(Color.GREEN);
        String debug = "Asteroids: " + asteroids.size()
                + " Bullets: " + bullets.size()
                + " FrameTime: " + frameTime;
        g.drawString(debug, 10, 30);
    }

    private void drawAmmo(Graphics g) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        for(int i = 0; i < player.getAmmo(); ++i) {
            g.drawImage(assets.get("bullet"), i*40, (int) d.getHeight()-150, 80, 80, null);
        }

        g.setColor(new Color(255,70,120,255));
        g.drawRoundRect(5, (int) d.getHeight()-150, player.getMaxAmmo()*40 + 35, 80, 20, 20);
    }

    private void drawScore(Graphics g) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        g.setColor(Color.WHITE);
        Font f = new Font("TimesRoman", Font.PLAIN, 30);
        g.setFont(f);
        String scoreText = "Score: " + player.getScore();
        g.drawString(scoreText, (int) d.getWidth()/2-g.getFontMetrics().stringWidth(scoreText)/2, 35);

    }

    private void drawBullets(Graphics g) {
        for(int i = 0; i < bullets.size(); ++i) {
            Bullet b = bullets.get(i);
            Vector2 pos = b.getPos();
            double rot = b.getRot();
            BufferedImage transformed = rotate(assets.get("bullet"), rot);
            g.drawImage(transformed, (int) pos.x(), (int) pos.y(), 30, 30, null);
        }
    }

    private void drawAsteroids(Graphics g) {
        for(int i = 0; i < asteroids.size(); ++i) {
            Asteroid a = asteroids.get(i);
            Vector2 pos = a.getPos();
            double rot = a.getRotation();
            BufferedImage transformed = rotate(assets.get("asteroid"), rot);
            g.drawImage(transformed, (int) pos.x(), (int) pos.y(), 50, 50, null);
        }
    }

    private void drawHealthBar(Graphics g) {
        Vector2 pos = player.getPosition();

        g.setColor(Color.WHITE);
        g.fillRoundRect((int) pos.x()+25, (int) pos.y()+90, 50, 5, 5, 5);

        g.setColor(Color.BLACK);
        g.fillRoundRect((int) pos.x()+25, (int) pos.y()+90, (int)((player.getHealth()+.05)*50), 5, 5, 5);

        g.setColor(Color.GREEN);
        g.fillRoundRect((int) pos.x()+25, (int) pos.y()+90, (int)(player.getHealth()*50), 5, 5, 5);
    }

    private void drawRocket(Graphics g) {
        double rot = player.getRotation();
        BufferedImage transformedRocket = rotate(getRocket(), rot);
        g.drawImage(transformedRocket, (int) player.getPosition().x(), (int) player.getPosition().y(), (int) player.getPosition().x()+100, (int) player.getPosition().y()+100, 0, 0, transformedRocket.getWidth(), transformedRocket.getHeight(), null);
    }

    private void drawBackground(Graphics g) {
        g.drawImage(assets.get("bg"), 0, 0, getWidth(), getHeight(), null);
    }

    private BufferedImage rotate(BufferedImage bimg, Double angle) {
        BufferedImage rotated = new BufferedImage(bimg.getWidth(), bimg.getHeight(), bimg.getType());
        Graphics2D g = rotated.createGraphics();
        g.rotate(angle, bimg.getWidth()/2, bimg.getHeight()/2);
        g.drawImage(bimg, 0, 0, null);
        return rotated;
    }

    public BufferedImage getRocket() {
        if(player.isAccelerating()) {
            player.resetAcceleration();
            return assets.get("rocket_on");
        }
        return assets.get("rocket_off");
    }
}
