package com.lifejourney.engine2d;

public class Point {

    public Point() {
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point(PointF p) {
        this.x = (int)p.x;
        this.y = (int)p.y;
    }

    public void setTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void offset(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public void offset(Point p) {
        this.x += p.x;
        this.y += p.y;
    }

    public void add(Point p) {
        this.x += p.x;
        this.y += p.y;
    }

    public void subtract(Point p) {
        this.x -= p.x;
        this.y -= p.y;
    }
    public Vector2D vectorize() {
        return new Vector2D((float)x, (float)y);
    }

    public Point multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    public int x = 0;
    public int y = 0;
}
