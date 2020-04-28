package com.lifejourney.racingfever;

public class Point {

    public Point() {
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void offset(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public Vector2D vectorize() {
        return new Vector2D((float)x, (float)y);
    }

    public int x = 0;
    public int y = 0;
}
