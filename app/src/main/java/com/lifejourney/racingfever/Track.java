package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Line;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Vector2D;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Track {

    private static final String LOG_TAG = "Track";

    enum PathSelection {
        OPTIMAL_PATH(10),
        LEFT_BOUNDARY_PATH(5),
        RIGHT_BOUNDARY_PATH(5),
        MIDDLE_PATH(10);

        PathSelection(int maxSearchRange) {
            this.maxSearchRange = maxSearchRange;
        }

        public int maxSearchRange() {
            return maxSearchRange;
        }

        private final int maxSearchRange;
    }

    public Track(String mapAsset, float scale) {
        // Load and setup map data
        data = new TrackData(mapAsset);

        // Prepare track view
        view = new TrackView(data, scale);
        view.show();

        // Find suitable paths
        findSuitablePaths();

        // for debugging
        raycastingLine = new Line.Builder(new PointF(), new PointF())
                .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
    }

    private void findSuitablePaths() {
        paths = new HashMap<PathSelection, ArrayList<Point>>();

        // Find optimal path
        TrackPathFinder pathFinder = new TrackPathFinder(data);
        ArrayList<Point> optimalPath = pathFinder.findOptimalPath();
        paths.put(PathSelection.OPTIMAL_PATH, optimalPath);

        // Find left and right boundary path
        ArrayList<Point> leftAlternativePath = new ArrayList<>();
        ArrayList<Point> rightAlternativePath = new ArrayList<>();
        ArrayList<Point> middleAlternativePath = new ArrayList<>();
        for (int index = 0; index < optimalPath.size(); ++index) {
            int prevIndex = (index == 0)? optimalPath.size() - 1 : index - 1;

            Point currentWaypointPt = optimalPath.get(index);
            Point prevWaypointPt = optimalPath.get(prevIndex);

            Vector2D deltaWaypoint =
                    currentWaypointPt.vectorize().subtract(prevWaypointPt.vectorize());
            Vector2D crossRoad = deltaWaypoint.perpendicular();

            Point left = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.direction());
            if (!leftAlternativePath.contains(left)) {
                leftAlternativePath.add(left);
            }
            Point right = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.multiply(-1)
                    .direction());
            if (!rightAlternativePath.contains(right)) {
                rightAlternativePath.add(right);
            }
            Point middle = new Point(left).add(right).divide(2.0f);
            if (!middleAlternativePath.contains(middle)) {
                middleAlternativePath.add(middle);
            }
        }
        paths.put(PathSelection.LEFT_BOUNDARY_PATH, leftAlternativePath);
        paths.put(PathSelection.RIGHT_BOUNDARY_PATH, rightAlternativePath);
        paths.put(PathSelection.MIDDLE_PATH, middleAlternativePath);
    }

    public int getNearestWaypointIndex(PathSelection pathSelection, Point pt) {
        ArrayList<Point> path = getPath(pathSelection);

        float nearestDistance = Float.MAX_VALUE;
        int nearestWaypoint = -1;
        for (int i = 0; i < path.size(); ++i) {
            float distance = pt.distance(path.get(i));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestWaypoint = i;
            }
        }

        return nearestWaypoint;
    }

    public TrackData getData() {
        return data;
    }

    public TrackView getView() {
        return view;
    }

    public ArrayList<Point> getOptimalPath() {
        return paths.get(PathSelection.OPTIMAL_PATH);
    }

    public ArrayList<Point> getLeftBoundaryPath() {
        return paths.get(PathSelection.LEFT_BOUNDARY_PATH);
    }

    public ArrayList<Point> getRightBoundaryPath() {
        return paths.get(PathSelection.RIGHT_BOUNDARY_PATH);
    }

    public ArrayList<Point> getPath(PathSelection pathSelection) {
        return paths.get(pathSelection);
    }

    public void show() {
        view.show();
    }

    public void hide() {
        view.hide();
    }

    public Point getBoundaryRoadCoordinate(Point pt, float direction) {
        if (!data.isMovable(pt)) {
            return null;
        }

        PointF screenPt = view.getScreenRegionfromTrackCoord(pt).center();
        ArrayList<Point> points = view.getRaytracedTileList(screenPt, direction,
                view.getTileSize().width*10);
        Point prevPt = points.get(0);
        for (Point p : points) {
            if (!data.isMovable(p)) {
                return prevPt;
            }

            prevPt = p;
        }
        return points.get(points.size()-1);
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

    public float getNearestDistanceToRoadBlock(PointF pt, float direction, float maxDistance) {
        if (!data.isMovable(view.getTrackCoordFromScreenCoord(pt))) {
            return 0.0f;
        }

        ArrayList<Point> points = view.getRaytracedTileList(pt, direction, maxDistance);
        Vector2D endVector = new Vector2D(direction).multiply(maxDistance);

        // for debugging
        raycastingLine.set(pt, endVector);
        raycastingLine.commit();

        for (Point p : points) {
            if (!data.isMovable(p)) {
                PointF blockPosition = view.getScreenRegionfromTrackCoord(p).center();
                return pt.distance(blockPosition);
            }
        }

        return Float.MAX_VALUE;
    }

    private TrackData data;
    private TrackView view;
    private Map<PathSelection, ArrayList<Point>> paths;

    // for debugging
    private Line raycastingLine;
}
