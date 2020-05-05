package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.Vector2D;

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
        reflectionTick = reflection == 0.0f ? 0.0f : reflection/100.0f;
    }

    public void ride(Car car) {
        this.car = car;
    }

    public void learn(Track track) {
        this.track = track;
    }

    public void start() {
        Point targetMap = track.getOptimalPath().get(currentWaypointTarget);
        Rect targetRegion = track.getView().getScreenRegionOfMapDataCoord(targetMap);
        setTargetPosition(targetRegion.exactCenter());
    }

    public void stop() {

    }

    public void update() {
        if (car == null) {
            return;
        }

        // Get the target position to the waypoint
        Point targetMap = track.getOptimalPath().get(currentWaypointTarget);
        Rect targetRegion = track.getView().getScreenRegionOfMapDataCoord(targetMap);

        // First checking the target position is achieved
        if (targetRegion.includes(new Point(car.getPosition()))) {
            currentWaypointTarget++;
            if (currentWaypointTarget >= track.getOptimalPath().size())
                currentWaypointTarget = 0;

            // Update the target position if needed
            targetMap = track.getOptimalPath().get(currentWaypointTarget);
            targetRegion = track.getView().getScreenRegionOfMapDataCoord(targetMap);
            setTargetPosition(targetRegion.exactCenter());
        }

        // Driver reflection on car
        if (residualTimeForReflect - reflectionTick <= 0.0f && targetPosition != null) {
            Log.e(LOG_TAG, "currentWaypointTarget: " + currentWaypointTarget);

            // Finding the suitable steering angle for next target
            Vector2D curPosition = car.getPositionVector();
            Vector2D pseudoPath = targetPosition.vectorize().subtract(curPosition);
            float headDirection = car.getHeadDirection();
            float targetDirection = pseudoPath.direction();
            float steeringAngle = targetDirection - headDirection;
            if (Math.abs(steeringAngle) > 180.0f) {
                steeringAngle = (360.0f - Math.abs(steeringAngle)) * ((steeringAngle>0.0f)?-1:1);
            }

            // Accelerate car
            car.accelerate(1.0f, steeringAngle);

            residualTimeForReflect += 1.0f;
        }
        residualTimeForReflect -= reflectionTick;
    }

    public void setTargetPosition(PointF targetPosition) {
        this.targetPosition = targetPosition;
    }

    private String name;
    private float reflection;

    private Car car;
    private Track track;
    private PointF targetPosition;

    private float reflectionTick;
    private float residualTimeForReflect = 0.0f;
    private int currentWaypointTarget = 10;
}
