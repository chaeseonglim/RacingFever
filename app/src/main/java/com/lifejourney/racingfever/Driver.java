package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.PointF;
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
        reflectionTick = reflection == 0.0f ? 0.0f : 30.0f / reflection;
    }

    public void ride(Car car) {
        this.car = car;
    }

    public void update() {
        if (targetPosition != null && car != null) {
            if (residualTimeForReflect - reflectionTick <= 0.0f) {
                // Finding the suitable steering angle for next target
                Vector2D curPosition = car.getPositionVector();
                Vector2D pseudoPath = targetPosition.vectorize().subtract(curPosition);
                float curDirection = car.getVelocity().direction();
                float targetDirection = pseudoPath.direction();
                float steeringAngle = targetDirection - curDirection;
                if (Math.abs(steeringAngle) > 180.0f) {
                    steeringAngle = (Math.abs(steeringAngle) - 180.0f) *
                            ((steeringAngle < 0.0f) ? -1 : 1);
                }

                // Accelerate car
                car.accelerate(1.0f, steeringAngle);

                residualTimeForReflect += 30.0f;
            }
            residualTimeForReflect -= reflectionTick;
        }
    }

    public void setTargetPosition(PointF targetPosition) {
        this.targetPosition = targetPosition;
    }

    String name;
    Car car;
    float reflection;
    PointF targetPosition;

    float reflectionTick = 0.0f;
    float residualTimeForReflect = 0.0f;
}
