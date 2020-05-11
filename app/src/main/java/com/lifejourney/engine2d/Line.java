package com.lifejourney.engine2d;

import android.util.Log;

public class Line {

    private static String LOG_TAG = "Line";

    public static class Builder {
        // required parameter
        private PointF begin;
        private PointF end;

        // optional
        private int layer = 0;
        private float r = 0.0f;
        private float g = 0.0f;
        private float b = 0.0f;
        private float a = 1.0f;
        private float depth = 0.0f;
        private boolean visible = false;

        public Builder(PointF begin, PointF end) {
            this.begin = begin;
            this.end = end;
        }
        public Builder color(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
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
        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }
        public Line build() {
            return new Line(this);
        }
    };

    private Line(Builder builder) {
        begin       = builder.begin;
        end         = builder.end;
        r           = builder.r;
        g           = builder.g;
        b           = builder.b;
        a           = builder.a;
        layer       = builder.layer;
        depth       = builder.depth;
        visible     = builder.visible;

        load();
    }

    public boolean load() {
        id = nCreateLine(begin.x, begin.y, end.x, end.y, r, g, b, a, layer);
        if (id == INVALID_ID) {
            Log.e(LOG_TAG, "Failed to create line");
            return false;
        }

        return true;
    }

    public void close() {
        if (id != INVALID_ID) {
            nDestoryLine(id);
            id = INVALID_ID;
        }
    }

    public void finalize() {
        if (id != INVALID_ID) {
            Log.w(LOG_TAG, "A line " + id + " is not properly closed");
            nDestoryLine(id);
        }
    }

    public void commit() {
        nSetProperties(id, begin.x, begin.y, end.x, end.y, r, g, b, a, layer, depth, visible);
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

    public void set(PointF begin, PointF end) {
        this.begin = begin;
        this.end = end;
    }

    public void set(PointF begin, PointF end, float r, float g, float b, float a,
                    int layer, float depth, boolean visible) {
        this.begin = begin;
        this.end = end;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.layer = layer;
        this.depth = depth;
        this.visible = visible;
    }

    private final int INVALID_ID = -1;

    private int id;
    private int layer;
    private PointF begin;
    private PointF end;
    private float r;
    private float g;
    private float b;
    private float a;
    private float depth;
    private boolean visible;

    private native int nCreateLine(float beginX, float beginY, float endX, float endY,
                                   float r, float g, float b, float a, int layer);
    private native void nDestoryLine(int id);
    private native void nSetProperties(int id, float beginX, float beginY, float endX, float endY,
                                       float r, float g, float b, float a, int layer,
                                       float depth, boolean visible);
}
