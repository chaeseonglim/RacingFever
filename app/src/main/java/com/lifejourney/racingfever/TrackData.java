package com.lifejourney.racingfever;

public class TrackData {

    public TrackData(String mapAsset) {
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        GrayscaleBitmap bitmap = resourceManager.loadGrayscaleBitmap(mapAsset);
        size = new Size(bitmap.getWidth(), bitmap.getHeight());
        grid = bitmap.get2DByteArray();
    }

    public byte[][] getGrid() {
        return grid;
    }

    public Size getSize() { return size; }

    private byte[][] grid;
    private Size size;
}
