package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.SteeringObject;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;

public class Car extends SteeringObject {

    private static final String LOG_TAG = "Car";

    enum Type {
        CAR1("CAR1");

        private static final String LOG_TAG = "Car.Type";
        private final String name;

        Type(String name) {
            this.name = name;
        }

        public Shape shape(float scale) {
            if (name == "CAR1") {
                return new Shape(new PointF[]{
                        new PointF(5, 13),
                        new PointF(2, 18),
                        new PointF(2, 23),
                        new PointF(23, 23),
                        new PointF(29, 15),
                        new PointF(29, 13),
                        new PointF(23, 8),
                        new PointF(15, 8),
                        new PointF(8, 13)
                }).subtract(new PointF(16, 16)).multiply(scale);
            }
            else {
                Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                return null;
            }
        }

        public Sprite sprite(float scale) {
            if (name == "CAR1") {
                Size spriteSize = new Size(32, 32).multiply(scale);
                return new Sprite.Builder("car1.png").size(spriteSize).build();
            }
            else {
                Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                return null;
            }
        }

        public float inertia() {
            switch (name) {
                case "CAR1":
                    return 200.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float mass() {
            switch (name) {
                case "CAR1":
                    return 10.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float power() {
            switch (name) {
                case "CAR1":
                    return 15.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float agility() {
            switch (name) {
                case "CAR1":
                    return 7.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float maxVelocity() {
            switch (name) {
                case "CAR1":
                    return 20.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }
    }

    /**
     * Builder for user
     */
    public static class Builder {
        private String name;
        private PointF position;
        private Type type;

        private float headDirection = 0.0f;
        private float scale = 1.0f;

        public Builder(String name, PointF position, Type type) {
            this.name = name;
            this.position = position;
            this.type = type;
        }
        public Builder headDirection(float headDirection) {
            this.headDirection = headDirection;
            return this;
        }
        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }
        public Car build() {
            return new PrivateBuilder<>(position, type).name(name)
                    .depth(1.0f).friction(0.03f).inertia(type.inertia()).mass(type.mass())
                    .headDirection(headDirection).maxVelocity(type.maxVelocity())
                    .maxSteeringForce(type.power()).maxLateralSteeringForce(type.agility())
                    .sprite(type.sprite(scale)).shape(type.shape(scale))
                    .visible(true).build();
        }
    }

    /**
     * Builder inherited from Engine2D
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static class PrivateBuilder<T extends Car.PrivateBuilder<T>>
            extends SteeringObject.Builder<T> {
        private String name;
        private Type type;

        private float headDirection = 0.0f;
        private float maxLateralSteeringForce = 0.0f;

        public PrivateBuilder(PointF position, Type type) {
            super(position);
            this.type = type;
        }
        public T name(String name) {
            this.name = name;
            return (T) this;
        }
        public T headDirection(float headDirection) {
            this.headDirection = headDirection;
            return (T) this;
        }
        public T maxLateralSteeringForce(float maxLateralSteeringForce) {
            this.maxLateralSteeringForce = maxLateralSteeringForce;
            return (T) this;
        }
        public Car build() {
            return new Car(this);
        }
    }

    private Car(PrivateBuilder builder) {
        super(builder);
        name = builder.name;
        type = builder.type;
        headDirection = builder.headDirection;
        maxLateralSteeringForce = builder.maxLateralSteeringForce;
        setRotation(headDirection);
    }

    @Override
    public void update() {
        if (!isUpdatePossible() || collisionRecoveryLeft > 0) {
            setSteeringForce(new Vector2D());
        }

        boolean wasUpdatePossible = isUpdatePossible();

        super.update();

        if (collisionRecoveryLeft == 0) {
            if (wasUpdatePossible) {
                headDirection = lastSeekPosition.vectorize().subtract(getPositionVector()).direction();
                if (Math.abs(lastAvoidanceSteeringAngle) < 110.0f) {
                    if (lastAvoidanceSteeringAngle > 30.0f) {
                        headDirection += 20.0f;
                    } else if (lastAvoidanceSteeringAngle < -30.0f) {
                        headDirection -= 20.0f;
                    }
                }
                lastAvoidanceSteeringAngle = 0.0f;
                setRotation((headDirection + 90.0f) % 360.0f);
            }
        }
        else {
            headDirection = (getRotation() - 90.0f) % 360.0f;
        }

        if (collisionRecoveryLeft > 0) {
            collisionRecoveryLeft--;
        }
    }

    @Override
    public void seek(PointF targetPosition, float weight) {
        super.seek(targetPosition, weight);
        lastSeekPosition = targetPosition;
    }

    public float getHeadDirection() {
        return headDirection;
    }

    @Override
    public void onCollisionOccured(CollidableObject targetObject) {
        collisionRecoveryLeft = COLLISION_RECOVERY_PERIOD;
    }

    public boolean avoidObstacles(ArrayList<CollidableObject> obstacles, float maxDistance,
                                  Track track, Vector2D targetVector) {
        // forward check
        float nearestForwardDistance = Float.MAX_VALUE;
        CollidableObject nearestForwardObstacle = null;
        for (CollidableObject obstacle : obstacles) {
            float distance = checkObstacleCollidability(obstacle, maxDistance,
                    getVelocity().direction());
            if (distance < nearestForwardDistance) {
                nearestForwardDistance = distance;
                nearestForwardObstacle = obstacle;
            }
        }

        if (nearestForwardObstacle != null) {
            Vector2D[] avoidanceVectors =
                    getAvoidanceVectorForObstacle(nearestForwardObstacle, maxDistance);
            if (avoidanceVectors == null)
                return false;

            boolean failedToAvoid = true;
            for (int i = 0; i < 2; ++i) {
                float direction = avoidanceVectors[i].direction();

                /*
                // Don't go to backward towards track if we have time
                Vector2D avoidanceAppliedVector = avoidanceVectors[i].clone().add(getVelocity());
                if (avoidanceAppliedVector.angle(targetVector) > 110.0f) {
                    continue;
                }
                */

                // check obstacles
                float nearestDistance = Float.MAX_VALUE;
                for (CollidableObject obstacle : obstacles) {
                    float distance = checkObstacleCollidability(obstacle, maxDistance, direction);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                    }
                }
                if (nearestDistance < maxDistance) {
                    continue;
                }

                // Check road block
                float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(getPosition(),
                        direction, maxDistance);
                if (distanceToRoadBlock > 0.0f && distanceToRoadBlock < Float.MAX_VALUE) {
                    continue;
                }

                // If it need to go further than we can, brake it
                float avoidanceVectorLength = avoidanceVectors[i].length();
                if (avoidanceVectorLength > maxLateralSteeringForce) {
                    avoidanceVectors[i].truncate(maxLateralSteeringForce);
                    if (getVelocity().length() > getMaxVelocity() * 0.8f) {
                        float brakeWeight = Math.max(0.2f, 1.0f - (maxLateralSteeringForce / avoidanceVectorLength));
                        brake(brakeWeight);
                    }
                }

                lastAvoidanceSteeringAngle = getVelocity().angle(avoidanceVectors[i]);
                if (getVelocity().ccw(avoidanceVectors[i]) < 0.0f) {
                    lastAvoidanceSteeringAngle *= -1.0f;
                }
                addSteeringForce(avoidanceVectors[i]);
                failedToAvoid = false;
                break;
            }
            if (failedToAvoid) {
                // There's no safe path, brake it
                if (nearestForwardObstacle instanceof  Car) {
                    brake(nearestForwardObstacle, 0.85f, 0.3f, 1.0f);
                    setSteeringForce(new Vector2D());
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Future prediction version
     * @param obstacle
     * @param maxDistance
     * @param direction
     * @return
     */
    protected float checkObstacleCollidability(CollidableObject obstacle, float maxDistance,
                                               float direction) {
        int maxUpdatesBeforeMaxDistance = (int) (maxDistance / getVelocity().length());
        int updateStep = 1;

        for (int nUpdate = 0; nUpdate <= maxUpdatesBeforeMaxDistance; nUpdate ++) {
            // if obstacle was at backward direction, only check near one
            if (nUpdate > getUpdatePeriod() * 2) {
                Vector2D unitOffset = obstacle.getPositionVector().subtract(getPositionVector()).normalize();
                float forwardness = getForwardVector().dot(unitOffset);
                if (forwardness < 0.0f) {
                    break;
                }
            }

            float radius = getShape().getRadius();
            float obstacleRadius = obstacle.getShape().getRadius();
            float totalRadius = radius + obstacleRadius;

            Vector2D futureObstaclePositionVector = obstacle.getFuturePositionVector(nUpdate);
            Vector2D futurePositionVector = getVirtualPositionVector(direction, nUpdate);
            Vector2D localOffset = futureObstaclePositionVector.clone().subtract(futurePositionVector);

            // Not collided
            if (localOffset.lengthSq() > totalRadius * totalRadius)
                continue;

            Vector2D currentOffset = futureObstaclePositionVector.clone().subtract(getPositionVector());
            return currentOffset.length();
        }

        return Float.MAX_VALUE;
    }

    /**
     * Return possible seering force to avoid an obstacle in opposite direction
     * @param obstacle
     * @param maxDistance
     * @return
     */
    protected Vector2D[] getAvoidanceVectorForObstacle(CollidableObject obstacle, float maxDistance) {
        int maxUpdatesBeforeMaxDistance = (int) (maxDistance / getVelocity().length());
        for (int nUpdate = 0; nUpdate <= maxUpdatesBeforeMaxDistance; nUpdate ++) {
            float radius = getShape().getRadius();
            float obstacleRadius = obstacle.getShape().getRadius();
            float totalRadius = radius + obstacleRadius;

            Vector2D futureObstaclePositionVector = obstacle.getFuturePositionVector(nUpdate);
            Vector2D futurePositionVector = getFuturePositionVector(nUpdate);
            Vector2D localOffset = futureObstaclePositionVector.subtract(futurePositionVector);

            float forwardComponent = localOffset.dot(getForwardVector());

            // Obstacle is not at front
            if (forwardComponent <= 0) {
                continue;
            }

            // Obstacle is far from here
            if (forwardComponent >= maxDistance) {
                continue;
            }

            Vector2D forwardOffset = getForwardVector().multiply(forwardComponent);
            Vector2D offForwardOffset = localOffset.clone().subtract(forwardOffset);

            // If it's in cylinder
            float offForwardOffsetlength = offForwardOffset.length();
            if (offForwardOffsetlength < totalRadius) {
                float intendedSteeringPower = totalRadius - offForwardOffsetlength;
                float oppositeIntendedSteeringPower = totalRadius + offForwardOffsetlength;

                if (nUpdate > 0) {
                    intendedSteeringPower /= (double) nUpdate / getUpdatePeriod();
                    oppositeIntendedSteeringPower /= (double) nUpdate / getUpdatePeriod();
                }

                Vector2D[] steeringPowers = new Vector2D[2];
                steeringPowers[0] = offForwardOffset.clone().normalize()
                        .multiply(intendedSteeringPower).multiply(-1);
                steeringPowers[1] = offForwardOffset.clone().normalize()
                        .multiply(oppositeIntendedSteeringPower);

                localOffsetLine.set(new PointF(getFuturePositionVector(nUpdate)), localOffset);
                localOffsetLine.commit(); // blue
                forwardOffsetLine.set(new PointF(getFuturePositionVector(nUpdate)), forwardOffset);
                forwardOffsetLine.commit(); // red

                return steeringPowers;
            }
        }

        return null;
    }

    public void avoidObstacle(CollidableObject obstacle, float maxDistance) {
        Vector2D[] avoidancePower = getAvoidanceVectorForObstacle(obstacle, maxDistance);
        if (avoidancePower != null) {
            addSteeringForce(avoidancePower[0]);
        }
    }

    // spec
    private String name;
    private Type type;
    private float maxLateralSteeringForce;

    // state
    private final int COLLISION_RECOVERY_PERIOD = 10;
    private int collisionRecoveryLeft = 0;
    private PointF lastSeekPosition;
    private float lastAvoidanceSteeringAngle = 0.0f;
    private float headDirection;
}
