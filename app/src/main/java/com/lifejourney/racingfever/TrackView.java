package com.lifejourney.racingfever;

import java.util.HashMap;
import java.util.Iterator;

public class TrackView extends View {

    private String LOG_TAG = "TrackView";

    public TrackView(TrackData trackData) {
        this.trackData = trackData;
        this.sprites = new HashMap<CoordKey, Sprite>();
        update();
    }

    private Rect getCachedRegion() {
        Rect cachedRegion = new Rect(Engine2D.GetInstance().getViewport());

        // Adding gaps to viewport for caching more sprites around
        cachedRegion.x = Math.max(0, cachedRegion.x - TILE_WIDTH);
        cachedRegion.width += TILE_WIDTH * 2;
        cachedRegion.y = Math.max(0, cachedRegion.y - TILE_HEIGHT);
        cachedRegion.height += TILE_HEIGHT * 2;

        return cachedRegion;
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
        byte[][] grid = trackData.getGrid();
        Size trackDataSize = trackData.getSize();

        for (int y = cachedRegion.top() / TILE_HEIGHT;
             y < Math.min(cachedRegion.bottom() / TILE_HEIGHT, trackDataSize.height);
             ++y) {
            for (int x = cachedRegion.left() / TILE_WIDTH;
                 x < Math.min(cachedRegion.right() / TILE_WIDTH, trackDataSize.width);
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
                            .position(new Point(
                                    x * TILE_WIDTH + TILE_WIDTH/2,
                                    y * TILE_HEIGHT + TILE_HEIGHT/2))
                            .size(new Size(TILE_WIDTH, TILE_HEIGHT))
                            .layer(MAP_LAYER).visible(visible);
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

    public Size getSize() {
        return new Size(trackData.getSize()).multiply(TILE_WIDTH, TILE_HEIGHT);
    }

    private final int TILE_WIDTH = 320, TILE_HEIGHT = 320;
    private final int MAP_LAYER = 0;

    private TrackData trackData;
    private HashMap<CoordKey, Sprite> sprites;
    private boolean visible;
}
