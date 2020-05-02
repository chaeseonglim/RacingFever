package com.lifejourney.engine2d;

import android.util.Log;

public class Sprite {

    private static String LOG_TAG = "Sprite";

    public static class Builder {
        // required parameter
        private String asset;

        // optional
        private Point position;
        private Size size;
        private int layer = 0;
        private float depth = 0.0f;
        private float rotation = 0.0f;
        private boolean visible = false;

        public Builder(String asset) {
            this.asset = asset;
        }
        public Builder position(Point position) {
            this.position = position;
            return this;
        }
        public Builder size(Size size) {
            this.size = size;
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
        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }
        public Sprite build() {
            return new Sprite(this);
        }
    };

    private Sprite(Builder builder) {
        position    = builder.position;
        size        = builder.size;
        layer       = builder.layer;
        depth       = builder.depth;
        rotation    = builder.rotation;
        asset       = builder.asset;
        visible     = builder.visible;

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

    public void commit() {
        nSetProperties(id, position.x, position.y, size.width, size.height, layer, depth,
                rotation, visible);
    }

    public Point getPos() {
        return position;
    }

    public void setPos(Point position) {
        this.position = position;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
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

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public String getAsset() {
        return asset;
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

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void set(Point position, Size size, int layer, float depth, float rotation, boolean visible) {
        this.position = position;
        this.size = size;
        this.layer = layer;
        this.depth = depth;
        this.rotation = rotation;
        this.visible = visible;
    }

    private final int INVALID_ID = -1;

    private int id;
    private int layer;
    private Point position;
    private Size size;
    private float rotation;
    private float depth;
    private String asset;
    private boolean visible;

    private native int nCreateSprite(String asset, int layer);
    private native void nDestorySprite(int id);
    private native void nSetProperties(int id, int x, int y, int width, int height, int layer,
                                       float depth, float rotation, boolean visible);
}
