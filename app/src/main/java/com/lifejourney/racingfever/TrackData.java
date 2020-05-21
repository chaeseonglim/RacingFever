package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.ResourceManager;
import com.lifejourney.engine2d.Size;

import java.util.ArrayList;

public class TrackData {

    private static final String LOG_TAG = "TrackData";

    public enum TileType {
        GRASS((byte)0xff, false),
        START((byte)0x82, true),
        END((byte)0x5a, true),
        ROAD((byte)0x00, true),
        ROAD_FINISH((byte)0xb2, true);

        TileType(byte code, boolean movable) {
            this.code = code;
            this.movable = movable;
        }

        byte getCode() {
            return code;
        }

        boolean isMovable() {
            return movable;
        }

        private byte code;
        private boolean movable;
    }

    public TrackData(String mapAsset) {
        // Load map data from bitmap (grayscale png)
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        InfoBitmap bitmap = resourceManager.loadGrayscaleBitmap(mapAsset);
        grid = bitmap.get2DByteArray();
        size = new Size(bitmap.getWidth(), bitmap.getHeight());

        // Retrieving starting points & end points
        startPoints = new ArrayList<>();
        endPoints = new ArrayList<>();
        for (int y = 0; y < size.height; ++y) {
            for (int x = 0; x < size.width; ++x) {
                if (grid[y][x] == TileType.START.getCode()) {
                    startPoints.add(new Point(x, y));
                }
                else if (grid[y][x] == TileType.END.getCode()) {
                    endPoints.add(new Point(x, y));
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

    public TileType getTileType(Point pt) {
        Rect trackRegion = new Rect(new Point(), getSize());
        if (!trackRegion.includes(pt)) {
            return TileType.GRASS;
        }

        byte code = grid[pt.y][pt.x];
        for (TileType type : TileType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }

        Log.e(LOG_TAG, "Unknown tile type!!! " + code);
        return TileType.GRASS;
    }

    public boolean isMovable(Point pt) {
        Rect trackRegion = new Rect(new Point(), getSize());
        if (!trackRegion.includes(pt)) {
            return false;
        }

        return getTileType(pt).isMovable();
    }

    public boolean isSearchable(Point pt) {
        Rect trackRegion = new Rect(new Point(), getSize());
        if (!trackRegion.includes(pt)) {
            return false;
        }

        TileType type = getTileType(pt);
        return (type != TileType.ROAD_FINISH && type.isMovable());
    }

    public Size getSize() {
        return size;
    }

    public int getStartPointCount() {
        return startPoints.size();
    }

    public Point getStartPoint(int index) {
        return startPoints.get(index);
    }

    public int getEndPointCount() {
        return endPoints.size();
    }

    public Point getEndPoint(int index) {
        return endPoints.get(index);
    }

    public Point getMidEndPoint() {
        return new Point(getEndPoint(0)).add(getEndPoint(1)).multiply(0.5f);
    }

    private byte[][] grid;
    private Size size;
    private ArrayList<Point> startPoints;
    private ArrayList<Point> endPoints;
}
