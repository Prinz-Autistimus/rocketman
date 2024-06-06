package com.rocketman.gui;

import com.rocketman.game.objects.Asteroid;
import com.rocketman.game.objects.Bullet;
import com.rocketman.game.objects.Player;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;

public class GuiClass {

    private JFrame window;
    private int width, height;
    private final String title = "Rocket Man";

    private CanvasClass canvas;
    private Player player;
    private Vector<Asteroid> asteroids;
    private Vector<Bullet> bullets;
    private Runnable onCloseFunction;
    private Consumer<Integer> onRotationInput;
    private Runnable onAccelerationInput;
    private Runnable onStopAccelerationInput;
    private Runnable onShootInput;

    public GuiClass(int _width, int _height, Player _player, Vector<Asteroid> _asteroids, Vector<Bullet> _bullets, Runnable _onClose, Consumer<Integer> _onRotationInput, Runnable _onAccelerationInput, Runnable _onStopAccelerationInput, Runnable _onShootInput) {
        this.width = _width;
        this.height = _height;

        this.player = _player;

        this.onCloseFunction = _onClose;
        this.onRotationInput = _onRotationInput;
        this.onAccelerationInput = _onAccelerationInput;
        this.onStopAccelerationInput = _onStopAccelerationInput;
        this.onShootInput = _onShootInput;

        this.asteroids = _asteroids;
        this.bullets = _bullets;

        initWindow();
        initComponents();
        canvas.repaint();

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCloseFunction.run();
                super.windowClosing(e);
            }
        });

        Set<Integer> pressedKeys = new HashSet<>();
        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                return;
            }

            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());

            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    for(int i = 0; i < pressedKeys.size(); ++i) {
                        int k = (int) pressedKeys.toArray()[i];
                        switch (k) {
                            case 65 -> onRotationInput.accept(-1);
                            case 68 -> onRotationInput.accept(1);
                            case 87 -> onAccelerationInput.run();
                            case 32 -> onShootInput.run();
                        }
                    }
                    if(!pressedKeys.contains(87)) {
                        onStopAccelerationInput.run();
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    private void initWindow() {
        window = new JFrame(getTitle());
        window.setSize(getWidth(), getHeight());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initComponents() {
        canvas = new CanvasClass(player, asteroids, bullets);
        window.add(canvas);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getTitle() { return title; }

    public void show() { window.setVisible(true); }
    public void hide() { window.setVisible(false); }

    public void repaint() { canvas.repaint(); }

}
