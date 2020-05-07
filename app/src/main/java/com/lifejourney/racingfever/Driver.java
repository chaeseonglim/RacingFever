package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.RectF;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;
import java.util.Collections;

public class Driver {

    private static final String LOG_TAG = "Driver";

    public static class Builder {
        String name;

        // Optional parameter
        float reflection = 1.0f;

        public Builder(String name) {
            this.name = name;
        }
        public Builder reflection(float reflection) {
            this.reflection = reflection;
            return this;
        }
        public Driver build() {
            return new Driver(this);
        }
    }

    private Driver(Builder builder) {
        name = builder.name;
        reflection = builder.reflection;
    }

    public void ride(Car car) {
        this.car = car;
    }

    public void learn(Track track) {
        this.track = track;
    }

    public void start() {
        setWaypointToTarget(currentWaypointIndex);
    }

    public void stop() {

    }

    public void update() {
        if (car == null) {
            return;
        }

        // Check if target region is achieved
        if (track == null) {
            if (checkTargetRegion())
                targetRegion = null;
        }
        else {
            if (checkTargetRegion()) {
                // Go to next waypoint
                setNextWaypointToTarget();
            }
            else {
                // Go to next waypoint only if possible
                if (lastTriedSteeringAngle > MAX_STEERING_ANGLE_FOR_NEXT_WAYPOIN &&
                        waypointSearchTimeLeft <= 0) {
                    setNextWaypointToTargetOnlyIfSuitable();
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

    private boolean checkTargetRegion() {
        if (targetRegion == null)
            return false;

        return targetRegion.includes(car.getPosition());
    }

    private int findSuitableWaypointToTarget() {
        ArrayList<Integer> candidatesWaypoints = new ArrayList<>();

        // Search through waypoints
        int maxNumberOfOverrunWaypoints = 3;
        int waypointCount = track.getOptimalPath().size();
        for (int i = 1; i <= maxNumberOfOverrunWaypoints; ++i) {
            candidatesWaypoints.add((currentWaypointIndex+i) % waypointCount);
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

    private void setNextWaypointToTarget() {
        int nextWaypoint = findSuitableWaypointToTarget();
        if (nextWaypoint == -1) {
            int waypointCount = track.getOptimalPath().size();
            nextWaypoint = (currentWaypointIndex + 1) % waypointCount;
        }

        setWaypointToTarget(nextWaypoint);
    }

    private void setNextWaypointToTargetOnlyIfSuitable() {
        int nextWaypoint = findSuitableWaypointToTarget();
        if (nextWaypoint != -1) {
            setWaypointToTarget(nextWaypoint);
        }
    }

    private void setWaypointToTarget(int waypointIndex) {
        currentWaypointIndex = waypointIndex;

        Point targetMap = track.getOptimalPath().get(currentWaypointIndex);
        RectF targetRegion = track.getView().getScreenRegionfromTrackCoord(targetMap);
        setTargetRegion(targetRegion);

        Log.e(LOG_TAG, "currentWaypointIndex: " + currentWaypointIndex);
    }

    private void drive() {

        // If there's no target region, just brake it
        if (targetRegion == null) {
            car.brake(1.0f);
            return;
        }

        // Get pseudo path vector
        Vector2D positionVector = car.getPositionVector();
        Vector2D targetVector = targetRegion.center().vectorize().subtract(positionVector);
        float targetDistance = targetVector.length();

        // Estimate acceleration pedal power
        Vector2D velocityVector = car.getVelocity();
        float velocityScalar = velocityVector.length();
        //float velocityScalarOnPseudoPath =
        //        new Vector2D(velocityVector).dot(targetVector)/targetVector.length();
        float estimatedNumberOfUpdateToTarget = targetDistance / velocityScalar;

        // Finding the suitable steering angle for next target
        float targetDirection = targetVector.direction();
        float headDirection = car.getHeadDirection();
        float steeringAngle = targetDirection - headDirection;
        if (Math.abs(steeringAngle) > 180.0f) {
            steeringAngle = (360.0f - Math.abs(steeringAngle)) * ((steeringAngle > 0.0f) ? -1 : 1);
        }

        Log.e(LOG_TAG, "velocityScalar: " + velocityScalar);
        Log.e(LOG_TAG, "targetDistance: " + targetDistance);
        Log.e(LOG_TAG, "estimatedNumberOfUpdateToTarget: " +
                estimatedNumberOfUpdateToTarget);
        Log.e(LOG_TAG, "steeringAngle: " + steeringAngle);

        // Rule based driving policy
        float accelerationPedal;
        float brakePedal = 0.0f;

        // If the car goes into deep corner
        float estimatedNumberOfSteeringRequired = Math.abs(steeringAngle) / car.getMaxSteeringAngle();
        if (estimatedNumberOfSteeringRequired > 1.0f) {

            // Don't accelerate if it could run enough in corner
            if (velocityScalar > targetDistance) {
                accelerationPedal = 0.1f;
            }
            else {
                accelerationPedal = 0.0f;

                if (Math.abs(steeringAngle) > 90.0f) {
                    // Brake it in this situation as it'll be going far from target anyway
                    brakePedal = 1.0f;
                }
                else if (estimatedNumberOfSteeringRequired > estimatedNumberOfUpdateToTarget) {
                    float estimatedRequiredSteeringGap =
                            estimatedNumberOfSteeringRequired - estimatedNumberOfUpdateToTarget;

                    // Brake it if it's too near from target
                    if (estimatedRequiredSteeringGap > 10.0f) {
                        brakePedal = 1.0f;
                    } else if (estimatedRequiredSteeringGap > 5.0f) {
                        brakePedal = 0.5f;
                    } else {
                        brakePedal = 0.2f;
                    }
                }
            }
        }

        // If the car goes into rather straight lane
        else {
            // If it's too fast
            if (velocityScalar > targetDistance) {
                accelerationPedal = 0.0f;
                brakePedal =
                    car.getEstimatedBrakePedalPowerRequired(estimatedNumberOfUpdateToTarget);
            }
            else {
                accelerationPedal = 1.0f;
            }
        }

        // Collision sensor
        float maxSensorDistance = track.getView().getTileSize().width*10;
        float movableDistanceOnTrack =
                track.getNearestDistanceToRoadBlock(new PointF(positionVector),
                    headDirection, maxSensorDistance);
        if (movableDistanceOnTrack < maxSensorDistance) {
            float estimatedNumberOfUpdateToRoadBoundary = movableDistanceOnTrack / velocityScalar;

            if (estimatedNumberOfUpdateToRoadBoundary < 20.0f) {
                brakePedal =
                        Math.max(brakePedal,
                                car.getEstimatedBrakePedalPowerRequired(estimatedNumberOfUpdateToTarget));
            }

            if (estimatedNumberOfUpdateToRoadBoundary < 3.0f) {
                accelerationPedal = Math.min(accelerationPedal, 0.1f);
            }

            Log.e(LOG_TAG, "estimatedNumberOfUpdateToRoadBoundary: " +
                    estimatedNumberOfUpdateToRoadBoundary);
        }


        car.setSteeringAngle(steeringAngle);
        car.accelerate(accelerationPedal);
        car.brake(brakePedal);

        lastTriedSteeringAngle = steeringAngle;

        Log.e(LOG_TAG, "accelerationPedal: " + accelerationPedal);
        Log.e(LOG_TAG, "brakePedal: " + brakePedal);
    }

    private final int STARTING_WAYPOINT_INDEX = 10;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 5;
    private final float MAX_STEERING_ANGLE_FOR_NEXT_WAYPOIN = 50.0f;

    private String name;
    private float reflection;

    private Car car;
    private Track track;
    private RectF targetRegion;

    private int currentWaypointIndex = STARTING_WAYPOINT_INDEX;
    private float lastTriedSteeringAngle = 0.0f;
    private int waypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;
}
