package com.lifejourney.racingfever;

import android.util.Log;

public class Object {

    private static String LOG_TAG = "Object";

    Object(int x, int y, int width, int height, float rotation, String spriteAsset, float[] color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.color = color;
        this.visible = false;
        this.sprite = new Sprite(spriteAsset);

        updateSprite();
    }

    Object(int x, int y, int width, int height, String spriteAsset) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.color = new float[] { 1.0f, 1.0f, 1.0f };
        this.visible = false;
        this.sprite = new Sprite(spriteAsset);

        updateSprite();
    }

    void updateSprite() {
        if (this.sprite != null) {
            sprite.set(x, y, width, height, rotation, color, visible);
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        updateSprite();
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        updateSprite();
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        updateSprite();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        updateSprite();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        updateSprite();
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        updateSprite();
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        updateSprite();
    }

    public void show() {
        this.visible = true;
        updateSprite();
    }

    public void hide() {
        this.visible = false;
        updateSprite();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        updateSprite();
    }

    public boolean isVisible() {
        return this.visible;
    }

    private int x;
    private int y;
    private int width;
    private int height;
    private float rotation;
    private Sprite sprite;
    private float[] color;
    private boolean visible;
}
