package com.lifejourney.engine2d;

import androidx.annotation.Nullable;

public class PointF {

    public PointF() {
        x = 0.0f;
        y = 0.0f;
    }

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF(PointF p) {
        this.x = p.x;
        this.y = p.y;
    }

    public PointF(Point p) {
        this.x = (float)p.x;
        this.y = (float)p.y;
    }

    public PointF(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public PointF setTo(float x, float y) {
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
    public PointF offset(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public PointF offset(PointF p) {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public PointF add(PointF p) {
        this.x += p.x;
        this.y += p.y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public PointF subtract(PointF p) {
        this.x -= p.x;
        this.y -= p.y;
        return this;
    }

    /**
     *
     * @param m
     * @return
     */
    public PointF multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public PointF multiply(PointF p) {
        x *= p.x;
        y *= p.y;
        return this;
    }

    /**
     *
     * @param m
     * @return
     */
    public PointF divide(float m) {
        x /= m;
        y /= m;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public PointF divide(PointF p) {
        x /= p.x;
        y /= p.y;
        return this;
    }

    /**
     *
     * @param p
     * @return
     */
    public float distance(PointF p) {
        return (float) Math.sqrt(distanceSq(p));
    }

    /**
     *
     * @param p
     * @return
     */
    public float distanceSq(PointF p) {
        float deltaX = x - p.x;
        float deltaY = y - p.y;
        return (float) (Math.pow(deltaX,2) + Math.pow(deltaY,2));
    }

    /**
     *
     * @return
     */
    public Vector2D vectorize() {
        return new Vector2D(x, y);
    }

    /**
     *
     * @return
     */
    public PointF expandToNextInt() {
        x = (float) ((x >= 0.0f) ? Math.ceil(x) : Math.floor(x));
        y = (float) ((y >= 0.0f) ? Math.ceil(y) : Math.floor(y));
        return this;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(@Nullable java.lang.Object obj) {
        if (this != obj) {
            if (obj instanceof PointF) {
                return this.x == ((PointF) obj).x && this.y == ((PointF) obj).y;
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
        return (int)x + ((int)(y * Math.pow(2, 8)));
    }

    public float x;
    public float y;
}
