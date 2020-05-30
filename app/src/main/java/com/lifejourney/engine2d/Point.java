package com.lifejourney.engine2d;

import android.util.Log;

import androidx.annotation.Nullable;

public class Point {

    final static String LOG_TAG = "Point";

    public Point() {
        x = 0;
        y = 0;
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

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public Point setTo(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public Point offset(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public Point offset(Point p) {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public Point add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public Point add(Point p) {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public Point subtract(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public Point subtract(Point p) {
        this.x -= p.x;
        this.y -= p.y;
        return this;
    }

    /**
     *
     * @param m
     * @return
     */
    public Point multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public Point multiply(Point p) {
        x *= p.x;
        y *= p.y;
        return this;
    }

    /**
     *
     * @param m
     * @return
     */
    public Point divide(float m) {
        x /= m;
        y /= m;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public Point divide(Point p) {
        x /= p.x;
        y /= p.y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public float distance(Point p) {
        return (float) Math.sqrt(distanceSq(p));
    }

    /**
     *
     * @param p
     * @return
     */
    public float distanceSq(Point p) {
        float deltaX = x - p.x;
        float deltaY = y - p.y;
        return (float) (Math.pow(deltaX,2) + Math.pow(deltaY,2));
    }

    /**
     *
     * @return
     */
    public Vector2D vectorize() {
        return new Vector2D((float)x, (float)y);
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(@Nullable java.lang.Object obj) {
        if (this != obj) {
            if (obj instanceof Point) {
                return this.x == ((Point) obj).x && this.y == ((Point) obj).y;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        return x + (y << 5);
    }

    public int x;
    public int y;
}
