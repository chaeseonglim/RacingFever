package com.lifejourney.engine2d;

public class RectF {

    public RectF() {
    }

    public RectF(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public RectF(RectF rect) {
        x = rect.x;
        y = rect.y;
        width = rect.width;
        height = rect.height;
    }

    public RectF(Rect rect) {
        x = (int)rect.x;
        y = (int)rect.y;
        width = (int)rect.width;
        height = (int)rect.height;
    }

    public boolean setIntersect(RectF a, RectF b) {
        if (a.left() < b.right() && b.left() < a.right() && a.top() < b.bottom() && b.top() < a.bottom()) {
            x = Math.max(a.left(), b.left());
            y = Math.max(a.top(), b.top());
            width = Math.min(a.right(), b.right()) - x;
            height = Math.min(a.bottom(), b.bottom()) - y;
            return true;
        }
        return false;
    }

    public static boolean intersects(RectF a, RectF b) {
        return a.left() < b.right() && b.left() < a.right() && a.top() < b.bottom() && b.top() < a.bottom();
    }

    public float centerX() {
        return x + width/2;
    }

    public float centerY() {
        return y + height/2;
    }

    public PointF center() {
        return new PointF(centerX(), centerY());
    }

    public float left() {
        return x;
    }

    public float right() {
        return x + width;
    }

    public float top() {
        return y;
    }

    public float bottom() {
        return y + height;
    }

    public boolean includes(PointF pt) {
        if (pt.x >= left() && pt.x < right() && pt.y >= top() && pt.y < bottom())
            return true;
        return false;
    }

    public float x = 0;
    public float y = 0;
    public float width = 0;
    public float height = 0;
}
