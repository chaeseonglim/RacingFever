package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.View;

import java.util.HashMap;
import java.util.Iterator;

public class TrackView implements View {

    private String LOG_TAG = "TrackView";

    public TrackView(TrackData trackData, float scale) {
        this.trackData = trackData;
        this.sprites = new HashMap<CoordKey, Sprite>();
        this.tileSize = new Size((int) (TILE_WIDTH*scale), (int) (TILE_HEIGHT*scale));
        update();
    }

    private Rect getCachedRegion() {
        Rect cachedRegion = new Rect(Engine2D.GetInstance().getViewport());

        // Adding gaps to viewport for caching more sprites around
        cachedRegion.x = Math.max(0, cachedRegion.x - tileSize.width *2);
        cachedRegion.width += tileSize.width * 4;
        cachedRegion.y = Math.max(0, cachedRegion.y - tileSize.height *2);
        cachedRegion.height += tileSize.height * 4;

        return cachedRegion;
    }

    private void cleanupUnusedSprites() {
        Rect cachedRegion = getCachedRegion();

        Iterator<HashMap.Entry<CoordKey, Sprite>> iter = sprites.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry<CoordKey, Sprite> entry = iter.next();
            CoordKey key = entry.getKey();
            Rect spriteRect = new Rect(key.getX()* tileSize.width, key.getY()* tileSize.height,
                    tileSize.width, tileSize.height);
            if (!Rect.intersects(cachedRegion, spriteRect)) {
                Sprite sprite = entry.getValue();
                sprite.close();
                iter.remove();
            }
        }
    }

    @Override
    public void update() {
        if (!visible)
            return;

        // clean up unused spries
        cleanupUnusedSprites();

        // build up sprites
        Rect cachedRegion = getCachedRegion();
        Size trackDataSize = trackData.getSize();

        for (int y = cachedRegion.top() / tileSize.height;
             y < Math.min(cachedRegion.bottom() / tileSize.height, trackDataSize.height);
             ++y) {
            for (int x = cachedRegion.left() / tileSize.width;
                 x < Math.min(cachedRegion.right() / tileSize.width, trackDataSize.width);
                 ++x) {
                if (sprites.get(new CoordKey(x, y)) != null)
                    continue;

                String spriteName;

                TrackData.TileType tileType = trackData.getTileType(new Point(x, y));

                // grass
                if (tileType == TrackData.TileType.GRASS) {
                    spriteName = "map_tile-2.png";
                }
                // starting
                else if (tileType == TrackData.TileType.START) {
                    spriteName = "map_tile-1.png";
                }
                // end
                else if (tileType == TrackData.TileType.END) {
                    spriteName = "map_tile-1.png";
                }
                // road
                else {
                    spriteName = "map_tile-1.png";
                }

                Sprite.Builder spriteBuilder =
                    new Sprite.Builder(spriteName)
                            .position(new Point(
                                    x * tileSize.width + tileSize.width /2,
                                    y * tileSize.height + tileSize.height /2))
                            .size(new Size(tileSize.width, tileSize.height))
                            .layer(MAP_LAYER).visible(true);
                Sprite sprite = spriteBuilder.build();
                sprites.put(new CoordKey(x, y), sprite);
            }
        }
    }

    @Override
    public void commit() {
        if (!visible)
            return;

        for (HashMap.Entry<CoordKey, Sprite> entry : sprites.entrySet()) {
            entry.getValue().commit();
        }
    }

    @Override
    public void show() {
        visible = true;
        for (HashMap.Entry<CoordKey, Sprite> entry : sprites.entrySet()) {
            entry.getValue().show();
        }
    }

    @Override
    public void hide() {
        visible = false;
        for (HashMap.Entry<CoordKey, Sprite> entry : sprites.entrySet()) {
            entry.getValue().hide();
        }
    }

    @Override
    public Size getSize() {
        return new Size(trackData.getSize()).multiply(tileSize.width, tileSize.height);
    }

    public Rect getScreenRegionOfMapDataCoord(Point pt) {
        return new Rect(pt.x*tileSize.width, pt.y*tileSize.height,
                tileSize.width, tileSize.height);
    }

    private final int MAP_LAYER = 0;
    private final int TILE_WIDTH = 32, TILE_HEIGHT = 32;

    private TrackData trackData;
    private HashMap<CoordKey, Sprite> sprites;
    private boolean visible;
    private Size tileSize;
}
