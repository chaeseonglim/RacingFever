package com.lifejourney.racingfever;

import android.graphics.Rect;
import android.util.Log;

public class Object {

    private static String LOG_TAG = "Object";

    public static class Builder {
        // Required parameters
        private Rect region;

        // Optional parameters - initialized to default values
        private float depth = 0.0f;
        private float rotation = 0.0f;
        private String spriteAsset;
        private float[] color = new float[] { 1.0f, 1.0f, 1.0f };;
        private boolean visible = false;

        public Builder(Rect region) {
            this.region = region;
        }

        public Builder depth(float depth) {
            this.depth = depth;
            return this;
        }
        public Builder rotation(int rotation) {
            this.rotation = rotation;
            return this;
        }
        public Builder spriteAsset(String spriteAsset) {
            this.spriteAsset = spriteAsset;
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
        public Object build() {
            return new Object(this);
        }
    }

    private Object(Builder builder) {
        region = builder.region;
        depth = builder.depth;
        rotation = builder.rotation;
        color = builder.color;
        visible = builder.visible;
        Sprite.Builder spriteBuilder =
                new Sprite.Builder(builder.spriteAsset)
                        .region(region).depth(depth).rotation(rotation).color(color);
        sprite = spriteBuilder.build();
    }

    public void update() {
    }

    void commit() {
        if (sprite != null) {
            sprite.set(region, depth, rotation, color, visible);
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

    private Rect region;
    private float depth;
    private float rotation;
    private Sprite sprite;
    private float[] color;
    private boolean visible;
}
