package com.lifejourney.engine2d;

public class Rect {

    public Rect() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rect(Point pt, Size size) {
        this.x = pt.x;
        this.y = pt.y;
        this.width = size.width;
        this.height = size.height;
    }

    public Rect(Rect rect) {
        x = rect.x;
        y = rect.y;
        width = rect.width;
        height = rect.height;
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
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

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean intersects(Rect a, Rect b) {
        return a.left() < b.right() && b.left() < a.right() && a.top() < b.bottom() && b.top() < a.bottom();
    }

    /**
     *
     * @return
     */
    public int centerX() {
        return x + width/2;
    }

    /**
     *
     * @return
     */
    public int centerY() {
        return y + height/2;
    }

    /**
     *
     * @return
     */
    public Point center() {
        return new Point(centerX(), centerY());
    }

    /**
     *
     * @return
     */
    public float exactCenterX() {
        return x + (float)width/2;
    }

    /**
     *
     * @return
     */
    public float exactCenterY() {
        return y + (float)height/2;
    }

    /**
     *
     * @return
     */
    public PointF exactCenter() {
        return new PointF(exactCenterX(), exactCenterY());
    }

    /**
     *
     * @return
     */
    public int left() {
        return x;
    }

    /**
     *
     * @return
     */
    public int right() {
        return x + width;
    }

    /**
     *
     * @return
     */
    public int top() {
        return y;
    }

    /**
     *
     * @return
     */
    public int bottom() {
        return y + height;
    }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setTo(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     *
     * @param x
     * @param y
     */
    public void offset(int x, int y) {
        this.x += x;
        this.y += y;
    }

    /**
     *
     * @param pt
     */
    public void offset(Point pt) {
        x += pt.x;
        y += pt.y;
    }

    /**
     *
     * @param x
     * @param y
     */
    public void offsetTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @param pt
     */
    public void offsetTo(Point pt) {
        x = pt.x;
        y = pt.y;
    }

    /**
     *
     * @param pt
     * @return
     */
    public boolean includes(Point pt) {
        return pt.x >= left() && pt.x < right() && pt.y >= top() && pt.y < bottom();
    }

    public int x;
    public int y;
    public int width;
    public int height;
}
