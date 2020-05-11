package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.CollidableObject;
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
        ArrayList<Car> cars;

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
        public Builder cars(ArrayList<Car> cars) {
            this.cars = cars;
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
        cars = builder.cars;
    }

    public void ride(Car car) {
        this.myCar = car;
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
        if (myCar == null) {
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

        return targetRegion.includes(myCar.getPosition());
    }

    private int getDistanceBetweenWaypointIndexes(int waypoint1, int waypoint2) {
        int totaNumberOfWaypoints = track.getOptimalPath().size();

        return Math.min(Math.abs(waypoint1-waypoint2),
                Math.abs(totaNumberOfWaypoints-Math.max(waypoint1, waypoint2)+
                        Math.min(waypoint1, waypoint2)));
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

        PointF position = myCar.getPosition();
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

        if (getDistanceBetweenWaypointIndexes(lastPassedWaypointIndex, currentWaypointTargetIndex) >
            MAX_WAYPOINT_SEARCH_RANGE) {
            return -1;
        }

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
            float distanceToWaypoint = targetCenter.distance(myCar.getPosition());

            if (track.getNearestDistanceToRoadBlock(myCar.getPosition(), targetCenter) >=
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

        //Log.e(LOG_TAG, name + " currentWaypointIndex: " + currentWaypointTargetIndex +
        //        " " + lastPassedWaypointIndex);
    }

    private RectF getWaypointTargetRegion(int waypointIndex) {
        Point targetMap = track.getOptimalPath().get(currentWaypointTargetIndex);
        return track.getView().getScreenRegionfromTrackCoord(targetMap);
    }

    private void drive() {
        if (targetRegion == null || !myCar.isUpdatePossible()) {
            return;
        }

        boolean isEmergency = false;
        int tileWidth = track.getView().getTileSize().width;

        myCar.seek(targetRegion.center());

        // Avoid obstacles
        if (obstacles != null) {
            if (myCar.avoidObstacles(obstacles, tileWidth * 4)) {
                isEmergency = true;
            }
        }

        // Boid into neighbor cars
        if (cars != null) {
            ArrayList<CollidableObject> neighborCars = new ArrayList<>();
            float cosMaxAngle = -0.5f; // cos(90')
            for (Car car : cars) {
                if (car != myCar) {
                    Vector2D offset = car.getFuturePositionVector().subtract(myCar.getFuturePositionVector());
                    if (offset.length() <= tileWidth * 4) {
                        Vector2D unitOffset = offset.normalize();
                        float forwardness = myCar.getForwardVector().dot(unitOffset);
                        //if (forwardness > cosMaxAngle)
                            neighborCars.add(car);
                    }
                }
            }
            //myCar.cohension(neighborCars, tileWidth * 4, 0.1f);
            //myCar.alignment(neighborCars, tileWidth * 3, 0.1f);
            myCar.separation(neighborCars, tileWidth * 2, 0.2f);

            //if (myCar.avoidObstacles(neighborCars, tileWidth * 4)) {
            //    isEmergency = true;
            //}
        }

        // Seek to target region
        if (isEmergency) {
            myCar.stop();
        }
    }

    private final int STARTING_WAYPOINT_INDEX = 30;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 1;
    private final int MAX_WAYPOINT_SEARCH_RANGE = 7;

    private String name;
    private float reflection;

    private Car myCar;
    private Track track;
    private RectF targetRegion;

    private int lastPassedWaypointIndex = 0;
    private int currentWaypointTargetIndex = STARTING_WAYPOINT_INDEX;
    private int waypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;

    private ArrayList<Car> cars;
    private ArrayList<CollidableObject> obstacles;
}
