package com.lifejourney.engine2d;

public class Size {

    public Size() {

    }

    public Size(Size size) {
        width = size.width;
        height = size.height;
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size add(int w, int h) {
        width += w;
        height += h;
        return this;
    }

    public Size multiply(float m) {
        width *= m;
        height *= m;
        return this;
    }

    public Size multiply(float w, float h) {
        width *= w;
        height *= h;
        return this;
    }

    public int width = 0;
    public int height = 0;
}
