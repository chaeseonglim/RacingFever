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
        private Size gridSize = new Size(1, 1);

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
        public Builder gridSize(Size gridSize) {
            this.gridSize = gridSize;
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
        gridSize    = builder.gridSize;
        gridIndex   = new Point();

        load();
    }

    /**
     *
     * @return
     */
    public boolean load() {
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        if (!resourceManager.loadTexture(asset)) {
            Log.e(LOG_TAG, "Failed to load texture");
            return false;
        }

        id = nCreateSprite(asset, gridSize.width, gridSize.height);
        if (id == INVALID_ID) {
            Log.e(LOG_TAG, "Failed to create sprite");
            return false;
        }

        return true;
    }

    /**
     *
     */
    public void close() {
        if (id != INVALID_ID) {
            nDestroySprite(id);
            id = INVALID_ID;
        }
    }

    /**
     *
     */
    public void finalize() {
        if (id != INVALID_ID) {
            Log.w(LOG_TAG, "A sprite " + id + " is not properly closed");
            nDestroySprite(id);
        }
    }

    /**
     *
     */
    public void commit() {
        nSetProperties(id, position.x, position.y, size.width, size.height, layer, depth,
                rotation, visible, gridIndex.x, gridIndex.y);
    }

    /**
     *
     * @return
     */
    public Point getPos() {
        return position;
    }

    /**
     *
     * @param position
     */
    public void setPos(Point position) {
        this.position = position;
    }

    /**
     *
     * @return
     */
    public Size getSize() {
        return size;
    }

    /**
     *
     * @param size
     */
    public void setSize(Size size) {
        this.size = size;
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
    public String getAsset() {
        return asset;
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
    public Size getGridSize() {
        return gridSize;
    }

    /**
     *
     * @return
     */
    public Point getGridIndex() {
        return gridIndex;
    }

    /**
     *
     * @param gridIndex
     */
    public void setGridIndex(Point gridIndex) {
        this.gridIndex = gridIndex;
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
    private Size gridSize;
    private Point gridIndex;

    private native int nCreateSprite(String asset, int gridCols, int gridRows);
    private native void nDestroySprite(int id);
    private native void nSetProperties(int id, int x, int y, int width, int height, int layer,
                                       float depth, float rotation, boolean visible, int gridCol,
                                       int gridRow);
}
