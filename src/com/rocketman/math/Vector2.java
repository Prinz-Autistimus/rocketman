package com.rocketman.math;

public class Vector2 {

    private double x, y;
    private final int roundPlace = (int) Math.pow(10, 3);

    public Vector2(double _x, double _y) {
        x = _x;
        y = _y;
        round();
    }

    public Vector2() {
        this(0, 0);
    }

    public Vector2(Vector2 a) {
        this(a.x, a.y);
    }

    public double x() { return x; }
    public double y() { return y; }

    public void setX(double _x) {
        x = _x;
    }

    public void setY(double _y) {
        y = _y;
    }

    public Vector2 rotate (double angle) {
        double newX = x * Math.cos(angle) - y * Math.sin(angle);
        double newY = x * Math.sin(angle) + y * Math.cos(angle);
        x = newX;
        y = newY;
        round();
        return this;
    }

    public Vector2 rotated(double angle) {
        return new Vector2(this).rotate(angle);
    }

    public Vector2 add(double _x, double _y) {
        x += _x;
        y += _y;
        return this;
    }

    public Vector2 add(Vector2 a) {
        return add(a.x, a.y);
    }

    public Vector2 added(double _x, double _y) {
        return new Vector2(this).add(_x, _y);
    }

    public Vector2 added(Vector2 a) {
        return added(a.x, a.y);
    }

    public Vector2 sub(double _x, double _y){
        x -= _x;
        y -= _y;
        return this;
    }

    public Vector2 sub(Vector2 a){
        return sub(a.x, a.y);
    }

    public Vector2 subbed(double _x, double _y){
        return new Vector2(_x, _y).sub(this);
    }

    public Vector2 subbed(Vector2 a){
        return subbed(a.x, a.y);
    }

    public Vector2 scale(double s) {
        x *= s;
        y *= s;
        return this;
    }

    public Vector2 scaled(double s) {
        return new Vector2(this).scale(s);
    }

    public double len() {
        return Math.sqrt(x*x + y*y);
    }

    public double dist(Vector2 a) {
        return Math.abs(a.subbed(this).len());
    }

    public Vector2 copy() {
        return new Vector2(this);
    }

    public Vector2 set(Vector2 a) {
        return set(a.x, a.y);
    }

    public Vector2 set(double _x, double _y) {
        x = _x;
        y = _y;
        return this;
    }

    private void round() {
        x = ((int)(x * roundPlace))/(double)roundPlace;
        y = ((int)(y * roundPlace))/(double)roundPlace;
    }

    @Override
    public String toString() {
        return "X: " + x + ", Y: " + y;
    }
}
