package com.lifejourney.racingfever;

import android.graphics.Rect;
import android.util.Log;

public class Object {

    private static String LOG_TAG = "Object";

    public static class Builder<T extends Builder<T>> {
        // Required parameters
        private Rect region;

        // Optional parameters - initialized to default values
        private int layer = 1;
        private float depth = 0.0f;
        private float rotation = 0.0f;
        private String spriteAsset;
        private float[] color = new float[] { 1.0f, 1.0f, 1.0f };
        private boolean visible = false;

        public Builder(Rect region) {
            this.region = region;
        }
        public T depth(float depth) {
            this.depth = depth;
            return (T)this;
        }
        public T rotation(int rotation) {
            this.rotation = rotation;
            return (T)this;
        }
        public T spriteAsset(String spriteAsset) {
            this.spriteAsset = spriteAsset;
            return (T)this;
        }
        public T color(float[] color) {
            this.color = color;
            return (T)this;
        }
        public T layer(int layer) {
            this.layer = layer;
            return (T)this;
        }
        public T visible(boolean visible) {
            this.visible = visible;
            return (T)this;
        }
        public Object build() {
            return new Object(this);
        }
    }

    protected Object(Builder builder) {
        layer = builder.layer;
        region = builder.region;
        depth = builder.depth;
        rotation = builder.rotation;
        color = builder.color;
        visible = builder.visible;
        Sprite.Builder spriteBuilder =
                new Sprite.Builder(builder.spriteAsset)
                        .region(region).layer(layer).depth(depth).rotation(rotation).color(color);
        sprite = spriteBuilder.build();
    }

    public void update() {
    }

    void commit() {
        if (sprite != null) {
            sprite.set(region, layer, depth, rotation, color, visible);
            sprite.commit();
        }
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
        region.offsetTo(region.left, y);
    }

    public void setPos(int x, int y) {
        region.offsetTo(x,y);
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public int getWidth() { return region.width(); }

    public void setWidth(int width) {
        region.right = region.left + width;
    }

    public int getHeight() {
        return region.height();
    }

    public void setHeight(int height) {
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

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
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

    protected int layer;
    protected Rect region;
    protected float depth;
    protected float rotation;
    protected Sprite sprite;
    protected float[] color;
    protected boolean visible;
}
