package com.lifejourney.racingfever;

public class MapData {

    public MapData(String mapAsset) {
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        GrayscaleBitmap bitmap = resourceManager.loadGrayscaleBitmap(mapAsset);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        grid = bitmap.get2DByteArray();
    }

    public byte[][] getGrid() {
        return grid;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private byte[][] grid;
    private int width;
    private int height;
}
