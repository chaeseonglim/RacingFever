package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.CollisionPool;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.RectF;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;
import java.util.Collections;

public class Driver implements Comparable<Driver> {

    private static final String LOG_TAG = "Driver";

    public static class Builder {
        String name;
        ArrayList<CollidableObject> obstacles;

        // Optional parameter
        float reflection = 1.0f;

        public Builder(String name) {
            this.name = name;
        }
        public Builder reflection(float reflection) {
            this.reflection = reflection;
            return this;
        }
        public Builder obstacles(ArrayList<CollidableObject> obstacles) {
            this.obstacles = obstacles;
            return this;
        }
        public Driver build() {
            return new Driver(this);
        }
    }

    private Driver(Builder builder) {
        name = builder.name;
        reflection = builder.reflection;
        obstacles = builder.obstacles;
    }

    public void ride(Car car) {
        this.car = car;
    }

    public void learn(Track track) {
        this.track = track;
    }

    public void start() {
        setWaypointToTarget(currentWaypointTargetIndex);
    }

    public void stop() {
    }

    @Override
    public int compareTo(Driver d) {
        if (d == this) {
            return 0;
        }
        else {
            if (d.lastPassedWaypointIndex > lastPassedWaypointIndex) {
                return -1;
            }
            else if (d.lastPassedWaypointIndex < lastPassedWaypointIndex) {
                return 1;
            }
            else {
                /*
                float targetDistanceSqOfD = d.car.getPosition().distanceSq(d.targetRegion.center());
                float targetDistanceSqOfThis = car.getPosition().distanceSq(targetRegion.center());

                if (targetDistanceSqOfD > targetDistanceSqOfThis) {
                    return -1;
                }
                else if (targetDistanceSqOfD < targetDistanceSqOfThis) {
                    return 1;
                }
                else {
                    return 0;
                }
                 */

                return 0;
            }
        }
    }

    public void update() {
        if (car == null) {
            return;
        }

        // Check if target region is achieved
        if (track == null) {
            if (checkIfCarArrivesTargetRegion())
                targetRegion = null;
        }
        else {
            updateLastPassedWaypoint();
            if (lastPassedWaypointIndex == currentWaypointTargetIndex) {
                // Go to next waypoint
                setNewWaypointToTarget();
            }
            else {
                // Go to next waypoint only if possible
                if (waypointSearchTimeLeft <= 0) {
                    setNewWaypointToTargetOnlyIfSuitable();
                    waypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;
                }
                else {
                    waypointSearchTimeLeft--;
                }
            }
        }

        // Drive car
        drive();
    }

    public void setTargetRegion(RectF targetRegion) {
        this.targetRegion = targetRegion;
    }

    private boolean checkIfCarArrivesTargetRegion() {
        if (targetRegion == null)
            return false;

        return targetRegion.includes(car.getPosition());
    }

    private void updateLastPassedWaypoint() {
        int totaNumberOfWaypoints = track.getOptimalPath().size();
        int numberOfWaypointsToTest;
        if (currentWaypointTargetIndex < lastPassedWaypointIndex) {
            numberOfWaypointsToTest = totaNumberOfWaypoints -
                            lastPassedWaypointIndex + currentWaypointTargetIndex;
        }
        else {
            numberOfWaypointsToTest = currentWaypointTargetIndex - lastPassedWaypointIndex;
        }

        PointF position = car.getPosition();
        for (int i = numberOfWaypointsToTest - 1; i >= 0; --i) {
            int currentWaypointIndex = (lastPassedWaypointIndex + i) % totaNumberOfWaypoints;
            RectF region = getWaypointTargetRegion(currentWaypointIndex);

            if (region.includes(position)) {
                lastPassedWaypointIndex = currentWaypointIndex;
                break;
            }
        }
    }

    private int findSuitableWaypointToTarget() {
        ArrayList<Integer> candidatesWaypoints = new ArrayList<>();

        // Search through waypoints
        int maxWaypointSearchRange = MAX_WAYPOINT_SEARCH_RANGE;
        int waypointCount = track.getOptimalPath().size();
        for (int i = 1; i <= maxWaypointSearchRange; ++i) {
            candidatesWaypoints.add((lastPassedWaypointIndex + i) % waypointCount);
        }

        // Raytracing waypoints to find possible one
        Collections.reverse(candidatesWaypoints);
        for (int waypointIndex : candidatesWaypoints) {
            Point targetMap = track.getOptimalPath().get(waypointIndex);
            RectF targetRegion = track.getView().getScreenRegionfromTrackCoord(targetMap);
            PointF targetCenter = targetRegion.center();
            float distanceToWaypoint = targetCenter.distance(car.getPosition());

            if (track.getNearestDistanceToRoadBlock(car.getPosition(), targetCenter) >=
                    distanceToWaypoint) {
                return waypointIndex;
            }
        }

        // Failed to find suitable waypoint index
        return -1;
    }

