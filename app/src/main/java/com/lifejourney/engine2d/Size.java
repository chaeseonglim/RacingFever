package com.lifejourney.engine2d;

public class Size {

    public Size() {
        width = 0;
        height = 0;
    }

    public Size(Size size) {
        width = size.width;
        height = size.height;
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     *
     * @param w
     * @param h
     * @return
     */
    public Size add(int w, int h) {
        width += w;
        height += h;
        return this;
    }

    /**
     *
     * @param m
     * @return
     */
    public Size multiply(float m) {
        width *= m;
        height *= m;
        return this;
    }

    /**
     *
     * @param w
     * @param h
     * @return
     */
    public Size multiply(float w, float h) {
        width *= w;
        height *= h;
        return this;
    }

    public int width;
    public int height;
}
