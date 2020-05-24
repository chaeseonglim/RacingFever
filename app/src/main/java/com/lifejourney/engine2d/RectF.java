package com.lifejourney.engine2d;

public class RectF {

    public RectF() {
        x = 0.0f;
        y = 0.0f;
        width = 0.0f;
        height = 0.0f;
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

    /**
     *
     * @param a
     * @param b
     * @return
     */
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

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean intersects(RectF a, RectF b) {
        return a.left() < b.right() && b.left() < a.right() && a.top() < b.bottom() && b.top() < a.bottom();
    }

    /**
     *
     * @return
     */
    public float centerX() {
        return x + width/2;
    }

    /**
     *
     * @return
     */
    public float centerY() {
        return y + height/2;
    }

    /**
     *
     * @return
     */
    public PointF center() {
        return new PointF(centerX(), centerY());
    }

    /**
     *
     * @return
     */
    public float left() {
        return x;
    }

    /**
     *
     * @return
     */
    public float right() {
        return x + width;
    }

    /**
     *
     * @return
     */
    public float top() {
        return y;
    }

    /**
     *
     * @return
     */
    public float bottom() {
        return y + height;
    }

    /**
     *
     * @return
     */
    public PointF topLeft() {
        return new PointF(left(), top());
    }

    /**
     *
     * @return
     */
    public PointF topRight() {
        return new PointF(right(), top());
    }

    /**
     *
     * @return
     */
    public PointF bottomLeft() {
        return new PointF(left(), bottom());
    }

    /**
     *
     * @return
     */
    public PointF bottomRight() {
        return new PointF(right(), bottom());
    }

    /**
     *
     * @param pt
     * @return
     */
    public boolean includes(PointF pt) {
        return pt.x >= left() && pt.x < right() && pt.y >= top() && pt.y < bottom();
    }

    public float x;
    public float y;
    public float width;
    public float height;
}
