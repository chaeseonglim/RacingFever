package com.lifejourney.engine2d;

public class Rect {

    public Rect() {
    }

    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rect(Rect rect) {
        x = rect.x;
        y = rect.y;
        width = rect.width;
        height = rect.height;
    }

    public boolean setIntersect(Rect a, Rect b) {
        if (a.left() < b.right() && b.left() < a.right() && a.top() < b.bottom() && b.top() < a.bottom()) {
            x = Math.max(a.left(), b.left());
            y = Math.max(a.top(), b.top());
            width = Math.min(a.right(), b.right()) - x;
            height = Math.min(a.bottom(), b.bottom()) - y;
            return true;
        }
        return false;
    }

    public static boolean intersects(Rect a, Rect b) {
        return a.left() < b.right() && b.left() < a.right() && a.top() < b.bottom() && b.top() < a.bottom();
    }

    public int centerX() {
        return x + width/2;
    }

    public int centerY() {
        return y + height/2;
    }

    public Point center() {
        return new Point(centerX(), centerY());
    }

    public float exactCenterX() {
        return x + (float)width/2;
    }

    public float exactCenterY() {
        return y + (float)height/2;
    }

    public PointF exactCenter() {
        return new PointF(exactCenterX(), exactCenterY());
    }

    public int left() {
        return x;
    }

    public int right() {
        return x + width;
    }

    public int top() {
        return y;
    }

    public int bottom() {
        return y + height;
    }

    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;
}