    private void setNewWaypointToTarget() {
        int newWaypoint = findSuitableWaypointToTarget();
        if (newWaypoint == -1) {
            int waypointCount = track.getOptimalPath().size();
            newWaypoint = (lastPassedWaypointIndex + 1) % waypointCount;
        }

        setWaypointToTarget(newWaypoint);
    }

    private void setNewWaypointToTargetOnlyIfSuitable() {
        int newWaypoint = findSuitableWaypointToTarget();
        if (newWaypoint != -1) {
            setWaypointToTarget(newWaypoint);
        }
    }

    private void setWaypointToTarget(int waypointIndex) {
        setTargetRegion(getWaypointTargetRegion(waypointIndex));
        currentWaypointTargetIndex = waypointIndex;

        Log.e(LOG_TAG, name + " currentWaypointIndex: " + currentWaypointTargetIndex +
                " " + lastPassedWaypointIndex);
    }

    private RectF getWaypointTargetRegion(int waypointIndex) {
        Point targetMap = track.getOptimalPath().get(currentWaypointTargetIndex);
        return track.getView().getScreenRegionfromTrackCoord(targetMap);
    }

    private void drive() {
        if (targetRegion == null) {
            return;
        }

        // Seek to target region
        if (!car.isUpdatePossible()) {
            return;
        }

        car.seek(targetRegion.center());

        Vector2D positionVector = car.getPositionVector();
        Vector2D velocityVector = car.getVelocity();
        float velocityScalar = velocityVector.length();
        float headDirection = car.getHeadDirection();

        /*
        // Collision sensor with obstacles
        if (obstacles != null) {
            float maxSensorDistance = velocityScalar * 10;
            ArrayList<CollidableObject> possibleObstacleList =
                    obstacles.getRaytracedObjectList(car.getPosition(), headDirection,
                            maxSensorDistance);

            if (possibleObstacleList.size() > 0) {
                Log.e(LOG_TAG, name + " possibleObstacleList: " + possibleObstacleList.size());
            }

            CollidableObject nearestObstacle = null;
            float nearestObstacleDistance = Float.MAX_VALUE;
            for (CollidableObject obstacle : obstacles) {
                float distanceSq =
                        obstacle.getPosition().distanceSq(car.getPosition()) -
                                obstacle.getShape().getRadius();
                if (distanceSq < nearestObstacleDistance) {
                    nearestObstacleDistance = distanceSq;
                    nearestObstacle = obstacle;
                }
            }

            if (nearestObstacle != null) {
                car.avoidObstacle(nearestObstacle);
            }
        }

         */

        /*
        // Collision sensor with track boundary
        Size tileSize = track.getView().getTileSize();
        float maxTrackBoundarySensorDistance = Math.max(tileSize.width, tileSize.height) * 5;
        float movableDistanceOnTrack =
                track.getNearestDistanceToRoadBlock(new PointF(positionVector),
                        headDirection, maxTrackBoundarySensorDistance);
        if (movableDistanceOnTrack > 0.0f &&
                movableDistanceOnTrack < maxTrackBoundarySensorDistance) {
            float estimatedNumberOfUpdateToRoadBoundary = movableDistanceOnTrack / velocityScalar;

            Log.e(LOG_TAG, "estimatedNumberOfUpdateToRoadBoundary: " + estimatedNumberOfUpdateToRoadBoundary);
            // If it's very close to the track boundary, don't accelerate
            if (estimatedNumberOfUpdateToRoadBoundary < 10.0f) {
                car.avoidObstacle(
                        new PointF(new Vector2D(headDirection)
                                .multiply(movableDistanceOnTrack).add(positionVector)));
                Log.e(LOG_TAG, "NONONO2");
            }
        }
         */
    }

    private final int STARTING_WAYPOINT_INDEX = 30;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 10;
    private final int MAX_WAYPOINT_SEARCH_RANGE = 5;

    private String name;
    private float reflection;

    private Car car;
    private Track track;
    private RectF targetRegion;

    private int lastPassedWaypointIndex = 0;
    private int currentWaypointTargetIndex = STARTING_WAYPOINT_INDEX;
    private int waypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;

    private ArrayList<Car> cars;
    private ArrayList<CollidableObject> obstacles;
}
