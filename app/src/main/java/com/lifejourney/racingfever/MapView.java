package com.lifejourney.racingfever;

import android.util.Log;

public class MapView {

    private String LOG_TAG = "MapView";

    public MapView(MapData map) {
        this.map = map;

        initResources();
    }

    public void initResources() {
        // Create sprites
        sprites = new Sprite[map.getHeight()][map.getWidth()];
        byte[][] grid = map.getGrid();
        int tileWidth = 32, tileHeight = 32;
        for (int y = 0; y < /*map.getHeight()*/50; ++y) {
            for (int x = 0; x < /*map.getWidth()*/50; ++x) {
                String sprite;

                // road
                if (grid[y][x] == 0x00) {
                    sprite = "map_tile-1.png";
                }
                // starting
                else if (grid[y][x] == 0x82) {
                    sprite = "map_tile-1.png";
                }
                // grass
                else {
                    sprite = "map_tile-2.png";
                }
                sprites[y][x] =
                        new Sprite(x * tileWidth, y * tileHeight, tileWidth, tileHeight, 0.0f,
                                new float[]{1.0f, 1.0f, 1.0f}, sprite);
                sprites[y][x].show();
            }
        }
    }

    private MapData map;
    private Sprite[][] sprites;
}
