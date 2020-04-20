package com.lifejourney.racingfever;

public class Sprite {

    public Sprite(int x, int y, int width, int height, float rotation, String textureName, float[] color) {
        mID = nCreateSprite(x, y, width, height, rotation, textureName, color);
    }
    public Sprite(int x, int y, int width, int height, float rotation, String textureName) {
        mID = nCreateSprite(x, y, width, height, rotation, textureName, new float[]{1.0f, 1.0f, 1.0f});
    }
    public void finalize() {
        nDestorySprite(mID);
    }

    private native int nCreateSprite(int x, int y, int width, int height, float rotation, String textureName, float[] color);
    private native void nDestorySprite(int id);

    public int mID;
}
