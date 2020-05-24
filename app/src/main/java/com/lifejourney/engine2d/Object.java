package com.lifejourney.engine2d;

import android.util.Log;

import com.lifejourney.racingfever.Car;

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
        updatePeriodLeft = (int) (Math.random()%updatePeriod);
    }

    /**
     *
     */
    public void close() {
        if (sprite != null) {
            sprite.close();
        }
    }

    /**
     *
     */
    public void update() {
        if (updatePeriodLeft == 0) {
            updatePeriodLeft = updatePeriod;
        }
        else {
            updatePeriodLeft--;
        }
    }

    /**
     *
     */
    public void commit() {
        if (sprite != null) {
            sprite.setPos(new Point(position));
            sprite.setLayer(layer);
            sprite.setDepth(depth);
            sprite.setRotation(rotation);
            sprite.setVisible(visible);
            sprite.commit();
        }
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        if (o == this)
            return 0;
        else
            return 1;
    }

    /**
     *
     * @return
     */
    public PointF getPosition() { return position; }

    /**
     *
     * @return
     */
    public Vector2D getPositionVector() {
        return new Vector2D(position.x, position.y);
    }

    /**
     *
     * @param position
     */
    public void setPosition(PointF position) {
        this.position = position;
    }

    /**
     *
     * @return
     */
    public float getRotation() {
        return rotation;
    }

    /**
     *
     * @param rotation
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     *
     * @return
     */
    public Sprite getSprite() {
        return sprite;
    }

    /**
     *
     * @param sprite
     */
    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    /**
     *
     * @return
     */
    public int getLayer() {
        return layer;
    }

    /**
     *
     * @param layer
     */
    public void setLayer(int layer) {
        this.layer = layer;
    }

    /**
     *
     * @return
     */
    public float getDepth() {
        return depth;
    }

    /**
     *
     * @param depth
     */
    public void setDepth(float depth) {
        this.depth = depth;
    }

    /**
     *
     */
    public void show() {
        this.visible = true;
    }

    /**
     *
     */
    public void hide() {
        this.visible = false;
    }

    /**
     *
     * @return
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     *
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     *
     * @return
     */
    public PointF center() {
        return position;
    }

    /**
     *
     * @param updatePeriod
     */
    public void setUpdatePeriod(int updatePeriod) {
        this.updatePeriod = updatePeriod;
    }

    /**
     *
     * @return
     */
    public int getUpdatePeriod() {
        return updatePeriod;
    }

    /**
     *
     * @return
     */
    public int getUpdatePeriodLeft() {
        return updatePeriodLeft;
    }

    /**
     *
     * @param updatePeriodLeft
     */
    public void setUpdatePeriodLeft(int updatePeriodLeft) {
        this.updatePeriodLeft = updatePeriodLeft;
    }

    /**
     *
     * @return
     */
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
    private int updatePeriodLeft;
}
