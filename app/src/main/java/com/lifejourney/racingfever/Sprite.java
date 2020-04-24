package com.lifejourney.racingfever;

import android.graphics.Rect;
import android.util.Log;

public class Sprite {

    private static String LOG_TAG = "Sprite";

    public Sprite(Rect region, float rotation, float[] color, String asset) {
        this.region = region;
        this.rotation = rotation;
        this.color = color;
        this.asset = asset;

        load();
    }

    public Sprite(int x, int y, int width, int height, float rotation, float[] color, String asset) {
        this.region = new Rect(x, y, x+width, y+height);
        this.rotation = rotation;
        this.color = color;
        this.asset = asset;

        load();
    }

    public Sprite(String asset) {
        this.region = new Rect();
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

        this.id = nCreateSprite(this.region.left, this.region.top, this.region.width(),
                this.region.height(), this.rotation, this.asset, this.color);
        if (this.id == INVALID_ID) {
            Log.e(LOG_TAG, "Failed to create sprite");
            return false;
        }

        return true;
    }

    public void close() {
        if (id != INVALID_ID) {
            nDestorySprite(id);
            id = INVALID_ID;
        }
    }

    public void finalize() {
        if (id != INVALID_ID) {
            Log.w(LOG_TAG, "A sprite " + id + " is not properly closed");
            nDestorySprite(id);
        }
    }

    public int getX() {
        return region.left;
    }

    public void setX(int x) {
        region.offsetTo(x, region.top);
        nSetPos(id, region.left, region.top);
    }

    public int getY() {
        return region.top;
    }

    public void setY(int y) {
        region.offsetTo(region.top, y);
        nSetPos(id, region.left, region.top);
    }

    public void setPos(int x, int y) {
        region.offsetTo(x, y);
        nSetPos(id, region.left, region.top);
    }

    public int getWidth() {
        return region.width();
    }

    public void setWidth(int width) {
        region.right = region.left + width;
        nSetSize(id, region.width(), region.height());
    }

    public int getHeight() { return region.height(); }

    public void setHeight(int height) {
        region.bottom = region.top + height;
        nSetSize(id, region.width(), region.height());
    }

    public void setSize(int width, int height) {
        region.right = region.left + width;
        region.bottom = region.top + height;
        nSetSize(id, region.width(), region.height());
    }

    public Rect getRegion() { return region; }

    public void setRegion(Rect region) {
        this.region = region;
        nSetPos(id, region.left, region.top);
        nSetSize(id, region.width(), region.height());
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

    public void set(Rect region, float rotation, float[] color, boolean visible) {
        this.region = region;
        this.rotation = rotation;
        this.color = color;
        this.visible = visible;
        nSetAll(id, region.left, region.top, region.width(), region.height(), rotation, color, visible);
    }

    public void set(int x, int y, int width, int height, float rotation, float[] color, boolean visible) {
        this.region = new Rect(x, y, x+width, y+height);
        this.rotation = rotation;
        this.color = color;
        this.visible = visible;
        nSetAll(id, region.left, region.top, region.width(), region.height(), rotation, color, visible);
    }

    private final int INVALID_ID = -1;

    private int id;
    private Rect region;
    private float rotation = 0.0f;
    private String asset;
    private float[] color = new float[] { 1.0f, 1.0f, 1.0f };
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
