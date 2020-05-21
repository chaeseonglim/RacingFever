package com.lifejourney.racingfever;

import com.lifejourney.engine2d.Line;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Vector2D;
import com.lifejourney.engine2d.Waypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Track {

    private static final String LOG_TAG = "Track";

    enum LaneSelection {
        OPTIMAL_LANE(8),
        LEFT_BOUNDARY_LANE(4),
        RIGHT_BOUNDARY_LANE(4),
        MIDDLE_LANE(8);

        LaneSelection(int maxSearchRange) {
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
        paths = new HashMap<>();

        // Find optimal path
        TrackPathFinder pathFinder = new TrackPathFinder(data);
        ArrayList<Waypoint> optimalPath = pathFinder.findOptimalPath();
        paths.put(LaneSelection.OPTIMAL_LANE, optimalPath);

        // Find left and right boundary path
        ArrayList<Waypoint> leftAlternativePath = new ArrayList<>();
        ArrayList<Waypoint> rightAlternativePath = new ArrayList<>();
        ArrayList<Waypoint> middleAlternativePath = new ArrayList<>();
        for (int index = 0; index < optimalPath.size(); ++index) {
            int prevIndex = (index == 0)? optimalPath.size() - 1 : index - 1;

            Waypoint currentWaypoint = optimalPath.get(index);
            Waypoint prevWaypoint = optimalPath.get(prevIndex);

            Point currentWaypointPt = currentWaypoint.getPosition();
            Point prevWaypointPt = prevWaypoint.getPosition();

            Vector2D delta =
                    currentWaypointPt.vectorize().subtract(prevWaypointPt.vectorize());
            Vector2D crossRoad = delta.perpendicular();

            Point left = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.direction());
            Waypoint leftWaypoint = new Waypoint(left, null, 0.0f);
            leftWaypoint.setValid(!leftAlternativePath.contains(leftWaypoint));
            leftAlternativePath.add(leftWaypoint);

            Point right = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.multiply(-1)
                    .direction());
            Waypoint rightWaypoint = new Waypoint(right, null, 0.0f);
            rightWaypoint.setValid(!rightAlternativePath.contains(rightWaypoint));
            rightAlternativePath.add(rightWaypoint);

            Point middle = new Point(left).add(right).divide(2.0f);
            Waypoint middleWaypoint = new Waypoint(middle, null, 0.0f);
            middleWaypoint.setValid(!middleAlternativePath.contains(middleWaypoint));
            middleAlternativePath.add(middleWaypoint);
        }
        paths.put(LaneSelection.LEFT_BOUNDARY_LANE, leftAlternativePath);
        paths.put(LaneSelection.RIGHT_BOUNDARY_LANE, rightAlternativePath);
        paths.put(LaneSelection.MIDDLE_LANE, middleAlternativePath);
    }

    /*
    public int getNearestWaypointIndex(LaneSelection pathSelection, PointF pt) {
        ArrayList<Waypoint> path = getPath(pathSelection);

        float nearestDistance = Float.MAX_VALUE;
        int nearestWaypoint = -1;
        for (int i = 0; i < path.size(); ++i) {
            PointF waypointPt = getView().getScreenRegionfromTrackCoord(path.get(i)).center();
            float distance = waypointPt.distance(pt);
            float roadBlockDistance = getNearestDistanceToRoadBlock(pt, waypointPt);
            if (roadBlockDistance != Float.MAX_VALUE) {
                continue;
            }

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestWaypoint = i;
            }
        }

        return nearestWaypoint;
    }
     */

    public TrackData getData() {
        return data;
    }

    public TrackView getView() {
        return view;
    }

    public ArrayList<Waypoint> getOptimalPath() {
        return paths.get(LaneSelection.OPTIMAL_LANE);
    }

    public ArrayList<Waypoint> getLeftBoundaryPath() {
        return paths.get(LaneSelection.LEFT_BOUNDARY_LANE);
    }

    public ArrayList<Waypoint> getRightBoundaryPath() {
        return paths.get(LaneSelection.RIGHT_BOUNDARY_LANE);
    }

    public ArrayList<Waypoint> getPath(LaneSelection laneSelection) {
        return paths.get(laneSelection);
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
    private Map<LaneSelection, ArrayList<Waypoint>> paths;

    // for debugging
    private Line raycastingLine;
}
