package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.RectF;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.Vector2D;
import com.lifejourney.engine2d.View;

import java.util.ArrayList;
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

                TrackData.TileType tileType = trackData.getTileType(new Point(x, y));

                Point textureGrid;

                // grass
                if (tileType == TrackData.TileType.GRASS) {
                    textureGrid = new Point(0, 1);
                }
                // starting
                else if (tileType == TrackData.TileType.START) {
                    textureGrid = new Point(0, 0);
                }
                // end
                else if (tileType == TrackData.TileType.END) {
                    textureGrid = new Point(0, 0);
                }
                // road
                else {
                    textureGrid = new Point(0, 0);
                }

                Sprite.Builder spriteBuilder =
                    new Sprite.Builder("map_tile.png")
                            .position(new Point(
                                    x * tileSize.width + tileSize.width /2,
                                    y * tileSize.height + tileSize.height /2))
                            .size(new Size(tileSize.width, tileSize.height))
                            .gridSize(new Size(1, 2))
                            .layer(MAP_LAYER).visible(true);
                Sprite sprite = spriteBuilder.build();
                sprite.setGridIndex(textureGrid);
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

    public RectF getScreenRegionfromTrackCoord(Point pt) {
        return new RectF(pt.x*tileSize.width, pt.y*tileSize.height,
                tileSize.width, tileSize.height);
    }

    public Point getTrackCoordFromScreenCoord(PointF pt) {
        return new Point(pt).divide(new Point(tileSize.width, tileSize.height));
    }

    public ArrayList<Point> getRaytracedTileList(PointF start, float direction, float maxDistance) {
        // Find end point first
        Vector2D endVector =
                new Vector2D(direction).multiply(maxDistance).add(start.vectorize());
        PointF end = new PointF(endVector);

        return getRaytracedTileList(start, end);
    }

    public ArrayList<Point> getRaytracedTileList(PointF start, PointF end) {
        PointF startpoint = new PointF(start).divide(new PointF(tileSize.width, tileSize.height));
        PointF endpoint = new PointF(end).divide(new PointF(tileSize.width, tileSize.height));

        // Raytracing code
        // http://playtechs.blogspot.com/2007/03/raytracing-on-grid.html
        float dx = Math.abs(endpoint.x - startpoint.x);
        float dy = Math.abs(endpoint.y - startpoint.y);

        int x = (int)(Math.floor(startpoint.x));
        int y = (int)(Math.floor(startpoint.y));

        float dt_dx = 1.0f / dx;
        float dt_dy = 1.0f / dy;

        float t = 0;

        int n = 1;
        int x_inc, y_inc;
        float t_next_vertical, t_next_horizontal;

        if (dx == 0) {
            x_inc = 0;
            t_next_horizontal = dt_dx; // infinity
        }
        else if (endpoint.x > startpoint.x) {
            x_inc = 1;
            n += (int)(Math.floor(endpoint.x)) - x;
            t_next_horizontal = (float) (Math.floor(startpoint.x) + 1 - startpoint.x) * dt_dx;
        }
        else {
            x_inc = -1;
            n += x - (int)(Math.floor(endpoint.x));
            t_next_horizontal = (float) (startpoint.x - Math.floor(startpoint.x)) * dt_dx;
        }

        if (dy == 0) {
            y_inc = 0;
            t_next_vertical = dt_dy; // infinity
        }
        else if (endpoint.y > startpoint.y) {
            y_inc = 1;
            n += (int) (Math.floor(endpoint.y)) - y;
            t_next_vertical = (float) (Math.floor(startpoint.y) + 1 - startpoint.y) * dt_dy;
        }
        else {
            y_inc = -1;
            n += y - (int)(Math.floor(endpoint.y));
            t_next_vertical = (float) (startpoint.y - Math.floor(startpoint.y)) * dt_dy;
        }

        ArrayList<Point> points = new ArrayList<>();
        for (; n > 0; --n) {
            if (x < 0 || y < 0 ||
                    x >= trackData.getSize().width ||
                    y >= trackData.getSize().height) {
                continue;
            }

            // x, y
            points.add(new Point(x, y));

            if (t_next_vertical < t_next_horizontal) {
                y += y_inc;
                t = t_next_vertical;
                t_next_vertical += dt_dy;
            }
            else {
                x += x_inc;
                t = t_next_horizontal;
                t_next_horizontal += dt_dx;
            }
        }

        return points;
    }

    public Size getTileSize() {
        return tileSize;
    }

    private final int MAP_LAYER = 0;
    private final int TILE_WIDTH = 32, TILE_HEIGHT = 32;

    private TrackData trackData;
    private HashMap<CoordKey, Sprite> sprites;
    private boolean visible;
    private Size tileSize;
}
