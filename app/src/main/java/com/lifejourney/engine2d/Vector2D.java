package com.lifejourney.engine2d;

import android.util.Log;

public class Vector2D {

    private static final String LOG_TAG = "Vector2D";

    public Vector2D() {
        x = 0.0f;
        y = 0.0f;
    }

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D clone() {
        return new Vector2D(this);
    }

    /**
     * Produce an unit vector which forwards towards direction
     * @param direction
     */
    public Vector2D(float direction) {
        x = 0.0f;
        y = -1.0f;
        rotate(direction);
    }

    public Vector2D(Vector2D v) {
        x = v.x;
        y = v.y;
    }

    /**
     *
     * @return
     */
    public Vector2D reset() {
        x = 0.0f;
        y = 0.0f;
        return this;
    }

    /**
     *
     * @param a
     * @return
     */
    public Vector2D add(float a) {
        x += a;
        y += a;
        return this;
    }

    /**
     *
     * @param v
     * @return
     */
    public Vector2D add(Vector2D v) {
        x += v.x;
        y += v.y;
        return this;
    }

    /**
     *
     * @param s
     * @return
     */
    public Vector2D subtract(float s) {
        x -= s;
        y -= s;
        return this;
    }

    /**
     *
     * @param v
     * @return
     */
    public Vector2D subtract(Vector2D v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    /**
     *
     * @param m
     * @return
     */
    public Vector2D multiply(float m) {
        x *= m;
        y *= m;
        return this;
    }

    /**
     *
     * @param v
     * @return
     */
    public Vector2D multiply(Vector2D v) {
        x *= v.x;
        y *= v.y;
        return this;
    }

    /**
     *
     * @param d
     * @return
     */
    public Vector2D divide(float d) {
        if (d != 0) {
            x /= d;
            y /= d;
        }
        return this;
    }

    /**
     *
     * @param v
     * @return
     */
    public Vector2D divide(Vector2D v) {
        if (v.x != 0) {
            x /= v.x;
        }
        if (v.y != 0) {
            y /= v.y;
        }
        return this;
    }

    /**
     *
     * @param maxLength
     * @return
     */
    public Vector2D truncate(float maxLength) {
        if (lengthSq() > maxLength*maxLength) {
            normalize();
            multiply(maxLength);
        }

        return this;
    }

    /**
     *
     * @param v
     * @return
     */
    public float dot(Vector2D v) {
        return x*v.x + y*v.y;
    }

    /**
     *
     * @param v
     * @return
     */
    public float cross(Vector2D v) {
        return x*v.y-y*v.x;
    }

    /**
     *
     * @param v
     * @return Positive - ccw, Negative - cw, Parallel - 0
     */
    public float ccw(Vector2D v) {
        return cross(v);
    }

    /**
     *
     * @param v
     * @return
     */
    public float angle(Vector2D v) {
        float lengthMult = length() * v.length();
        if (lengthMult == 0.0f) {
            return 0.0f;
        }
        float angle = (float) (Math.acos(dot(v)/lengthMult) * 180.0f / Math.PI);
        if (Float.isNaN(angle)) {
            angle = 0.0f;
        }

        return angle;
    }

    /**
     * Angle between y-axis and this vector
     * @return
     */
    public float direction() {
        if (length() == 0.0f) {
            return 0.0f;
        }

        float direction = (float) (Math.acos(-y/length()) * 180.0f / Math.PI);
        if (x < 0.0f) {
            direction = 360.0f - direction;
        }

        return direction;

    }

    /**
     *
     * @return
     */
    public Vector2D normalize() {
        float lengthsq = lengthSq();
        if (lengthsq > 0.0f) {
            divide((float) Math.sqrt(lengthsq));
        }
        return this;
    }

    /**
     *
     * @param degree
     * @return
     */
    public Vector2D rotate(float degree) {
        float radian = (float) (Math.PI / 180 * degree);
        float newX = (float) (this.x*Math.cos(radian) - this.y*Math.sin(radian));
        float newY = (float) (this.x*Math.sin(radian) + this.y*Math.cos(radian));

        this.x = newX;
        this.y = newY;

        return this;
    }

    /**
     *
     * @return
     */
    public Vector2D perpendicular() {
        float x = this.y;
        float y = -this.x;
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     *
     * @param unitBasis
     * @return
     */
    public Vector2D parallelComponent(Vector2D unitBasis) {
        float projection = dot(unitBasis);
        return new Vector2D(unitBasis).multiply(projection);
    }

    /**
     *
     * @param unitBasis
     * @return
     */
    public Vector2D perpendicularComponent(Vector2D unitBasis) {
        return subtract(parallelComponent(unitBasis));
    }

    /**
     *
     * @return
     */
    public float length() {
        return (float) Math.sqrt(lengthSq());
    }

    /**
     *
     * @return
     */
    public float lengthSq() {
        return x*x + y*y;
    }

    /**
     *
     * @param v
     * @return
     */
    public float distance(Vector2D v) {
        return (float) Math.sqrt(distanceSq(v));
    }

    /**
     *
     * @param v
     * @return
     */
    public float distanceSq(Vector2D v) {
        float deltaX = x - v.x;
        float deltaY = y - v.y;
        return (float) (Math.pow(deltaX,2) + Math.pow(deltaY,2));
    }

    public float x;
    public float y;
}
