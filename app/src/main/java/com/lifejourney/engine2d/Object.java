package com.lifejourney.engine2d;

public class Object {

    private static String LOG_TAG = "Object";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends Builder<T>> {
        // Required parameters
        protected PointF position;

        // Optional parameters - initialized to default values
        protected Size size = new Size();
        protected float rotation = 0.0f;
        protected int layer = 1;
        protected float depth = 0.0f;
        protected String asset = new String();
        protected boolean visible = false;

        public Builder(PointF position) {
            this.position = position;
        }
        public T size(Size size) {
            this.size = size;
            return (T)this;
        }
        public T depth(float depth) {
            this.depth = depth;
            return (T)this;
        }
        public T rotation(float rotation) {
            this.rotation = rotation;
            return (T)this;
        }
        public T asset(String asset) {
            this.asset = asset;
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
        position = builder.position;
        size = builder.size;
        rotation = builder.rotation;
        layer = builder.layer;
        depth = builder.depth;
        visible = builder.visible;
        Sprite.Builder spriteBuilder =
                new Sprite.Builder(builder.asset)
                        .position(new Point(position)).size(size).rotation(rotation).layer(layer).depth(depth);
        sprite = spriteBuilder.build();
    }

    public void update() {
    }

    public void commit() {
        if (sprite != null) {
            sprite.set(new Point(position), size, layer, depth, rotation, visible);
            sprite.commit();
        }
    }

    public PointF getPosition() { return position; }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public Size getSize() { return size; }

    public void setSize(Size size) {
        this.size = size;
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

    protected PointF position;
    protected Size size;
    protected float rotation;
    protected int layer;
    protected float depth;
    protected Sprite sprite;
    protected boolean visible;
}
