package com.lifejourney.racingfever;

import android.util.Log;

import java.util.HashMap;

public class MapView {

    private String LOG_TAG = "MapView";

    public MapView(MapData map) {
        this.map = map;
        this.sprites = new HashMap<CoordKey, Sprite>();
        update();
    }

    private void cleanupUnusedSprites() {
        Rect viewport = Engine2D.GetInstance().getViewport();

        // Current approach : tight managing
        for (CoordKey key : sprites.keySet()) {
            if (key.getX() < viewport.getX() ||
                    key.getX() > viewport.getX() + viewport.getWidth() ||
                    key.getY() < viewport.getY() ||
                    key.getY() > viewport.getY() + viewport.getHeight()) {
                sprites.remove(key);
            }
        }
    }

    public void update() {
        Rect viewport = Engine2D.GetInstance().getViewport();

        // build up sprites
        byte[][] grid = map.getGrid();

        for (int y = viewport.getY()/TILE_HEIGHT;
             y < Math.min((viewport.getY()+viewport.getHeight())/TILE_HEIGHT, map.getHeight());
             ++y) {
            for (int x = viewport.getX()/TILE_WIDTH;
                 x < Math.min((viewport.getX()+viewport.getWidth())/TILE_WIDTH, map.getWidth());
                 ++x) {
                String spriteName;
                // road
                if (grid[y][x] == 0x00) {
                    spriteName = "map_tile-1.png";
                }
                // starting
                else if (grid[y][x] == 0x82) {
                    spriteName = "map_tile-1.png";
                }
                // grass
                else {
                    spriteName = "map_tile-2.png";
                }
                sprites.put(new CoordKey(x, y),
                        new Sprite(x * TILE_WIDTH, y * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT,
                                0.0f, new float[]{1.0f, 1.0f, 1.0f}, spriteName));
            }
        }
    }

    public void show() {
        for (CoordKey key : sprites.keySet()) {
            sprites.get(key).show();
        }
    }

    public void hide() {
        for (CoordKey key : sprites.keySet()) {
            sprites.get(key).hide();
        }
    }

    private final int TILE_WIDTH = 32, TILE_HEIGHT = 32;

    private MapData map;
    private HashMap<CoordKey, Sprite> sprites;
}
