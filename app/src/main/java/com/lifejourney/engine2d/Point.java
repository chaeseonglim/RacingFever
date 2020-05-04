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

    public Point setTo(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Point offset(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Point offset(Point p) {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    public Point add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Point add(Point p) {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    public Point subtract(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Point subtract(Point p) {
        this.x -= p.x;
        this.y -= p.y;
        return this;
    }

    public Point multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    public Point divide(float m) {
        x /= m;
        y /= m;
        return this;
    }

    public Vector2D vectorize() {
        return new Vector2D((float)x, (float)y);
    }

    public int x = 0;
    public int y = 0;
}
