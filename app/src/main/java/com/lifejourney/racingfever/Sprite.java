package com.lifejourney.racingfever;

import android.graphics.Rect;
import android.util.Log;

public class Sprite {

    private static String LOG_TAG = "Sprite";

    public static class Builder {
        // required parameter
        private String asset;

        // optional
        private Rect region;
        private int layer = 0;
        private float depth = 0.0f;
        private float rotation = 0.0f;
        private float[] color = new float[] { 1.0f, 1.0f, 1.0f };
        private boolean visible = false;

        public Builder(String asset) {
            this.asset = asset;
        }
        public Builder region(Rect region) {
            this.region = region;
            return this;
        }
        public Builder layer(int layer) {
            this.layer = layer;
            return this;
        }
        public Builder depth(float depth) {
            this.depth = depth;
            return this;
        }
        public Builder rotation(float rotation) {
            this.rotation = rotation;
            return this;
        }
        public Builder color(float[] color) {
            this.color = color;
            return this;
        }
        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }
        public Sprite build() {
            return new Sprite(this);
        }
    };

    private Sprite(Builder builder) {
        region = builder.region;
        layer = builder.layer;
        depth = builder.depth;
        rotation = builder.rotation;
        color = builder.color;
        asset = builder.asset;
        visible = builder.visible;
        layer = builder.layer;

        load();
    }

    public boolean load() {
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        if (!resourceManager.loadTexture(asset)) {
            Log.e(LOG_TAG, "Failed to load texture");
            return false;
        }

        id = nCreateSprite(asset, layer);
        if (id == INVALID_ID) {
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

    void commit() {
        nSetProperties(id, region.left, region.top, region.width(), region.height(), layer, depth,
                rotation, color, visible);
    }

    public int getX() {
        return region.left;
    }

    public void setX(int x) {
        region.offsetTo(x, region.top);
    }

    public int getY() {
        return region.top;
    }

    public void setY(int y) {
        region.offsetTo(region.top, y);
    }

    public void setPos(int x, int y) {
        region.offsetTo(x, y);
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public int getWidth() {
        return region.width();
    }

    public void setWidth(int width) {
        region.right = region.left + width;
    }

    public int getHeight() { return region.height(); }

    public void setHeight(int height) {
        region.bottom = region.top + height;
    }

    public void setSize(int width, int height) {
        region.right = region.left + width;
        region.bottom = region.top + height;
    }

    public Rect getRegion() { return region; }

    public void setRegion(Rect region) {
        this.region = region;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public String getAsset() {
        return asset;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public void show() {
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void set(Rect region, int layer, float depth, float rotation, float[] color, boolean visible) {
        this.region = region;
        this.layer = layer;
        this.depth = depth;
        this.rotation = rotation;
        this.color = color;
        this.visible = visible;
    }

    private final int INVALID_ID = -1;

    private int id;
    private int layer;
    private Rect region;
    private float depth;
    private float rotation;
    private String asset;
    private float[] color;
    private boolean visible;

    private native int nCreateSprite(String asset, int layer);
    private native void nDestorySprite(int id);
    private native void nSetProperties(int id, int x, int y, int width, int height, int layer,
                                       float depth, float rotation, float[] color, boolean visible);
}
