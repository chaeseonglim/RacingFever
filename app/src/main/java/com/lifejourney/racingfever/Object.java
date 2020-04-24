package com.lifejourney.racingfever;

import android.graphics.Rect;
import android.util.Log;

public class Object {

    private static String LOG_TAG = "Object";

    Object(int x, int y, int width, int height, float rotation, String spriteAsset) {
        this.location = new Rect(x, y, x+width, y+height);
        this.rotation = rotation;
        this.sprite = new Sprite(x, y, width, height, rotation, color, spriteAsset);
    }

    Object(Rect location, float rotation, String spriteAsset) {
        this.location = location;
        this.rotation = rotation;
        this.sprite = new Sprite(location.left, location.top, location.width(), location.height(),
                rotation, color, spriteAsset);
    }

    void updateSprite() {
        if (this.sprite != null && isSpriteInvalid) {
            sprite.set(location.left, location.top, location.width(), location.height(),
                    rotation, color, visible);
            isSpriteInvalid = false;
        }
    }

    public void update() {
        updateSprite();
    }

    public int getX() {
        return location.left;
    }

    public void setX(int x) {
        location.offsetTo(x, location.top);
        isSpriteInvalid = true;
    }

    public int getY() {
        return location.top;
    }

    public void setY(int y) {
        location.offsetTo(location.left, y);
        isSpriteInvalid = true;
    }

    public void setPos(int x, int y) {
        location.offsetTo(x,y);
        isSpriteInvalid = true;
    }

    public int getWidth() { return location.width(); }

    public void setWidth(int width) {
        location.right = location.left + width;
        isSpriteInvalid = true;
    }

    public int getHeight() {
        return location.height();
    }

    public void setHeight(int height) {
        location.bottom = location.top + height;
        isSpriteInvalid = true;
    }

    public Rect getLocation() { return location; }

    public void setLocation(Rect location) {
        this.location = location;
        isSpriteInvalid = true;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        isSpriteInvalid = true;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        isSpriteInvalid = true;
    }

    public void show() {
        this.visible = true;
        isSpriteInvalid = true;
    }

    public void hide() {
        this.visible = false;
        isSpriteInvalid = true;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        isSpriteInvalid = true;
    }

    private Rect location;
    private float rotation = 0.0f;
    private Sprite sprite;
    private float[] color = new float[] { 1.0f, 1.0f, 1.0f };;
    private boolean visible = false;

    private boolean isSpriteInvalid = false;
}
