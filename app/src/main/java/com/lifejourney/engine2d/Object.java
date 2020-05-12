package com.lifejourney.engine2d;

import android.util.Log;

public class Object implements Comparable<Object> {

    private static String LOG_TAG = "Object";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends Builder<T>> {
        // Required parameters
        protected PointF position;

        // Optional parameters
        protected float rotation = 0.0f;
        protected int layer = 1;
        protected float depth = 0.0f;
        protected Sprite sprite;
        protected boolean visible = false;
        protected int updatePeriod = 3;

        public Builder(PointF position) {
            this.position = position;
        }
        public T depth(float depth) {
            this.depth = depth;
            return (T)this;
        }
        public T rotation(float rotation) {
            this.rotation = rotation;
            return (T)this;
        }
        public T sprite(Sprite sprite) {
            this.sprite = sprite;
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
        public T updatePeriod(int updatePeriod) {
            this.updatePeriod = updatePeriod;
            return (T)this;
        }
        public Object build() {
            return new Object(this);
        }
    }

    protected Object(Builder builder) {
        position = builder.position;
        rotation = builder.rotation;
        layer = builder.layer;
        depth = builder.depth;
        visible = builder.visible;
        sprite = builder.sprite;
        updatePeriod = builder.updatePeriod;
    }

    public void update() {
        if (updatePeriodLeft == 0) {
            updatePeriodLeft = updatePeriod;
        }
        else {
            updatePeriodLeft--;
        }
    }

    public void commit() {
        if (sprite != null) {
            sprite.set(new Point(position), new Size(), layer, depth, rotation, visible);
            sprite.commit();
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o == this)
            return 0;
        else
            return 1;
    }

    public PointF getPosition() { return position; }

    public Vector2D getPositionVector() {
        return new Vector2D(position.x, position.y);
    }

    public void setPosition(PointF position) {
        this.position = position;
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

    public PointF center() {
        return position;
    }

    public void setUpdatePeriod(int updatePeriod) {
        this.updatePeriod = updatePeriod;
    }

    public int getUpdatePeriod() {
        return updatePeriod;
    }

    public int getUpdatePeriodLeft() {
        return updatePeriodLeft;
    }

    public void setUpdatePeriodLeft(int updatePeriodLeft) {
        this.updatePeriodLeft = updatePeriodLeft;
    }

    public boolean isUpdatePossible() {
        return (updatePeriodLeft == 0);
    }

    private PointF position;
    private float rotation;
    private int layer;
    private float depth;
    private Sprite sprite;
    private boolean visible;

    private int updatePeriod;
    private int updatePeriodLeft = 0;
}
