package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.InfoBitmap;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.ResourceManager;
import com.lifejourney.engine2d.Size;

import java.util.ArrayList;

class TrackData {

    private static final String LOG_TAG = "TrackData";

    enum TileType {
        GRASS((byte)0xff, false),
        START((byte)0x82, true),
        END((byte)0x5a, true),
        ROAD((byte)0x00, true),
        FINISH((byte)0xb2, true),
        UNKNOWN((byte)0x0fe, false);

        TileType(byte code, boolean movable) {
            this.code = code;
            this.movable = movable;
        }

        byte code() {
            return code;
        }

        boolean movable() {
            return movable;
        }

        private final byte code;
        private final boolean movable;
    }

    /**
     *
     * @param mapAsset
     */
    TrackData(String mapAsset) {
        this.mapAsset = mapAsset;

        // Load map data from bitmap (grayscale png)
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        InfoBitmap bitmap = resourceManager.loadGrayscaleBitmap(mapAsset);
        grid = bitmap.get2DByteArray();
        size = new Size(bitmap.getWidth(), bitmap.getHeight());

        // Retrieving starting points & end points
        startPoints = new ArrayList<>();
        endPoints = new ArrayList<>();
        finishPoints = new ArrayList<>();
        for (int y = 0; y < size.height; ++y) {
            for (int x = 0; x < size.width; ++x) {
                if (grid[y][x] == TileType.START.code()) {
                    startPoints.add(new Point(x, y));
                }
                else if (grid[y][x] == TileType.END.code()) {
                    endPoints.add(new Point(x, y));
                }
                else if (grid[y][x] == TileType.FINISH.code()) {
                    finishPoints.add(new Point(x, y));
                }
            }
        }

        if (startPoints.size() == 0) {
            throw new IllegalArgumentException("There's no starting point in this map!!!");
        }
        if (endPoints.size() != 2) {
            throw new IllegalArgumentException("There are not suitable end points in this map!!!");
        }
    }

    /**
     *
     * @param pt
     * @return
     */
    TileType getTileType(Point pt) {
        Rect trackRegion = new Rect(new Point(), getSize());
        if (!trackRegion.includes(pt)) {
            return TileType.UNKNOWN;
        }

        byte code = grid[pt.y][pt.x];
        for (TileType type : TileType.values()) {
            if (type.code() == code) {
                return type;
            }
        }

        Log.e(LOG_TAG, "Unknown tile type!!! " + code);
        return TileType.UNKNOWN;
    }

    /**
     *
     * @param pt
     * @return
     */
    boolean isMovable(Point pt) {
        Rect trackRegion = new Rect(new Point(), getSize());
        if (!trackRegion.includes(pt)) {
            return false;
        }

        return getTileType(pt).movable();
    }

    /**
     *
     * @param pt
     * @return
     */
    boolean isSearchable(Point pt) {
        Rect trackRegion = new Rect(new Point(), getSize());
        if (!trackRegion.includes(pt)) {
            return false;
        }

        TileType type = getTileType(pt);
        return (type != TileType.FINISH && type.movable());
    }

    /**
     *
     * @return
     */
    Size getSize() {
        return size;
    }

    /**
     *
     * @return
     */
    int getStartPointCount() {
        return startPoints.size();
    }

    /**
     *
     * @param index
     * @return
     */
    Point getStartPoint(int index) {
        return startPoints.get(index);
    }

    /**
     *
     * @return
     */
    int getEndPointCount() {
        return endPoints.size();
    }

    /**
     *
     * @param index
     * @return
     */
    Point getEndPoint(int index) {
        return endPoints.get(index);
    }

    /**
     *
     * @return
     */
    Point getMidEndPoint() {
        return new Point(getEndPoint(0)).add(getEndPoint(1)).multiply(0.5f);
    }

    /**
     *
     * @return
     */
    int getFinishPointCount() {
        return finishPoints.size();
    }

    /**
     *
     * @param index
     * @return
     */
    Point getFinishPoint(int index) {
        return finishPoints.get(index);
    }

    String getMapAsset() {
        return mapAsset;
    }

    private String mapAsset;
    private byte[][] grid;
    private Size size;
    private ArrayList<Point> startPoints;
    private ArrayList<Point> endPoints;
    private ArrayList<Point> finishPoints;
}
