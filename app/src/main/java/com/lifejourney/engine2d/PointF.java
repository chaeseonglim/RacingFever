package com.lifejourney.engine2d;

import androidx.annotation.Nullable;

public class PointF {

    public PointF() {
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

    public void add(PointF p) {
        this.x += p.x;
        this.y += p.y;
    }

    public void subtract(PointF p) {
        this.x -= p.x;
        this.y -= p.y;
    }

    public Vector2D vectorize() {
        return new Vector2D(x, y);
    }

    public PointF multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    public PointF expandToNextInt() {
        x = (float) ((x >= 0.0f) ? Math.ceil(x) : Math.floor(x));
        y = (float) ((y >= 0.0f) ? Math.ceil(y) : Math.floor(y));
        return this;
    }

    @Override
    public boolean equals(@Nullable java.lang.Object obj) {
        if (!super.equals(obj)) {
            if (this.x == ((PointF)obj).x &&
                    this.y == ((PointF)obj).y)
                return true;
            else
                return false;
        }
        else {
            return true;
        }
    }

    public float x = 0;
    public float y = 0;
}
