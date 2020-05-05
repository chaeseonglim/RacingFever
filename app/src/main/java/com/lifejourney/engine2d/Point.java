package com.lifejourney.engine2d;

import androidx.annotation.Nullable;

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

    public float distance(Point p) {
        return (float) Math.sqrt(distanceSq(p));
    }

    public float distanceSq(Point p) {
        float deltaX = x - p.x;
        float deltaY = y - p.y;
        return (float) (Math.pow(deltaX,2) + Math.pow(deltaY,2));
    }

    public Vector2D vectorize() {
        return new Vector2D((float)x, (float)y);
    }

    @Override
    public boolean equals(@Nullable java.lang.Object obj) {
        if (!super.equals(obj)) {
            if (this.x == ((Point)obj).x && this.y == ((Point)obj).y) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    public int x = 0;
    public int y = 0;
}
