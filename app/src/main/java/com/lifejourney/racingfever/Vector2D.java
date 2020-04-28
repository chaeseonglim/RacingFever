package com.lifejourney.racingfever;

public class Vector2D {

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v) {
        x = v.x;
        y = v.y;
    }

    public Vector2D add(float a) {
        x += a;
        y += a;
        return this;
    }

    public Vector2D add(Vector2D v) {
        x += v.x;
        y += v.y;
        return this;
    }

    public Vector2D subtract(float s) {
        x -= s;
        y -= s;
        return this;
    }

    public Vector2D subtract(Vector2D v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    public Vector2D multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    public Vector2D multiply(Vector2D v) {
        x *= v.x;
        y *= v.y;
        return this;
    }

    public Vector2D divide(float d) {
        if (d != 0) {
            x /= d;
            y /= d;
        }
        return this;
    }

    public Vector2D divide(Vector2D v) {
        if (v.x != 0) {
            x /= v.x;
        }
        if (v.y != 0) {
            y /= v.y;
        }
        return this;
    }

    public float dot(Vector2D v) {
        return x*v.x + y*v.y;
    }

    public float cross(Vector2D v) {
        return x*v.y-y*v.x;
    }

    public float ccw(Vector2D v) {
        /* Positive - ccw
           Negative - cw
           Parallel - 0
         */
        return cross(v);
    }

    public Vector2D normalize() {
        if (lengthSq() > 0.0f) {
            divide((float) Math.sqrt(lengthSq()));
        }
        return this;
    }

    public Vector2D rotate(float degree) {
        float radian = (float) (Math.PI / 180 * degree);
        float newX = (float) (this.x*Math.cos(radian) - this.y*Math.sin(radian));
        float newY = (float) (this.x*Math.sin(radian) + this.y*Math.cos(radian));

        this.x = newX;
        this.y = newY;

        return this;
    }

    public float length() {
        return (float) Math.sqrt(lengthSq());
    }

    public float lengthSq() {
        return x*x + y*y;
    }

    public float distance(Vector2D v) {
        return (float) Math.sqrt(distanceSq(v));
    }

    public float distanceSq(Vector2D v) {
        float deltaX = x - v.x;
        float deltaY = y - v.y;
        return deltaX*deltaX + deltaY*deltaY;
    }

    public float angle() {
        return (float) (Math.atan2((double)y, (double)x) * 180 / Math.PI);
    }

    public float x;
    public float y;
}
