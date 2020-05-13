package com.lifejourney.racingfever;

import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;

import java.util.ArrayList;

public class Track {

    public Track(String mapAsset, float scale) {
        data = new TrackData(mapAsset);
        view = new TrackView(data, scale);
        view.show();

        TrackPathFinder pathFinder = new TrackPathFinder(data);
        optimalPath = pathFinder.findOptimalPath();
    }

    public TrackData getData() {
        return data;
    }

    public TrackView getView() {
        return view;
    }

    public ArrayList<Point> getOptimalPath() {
        return optimalPath;
    }

    public void show() {
        view.show();
    }

    public void hide() {
        view.hide();
    }

    public float getNearestDistanceToRoadBlock(PointF start, PointF end) {
        if (!data.isMovable(view.getTrackCoordFromScreenCoord(start))) {
            return 0.0f;
        }

        ArrayList<Point> points = view.getRaytracedTileList(start, end);

        for (Point p : points) {
            if (!data.isMovable(p)) {
                PointF blockPosition = view.getScreenRegionfromTrackCoord(p).center();
                return start.distance(blockPosition);
            }
        }

        return Float.MAX_VALUE;
    }

    public float getNearestDistanceToRoadBlock(PointF pt, float angle, float maxDistance) {
        if (!data.isMovable(view.getTrackCoordFromScreenCoord(pt))) {
            return 0.0f;
        }

        ArrayList<Point> points = view.getRaytracedTileList(pt, angle, maxDistance);

        float nearestDistance = Float.MAX_VALUE;
        for (Point p : points) {
            if (!data.isMovable(p)) {
                PointF blockPosition = view.getScreenRegionfromTrackCoord(p).center();
                return pt.distance(blockPosition) - view.getTileSize().width;
            }
        }

        return Float.MAX_VALUE;
    }

    

    private TrackData data;
    private TrackView view;
    private ArrayList<Point> optimalPath;
}
