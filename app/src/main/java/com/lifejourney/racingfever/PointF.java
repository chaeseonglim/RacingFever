package com.lifejourney.racingfever;

public class PointF {

    public PointF() {
    }

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setTo(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void offset(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void offset(PointF p) {
        this.x += p.x;
        this.y += p.y;
    }

    public Vector2D vectorize() {
        return new Vector2D(x, y);
    }

    public PointF multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    public float x = 0;
    public float y = 0;
}
