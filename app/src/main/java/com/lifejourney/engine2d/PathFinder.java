package com.lifejourney.engine2d;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public abstract class PathFinder {

    private static final String LOG_TAG = "PathFinder";

    protected enum Direction {
        TOP(0, -1),
        TOP_LEFT(-1, -1),
        TOP_RIGHT(1, -1),
        LEFT(-1, 0),
        RIGHT(1, 0),
        BOTTOM(0, 1),
        BOTTOM_LEFT(-1, 1),
        BOTTOM_RIGHT(1, 1);

        Direction(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        public Point getOffset() {
            return new Point(xOffset, yOffset);
        }

        private int xOffset;
        private int yOffset;
    }

    public PathFinder(Point startPosition, Point targetPosition) {
        this.startPosition = startPosition;
        this.targetPosition = targetPosition;
    }

    /**
     * Finding path using A* algorithm
     */
    public ArrayList<Waypoint> findOptimalPath() {
        PriorityQueue<Waypoint> openList = new PriorityQueue<>();
        ArrayList<Waypoint> closeList = new ArrayList<>();

        // Add start point to open list
        openList.offer(new Waypoint(startPosition, null,0.0f));

        // Search through open list
        while (openList.size() > 0) {
            Waypoint waypoint = openList.poll();

            // Check if it's goal
            assert waypoint != null;
            if (waypoint.getPosition().equals(targetPosition)) {
                // Get optimal path
                optimalPath = new ArrayList<>();
                while (waypoint != null) {
                    optimalPath.add(waypoint);
                    waypoint = waypoint.getParent();
                }
                Collections.reverse(optimalPath);
                return optimalPath;
            }

            if (closeList.contains(waypoint)) {
                continue;
            }

            // Add waypoint to close list
            closeList.add(waypoint);

            // Find possible waypoints from here
            ArrayList<Waypoint> possibleWaypoints = getPossibleWaypoints(waypoint);

            for (Waypoint possibleWaypoint: possibleWaypoints) {
                // Calculate heuristic score
                possibleWaypoint.setCostToTarget(calcCostToTarget(possibleWaypoint, targetPosition));

                // Check if this candidate is in open list already
                boolean skipThisWaypoint = false;
                if (openList.contains(possibleWaypoint)) {
                    for (Waypoint w : openList) {
                        if (w.equals(possibleWaypoint) && w.getCost() <= possibleWaypoint.getCost()) {
                            skipThisWaypoint = true;
                            break;
                        }
                    }
                    if (skipThisWaypoint) {
                        continue;
                    }
                }

                // Check if it's in close list
                if (closeList.contains(possibleWaypoint)) {
                    Waypoint w = closeList.get(closeList.indexOf(possibleWaypoint));
                    if (w.getCost() <= possibleWaypoint.getCost()) {
                        continue;
                    }

                    closeList.remove(possibleWaypoint);
                }

                // Replace one if the exist one in open list has worse score than candidate
                openList.remove(possibleWaypoint);
                openList.offer(possibleWaypoint);

            }
        }

        Log.e(LOG_TAG, "Failed to find a path!!!");
        return null;
    }

    /**
     *
     * @param waypoint
     * @param targetPosition
     * @return
     */
    private float calcCostToTarget(Waypoint waypoint, Point targetPosition) {
        return waypoint.getPosition().distance(targetPosition);
    }

    /**
     *
     * @return
     */
    public ArrayList<Waypoint> getOptimalPath() {
        return optimalPath;
    }

    /**
     *
     * @param waypoint
     * @return
     */
    private ArrayList<Waypoint> getPossibleWaypoints(Waypoint waypoint) {
        ArrayList<Waypoint> waypoints = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            Point newPositon = new Point(waypoint.getPosition()).offset(direction.getOffset());
            if (canMove(waypoint.getPosition(), newPositon)) {
                waypoints.add(new Waypoint(newPositon, waypoint,
                        waypoint.getCostFromStart()+1.0f));
            }
        }
        return waypoints;
    }

    /**
     *
     * @param curPt
     * @param newPt
     * @return
     */
    protected abstract boolean canMove(Point curPt, Point newPt);

    private Point startPosition;
    private Point targetPosition;
    private ArrayList<Waypoint> optimalPath;
}
