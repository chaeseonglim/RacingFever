package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.RectF;
import com.lifejourney.engine2d.Vector2D;
import com.lifejourney.engine2d.Waypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Track {

    private static final String LOG_TAG = "Track";

    enum LaneSelection {
        OPTIMAL_LANE(8),
        L2_LANE(6),
        L1_LANE(6),
        MIDDLE_LANE(6),
        R1_LANE(6),
        R2_LANE(6);

        LaneSelection(int maxSearchRange) {
            this.maxSearchRange = maxSearchRange;
        }

        int maxSearchRange() {
            return maxSearchRange;
        }

        private final int maxSearchRange;
    }

    Track(String mapAsset, float scale) {
        // Load and setup map data
        data = new TrackData(mapAsset);

        // Prepare track view
        view = new TrackView(data, scale);
        view.show();

        // Find suitable paths
        searchLanes();
    }

    /**
     *
     */
    private void searchLanes() {
        lanes = new HashMap<>();

        // Find optimal lane
        ArrayList<Waypoint> optimalLane = new TrackPathFinder(data).findOptimalPath();
        lanes.put(LaneSelection.OPTIMAL_LANE, optimalLane);

        // Find left and right boundary lane
        ArrayList<Waypoint> l2Lane = new ArrayList<>();
        ArrayList<Waypoint> r2Lane = new ArrayList<>();
        ArrayList<Waypoint> l1Lane = new ArrayList<>();
        ArrayList<Waypoint> r1Lane = new ArrayList<>();
        ArrayList<Waypoint> middleLane = new ArrayList<>();
        Waypoint prevL2Waypoint = null;
        Waypoint prevR2Waypoint = null;
        Waypoint prevL1Waypoint = null;
        Waypoint prevR1Waypoint = null;
        Waypoint prevMiddleWaypoint = null;
        for (int index = 0; index < optimalLane.size(); ++index) {
            int prevIndex = (index == 0)? optimalLane.size() - 1 : index - 1;

            Waypoint currentWaypoint = optimalLane.get(index);
            Waypoint prevWaypoint = optimalLane.get(prevIndex);

            Point currentWaypointPt = currentWaypoint.getPosition();
            Point prevWaypointPt = prevWaypoint.getPosition();

            Vector2D delta = currentWaypointPt.vectorize().subtract(prevWaypointPt.vectorize());
            Vector2D crossRoad = delta.perpendicular();

            // Find l2 path
            Point l2 = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.direction());
            Waypoint l2Waypoint = new Waypoint(l2, null, 0.0f);
            l2Waypoint.setValid(!l2Lane.contains(l2Waypoint));
            l2Waypoint.setPrev(prevL2Waypoint);
            if (prevL2Waypoint != null) {
                prevL2Waypoint.setNext(l2Waypoint);
            }
            prevL2Waypoint = l2Waypoint;
            l2Lane.add(l2Waypoint);

            // Find r2 path
            Point r2 = getBoundaryRoadCoordinate(currentWaypointPt, crossRoad.multiply(-1)
                    .direction());
            Waypoint r2Waypoint = new Waypoint(r2, null, 0.0f);
            r2Waypoint.setValid(!r2Lane.contains(r2Waypoint));
            r2Waypoint.setPrev(prevR2Waypoint);
            if (prevR2Waypoint != null) {
                prevR2Waypoint.setNext(r2Waypoint);
            }
            prevR2Waypoint = r2Waypoint;
            r2Lane.add(r2Waypoint);

            // Find middle path
            Point middle = new Point(l2).add(r2).divide(2.0f);
            Waypoint middleWaypoint = new Waypoint(middle, null, 0.0f);
            middleWaypoint.setValid(!middleLane.contains(middleWaypoint));
            middleWaypoint.setPrev(prevMiddleWaypoint);
            if (prevMiddleWaypoint != null) {
                prevMiddleWaypoint.setNext(middleWaypoint);
            }
            prevMiddleWaypoint = middleWaypoint;
            middleLane.add(middleWaypoint);

            // Find l1 path
            Point l1 = new Point(l2).add(middle).divide(2.0f);
            Waypoint l1Waypoint = new Waypoint(l1, null, 0.0f);
            l1Waypoint.setValid(!l1Lane.contains(l1Waypoint));
            l1Waypoint.setPrev(prevL1Waypoint);
            if (prevL1Waypoint != null) {
                prevL1Waypoint.setNext(l1Waypoint);
            }
            prevL1Waypoint = l1Waypoint;
            l1Lane.add(l1Waypoint);

            // Find r1 path
            Point r1 = new Point(r2).add(middle).divide(2.0f);
            Waypoint r1Waypoint = new Waypoint(r1, null, 0.0f);
            r1Waypoint.setValid(!r1Lane.contains(r1Waypoint));
            r1Waypoint.setPrev(prevR1Waypoint);
            if (prevR1Waypoint != null) {
                prevR1Waypoint.setNext(r1Waypoint);
            }
            prevR1Waypoint = r1Waypoint;
            r1Lane.add(r1Waypoint);
        }
        if (l2Lane.size() > 0) {
            assert prevL2Waypoint != null;
            prevL2Waypoint.setNext(l2Lane.get(0));
            l2Lane.get(0).setPrev(prevL2Waypoint);
            calcCostToSearch(l2Lane);
        }
        if (r2Lane.size() > 0) {
            assert prevR2Waypoint != null;
            prevR2Waypoint.setNext(r2Lane.get(0));
            r2Lane.get(0).setPrev(prevR2Waypoint);
            calcCostToSearch(r2Lane);
        }
        if (l1Lane.size() > 0) {
            assert prevL1Waypoint != null;
            prevL1Waypoint.setNext(l1Lane.get(0));
            l1Lane.get(0).setPrev(prevL1Waypoint);
            calcCostToSearch(l1Lane);
        }
        if (r1Lane.size() > 0) {
            assert prevR1Waypoint != null;
            prevR1Waypoint.setNext(r1Lane.get(0));
            r1Lane.get(0).setPrev(prevR1Waypoint);
            calcCostToSearch(r1Lane);
        }
        if (middleLane.size() > 0) {
            assert prevMiddleWaypoint != null;
            prevMiddleWaypoint.setNext(middleLane.get(0));
            middleLane.get(0).setPrev(prevMiddleWaypoint);
            calcCostToSearch(middleLane);
        }

        lanes.put(LaneSelection.L1_LANE, l1Lane);
        lanes.put(LaneSelection.R1_LANE, r1Lane);
        lanes.put(LaneSelection.L2_LANE, l2Lane);
        lanes.put(LaneSelection.R2_LANE, r2Lane);
        lanes.put(LaneSelection.MIDDLE_LANE, middleLane);
    }

    /**
     *
     * @param lane
     */
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
            /*
            else if (angle > 40.0f) {
                waypoint.setCostToSearch(3);
            }
             */
        }
    }

    /**
     *
     * @return
     */
    TrackData getData() {
        return data;
    }

    /**
     *
     * @return
     */
    TrackView getView() {
        return view;
    }

    /**
     *
     * @param laneSelection
     * @return
     */
    ArrayList<Waypoint> getLane(LaneSelection laneSelection) {
        return lanes.get(laneSelection);
    }

    /**
     *
     */
    void show() {
        view.show();
    }

    /**
     *
     */
    void hide() {
        view.hide();
    }

    /**
     *
     * @param pt
     * @param direction
     * @return
     */
    private Point getBoundaryRoadCoordinate(Point pt, float direction) {
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

    /**
     *
     * @param start
     * @param end
     * @return
     */
    float getNearestDistanceToRoadBlock(PointF start, PointF end) {
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

    /**
     *
     * @param pt
     * @param direction
     * @param maxDistance
     * @return
     */
    float getNearestDistanceToRoadBlock(PointF pt, float direction, float maxDistance) {
        if (!data.isMovable(view.getTrackCoordFromScreenCoord(pt))) {
            return 0.0f;
        }

        ArrayList<Point> points = view.getRaytracedTileList(pt, direction, maxDistance);
        Vector2D endVector = new Vector2D(direction).multiply(maxDistance);

        for (Point p : points) {
            if (!data.isMovable(p)) {
                PointF blockPosition = view.getScreenRegionfromTrackCoord(p).center();
                return pt.distance(blockPosition);
            }
        }

        return Float.MAX_VALUE;
    }

    LaneSelection getNearestLaneFromCurrentPosition(int waypointIndex, PointF pt) {
        float nearestDistance = Float.MAX_VALUE;
        LaneSelection nearestLane = null;
        for (int i = 1; i < LaneSelection.values().length; ++i) {
            float distance =
                    getWaypointRegion(LaneSelection.values()[i], waypointIndex)
                            .center().distance(pt);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestLane = LaneSelection.values()[i];
            }
        }

        return nearestLane;
    }

    /**
     *
     * @param selection
     * @param waypointIndex
     * @return
     */
    RectF getWaypointRegion(Track.LaneSelection selection, int waypointIndex) {
        Point targetMap = getLane(selection).get(waypointIndex).getPosition();
        return getView().getScreenRegionfromTrackCoord(targetMap);
    }

    /**
     *
     * @param laneSelection
     * @param waypointIndex1
     * @param waypointIndex2
     * @return
     */
    int getDistanceBetweenWaypointIndex(LaneSelection laneSelection,
                                        int waypointIndex1, int waypointIndex2) {
        int totaNumberOfWaypoints = getLane(laneSelection).size();

        return Math.min(Math.abs(waypointIndex1-waypointIndex2),
                Math.abs(totaNumberOfWaypoints-Math.max(waypointIndex1, waypointIndex2)+
                        Math.min(waypointIndex1, waypointIndex2)));
    }

    /**
     *
     * @param laneSelection
     * @param waypointIndex
     * @return
     */
    int findNextValidWaypoint(Track.LaneSelection laneSelection, int waypointIndex) {
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

    /**
     *
     * @param laneSelection
     * @param currentIndex
     * @param maxSearchableScore
     * @return
     */
    int getWaypointCountWhichCanBeSearched(Track.LaneSelection laneSelection,
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
}
