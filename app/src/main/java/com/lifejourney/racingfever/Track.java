package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Line;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.RectF;
import com.lifejourney.engine2d.Vector2D;
import com.lifejourney.engine2d.Waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Track {

    private static final String LOG_TAG = "Track";

    enum LaneSelection {
        OPTIMAL_LANE(8),
        LEFT_BOUNDARY_LANE(5),
        RIGHT_BOUNDARY_LANE(5),
        MIDDLE_LANE(5);

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
        searchLanes();

        // for debugging
        raycastingLine = new Line.Builder(new PointF(), new PointF())
                .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
    }

    private void searchLanes() {
        lanes = new HashMap<>();

        // Find optimal lane
        ArrayList<Waypoint> optimalLane = new TrackPathFinder(data).findOptimalPath();
        lanes.put(LaneSelection.OPTIMAL_LANE, optimalLane);

        // Find left and right boundary lane
        ArrayList<Waypoint> leftAlternativeLane = new ArrayList<>();
        ArrayList<Waypoint> rightAlternativeLane = new ArrayList<>();
        ArrayList<Waypoint> middleAlternativeLane = new ArrayList<>();
        Waypoint prevLeftWaypoint = null, prevRightWaypoint = null, prevMiddleWaypoint = null;
        for (int index = 0; index < optimalLane.size(); ++index) {
            int prevIndex = (index == 0)? optimalLane.size() - 1 : index - 1;

            Waypoint currentWaypoint = optimalLane.get(index);
            Waypoint prevWaypoint = optimalLane.get(prevIndex);

            Point currentWaypointPt = currentWaypoint.getPosition();
            Point prevWaypointPt = prevWaypoint.getPosition();

            Vector2D delta =
                    currentWaypointPt.vectorize().subtract(prevWaypointPt.vectorize());
            Vector2D crossRoad = delta.perpendicular();

            // Find left boundary path
            Point left = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.direction());
            Waypoint leftWaypoint = new Waypoint(left, null, 0.0f);
            leftWaypoint.setValid(!leftAlternativeLane.contains(leftWaypoint));
            leftWaypoint.setPrev(prevLeftWaypoint);
            if (prevLeftWaypoint != null) {
                prevLeftWaypoint.setNext(leftWaypoint);
            }
            prevLeftWaypoint = leftWaypoint;
            leftAlternativeLane.add(leftWaypoint);

            // Find right boundary path
            Point right = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.multiply(-1)
                    .direction());
            Waypoint rightWaypoint = new Waypoint(right, null, 0.0f);
            rightWaypoint.setValid(!rightAlternativeLane.contains(rightWaypoint));
            rightWaypoint.setPrev(prevRightWaypoint);
            if (prevRightWaypoint != null) {
                prevRightWaypoint.setNext(rightWaypoint);
            }
            prevRightWaypoint = rightWaypoint;
            rightAlternativeLane.add(rightWaypoint);

            // Find middle path
            Point middle = new Point(left).add(right).divide(2.0f);
            Waypoint middleWaypoint = new Waypoint(middle, null, 0.0f);
            middleWaypoint.setValid(!middleAlternativeLane.contains(middleWaypoint));
            middleWaypoint.setPrev(prevMiddleWaypoint);
            if (prevMiddleWaypoint != null) {
                prevMiddleWaypoint.setNext(middleWaypoint);
            }
            prevMiddleWaypoint = middleWaypoint;
            middleAlternativeLane.add(middleWaypoint);
        }
        if (leftAlternativeLane.size() > 0) {
            prevLeftWaypoint.setNext(leftAlternativeLane.get(0));
            leftAlternativeLane.get(0).setPrev(prevLeftWaypoint);
            calcCostToSearch(leftAlternativeLane);
        }
        if (rightAlternativeLane.size() > 0) {
            prevRightWaypoint.setNext(rightAlternativeLane.get(0));
            rightAlternativeLane.get(0).setPrev(prevRightWaypoint);
            calcCostToSearch(rightAlternativeLane);
        }
        if (middleAlternativeLane.size() > 0) {
            prevMiddleWaypoint.setNext(middleAlternativeLane.get(0));
            middleAlternativeLane.get(0).setPrev(prevMiddleWaypoint);
            calcCostToSearch(middleAlternativeLane);
        }

        lanes.put(LaneSelection.LEFT_BOUNDARY_LANE, leftAlternativeLane);
        lanes.put(LaneSelection.RIGHT_BOUNDARY_LANE, rightAlternativeLane);
        lanes.put(LaneSelection.MIDDLE_LANE, middleAlternativeLane);
    }

    private void calcCostToSearch(ArrayList<Waypoint> lane) {
        for (Waypoint waypoint: lane) {
            Waypoint prevWaypoint = waypoint.getPrev();
            Waypoint nextWaypoint = waypoint.getNext();

            Point prevPt = prevWaypoint.getPosition();
            Point curPt = waypoint.getPosition();
            Point nextPt = nextWaypoint.getPosition();

            Vector2D prevDirection = curPt.vectorize().subtract(prevPt.vectorize());
            Vector2D nextDirection = nextPt.vectorize().subtract(curPt.vectorize());
            float angle = prevDirection.angle(nextDirection);

            if (angle > 20.0f) {
                waypoint.setCostToSearch(2);
            }
            else if (angle > 40.0f) {
                waypoint.setCostToSearch(3);
            }
        }
    }

    public TrackData getData() {
        return data;
    }

    public TrackView getView() {
        return view;
    }

    public ArrayList<Waypoint> getLane(LaneSelection laneSelection) {
        return lanes.get(laneSelection);
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

    public RectF getWaypointRegion(Track.LaneSelection selection, int waypointIndex) {
        Point targetMap = getLane(selection).get(waypointIndex).getPosition();
        return getView().getScreenRegionfromTrackCoord(targetMap);
    }

    public int getDistanceBetweenWaypointIndex(LaneSelection laneSelection,
                                               int waypointIndex1, int waypointIndex2) {
        int totaNumberOfWaypoints = getLane(laneSelection).size();

        return Math.min(Math.abs(waypointIndex1-waypointIndex2),
                Math.abs(totaNumberOfWaypoints-Math.max(waypointIndex1, waypointIndex2)+
                        Math.min(waypointIndex1, waypointIndex2)));
    }

    public int findNextValidWaypoint(Track.LaneSelection laneSelection, int waypointIndex) {
        int waypointCount = getLane(laneSelection).size();
        for (int i = 1; i < waypointCount; ++i) {
            int newWaypointIndex = (waypointIndex + i) % waypointCount;
            if (getLane(laneSelection).get(newWaypointIndex).isValid())
                return newWaypointIndex;
        }

        // This shouldn't be happend
        Log.e(LOG_TAG, "There's no valid waypoint found");
        return -1;
    }

    public int getWaypointCountWhichCanBeSearched(Track.LaneSelection laneSelection,
                                                   int currentIndex,
                                                   int maxSearchableScore) {

        if (maxSearchableScore == 0) {
            maxSearchableScore = laneSelection.maxSearchRange();
        }

        int waypointCount = getLane(laneSelection).size();
        int i = 1, score = 1;
        for (; i < maxSearchableScore && score < maxSearchableScore; ++i) {
            int waypointIndex = (currentIndex + i) % waypointCount;
            Waypoint waypoint = getLane(laneSelection).get(waypointIndex);
            if (!waypoint.isValid())
                continue;
            score += waypoint.getCostToSearch();
        }

        return i;
    }


    private TrackData data;
    private TrackView view;
    private Map<LaneSelection, ArrayList<Waypoint>> lanes;

    // for debugging
    private Line raycastingLine;
}
