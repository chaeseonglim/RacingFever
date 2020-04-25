package com.lifejourney.racingfever;

import android.graphics.Rect;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapView {

    private String LOG_TAG = "MapView";

    public MapView(MapData map) {
        this.map = map;
        this.sprites = new HashMap<CoordKey, Sprite>();
        update();
    }

    private Rect getCachedRegion() {
        Rect cacheRegion = new Rect(Engine2D.GetInstance().getViewport());

        // Adding gaps to viewport for caching more sprites around
        cacheRegion.left = Math.max(0, cacheRegion.left - TILE_WIDTH);
        cacheRegion.right = cacheRegion.right + TILE_WIDTH;
        cacheRegion.top = Math.max(0, cacheRegion.top - TILE_HEIGHT);
        cacheRegion.bottom = cacheRegion.bottom + TILE_HEIGHT;

        return cacheRegion;
    }

    private void cleanupUnusedSprites() {
        Rect cachedRegion = getCachedRegion();

        Iterator<HashMap.Entry<CoordKey, Sprite>> iter = sprites.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry<CoordKey, Sprite> entry = iter.next();
            CoordKey key = entry.getKey();
            Rect spriteRect = new Rect(key.getX()*TILE_WIDTH, key.getY()*TILE_HEIGHT,
                    (key.getX()+1)*TILE_WIDTH, (key.getY()+1)*TILE_HEIGHT);
            if (!Rect.intersects(cachedRegion, spriteRect)) {
                Sprite sprite = entry.getValue();
                sprite.close();
                iter.remove();
            }
        }
    }

    public void update() {
        // clean up unused spries
        cleanupUnusedSprites();

        // build up sprites
        Rect cachedRegion = getCachedRegion();
        byte[][] grid = map.getGrid();

        for (int y = cachedRegion.top / TILE_HEIGHT;
             y < Math.min(cachedRegion.bottom / TILE_HEIGHT, map.getHeight());
             ++y) {
            for (int x = cachedRegion.left / TILE_WIDTH;
                 x < Math.min(cachedRegion.right / TILE_WIDTH, map.getWidth());
                 ++x) {
                if (sprites.get(new CoordKey(x, y)) != null)
                    continue;

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

                Sprite.Builder spriteBuilder =
                        new Sprite.Builder(spriteName)
                                .region(new Rect(x * TILE_WIDTH, y * TILE_HEIGHT, (x + 1) * TILE_WIDTH, (y + 1) * TILE_HEIGHT))
                                .depth(MAP_DEPTH).visible(visible);
                Sprite sprite = spriteBuilder.build();
                sprites.put(new CoordKey(x, y), sprite);
            }
        }
    }

    public void commit() {
        for (HashMap.Entry<CoordKey, Sprite> entry : sprites.entrySet()) {
            entry.getValue().commit();
        }
    }

    public void show() {
        visible = true;
        for (HashMap.Entry<CoordKey, Sprite> entry : sprites.entrySet()) {
            entry.getValue().show();
        }
    }

    public void hide() {
        visible = false;
        for (HashMap.Entry<CoordKey, Sprite> entry : sprites.entrySet()) {
            entry.getValue().hide();
        }
    }

    private final int TILE_WIDTH = 320, TILE_HEIGHT = 320;
    private final float MAP_DEPTH = 0.0f;

    private MapData map;
    private HashMap<CoordKey, Sprite> sprites;
    private boolean visible;
}
