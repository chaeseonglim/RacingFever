package com.lifejourney.racingfever;

import android.util.Log;

public class Sprite {

    private static String LOG_TAG = "Sprite";

    public Sprite(int x, int y, int width, int height, float rotation, float[] color, String asset) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.color = color;
        this.asset = asset;

        load();
    }

    public Sprite(String asset) {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.rotation = 0.0f;
        this.asset = asset;
        this.color = new float[] { 1.0f, 1.0f, 1.0f };

        load();
    }

    public boolean load() {
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        if (!resourceManager.loadTexture(asset)) {
            Log.e(LOG_TAG, "Failed to load texture");
            return false;
        }

        this.id = nCreateSprite(this.x, this.y, this.width, this.height, this.rotation, this.asset, this.color);
        if (this.id == INVALID_ID) {
            Log.e(LOG_TAG, "Failed to create sprite");
            return false;
        }

        return true;
    }

    public void finalize() {
        if (id != INVALID_ID) {
            nDestorySprite(id);
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        nSetPos(id, this.x, this.y);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        nSetPos(id, this.x, this.y);
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        nSetPos(id, this.x, this.y);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        nSetSize(id, this.width, this.height);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        nSetSize(id, this.width, this.height);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        nSetSize(id, this.width, this.height);
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        nSetRotation(id, this.rotation);
    }

    public String getAsset() {
        return asset;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
        nSetColor(id, this.color);
    }

    public void show() {
        this.visible = true;
        nShow(id);
    }

    public void hide() {
        this.visible = false;
        nHide(id);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void set(int x, int y, int width, int height, float rotation, float[] color, boolean visible) {
        nSetAll(id, x, y, width, height, rotation, color, visible);
    }

    private final int INVALID_ID = -1;

    private int id;
    private int x;
    private int y;
    private int width;
    private int height;
    private float rotation;
    private String asset;
    private float[] color;
    private boolean visible = false;

    private native int nCreateSprite(int x, int y, int width, int height, float rotation, String asset, float[] color);
    private native void nDestorySprite(int id);
    private native void nSetPos(int id, int x, int y);
    private native void nSetSize(int id, int width, int height);
    private native void nSetRotation(int id, float rotation);
    private native void nSetColor(int id, float[] color);
    private native void nShow(int id);
    private native void nHide(int id);
    private native void nSetAll(int id, int x, int y, int width, int height, float rotation, float[] color, boolean visible);
    private native boolean nIsVisible(int id);
}
