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

        public Shape getShape(float scale) {
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

        public Sprite getSprite(float scale) {
            if (name == "CAR1") {
                Size spriteSize = new Size(32, 32).multiply(scale);
                return new Sprite.Builder("car1.png").size(spriteSize).build();
            }
            else {
                Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                return null;
            }
        }

        public float getInertia() {
            switch (name) {
                case "CAR1":
                    return 100.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getMass() {
            switch (name) {
                case "CAR1":
                    return 1.1f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getEnginePower() {
            switch (name) {
                case "CAR1":
                    return 15.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getMaxVelocity() {
            switch (name) {
                case "CAR1":
                    return 100.0f;
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
        private PointF position;
        private Type type;

        private float headDirection = 0.0f;
        private float scale = 1.0f;

        public Builder(PointF position, Type type) {
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
            return new PrivateBuilder<>(position, type)
                    .depth(1.0f).friction(0.03f).inertia(type.getInertia()).mass(type.getMass())
                    .headDirection(headDirection).maxVelocity(type.getMaxVelocity())
                    .maxSteeringForce(type.getEnginePower())
                    .sprite(type.getSprite(scale)).shape(type.getShape(scale))
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
        Type type;
        float headDirection = 0.0f;

        public PrivateBuilder(PointF position, Type type) {
            super(position);
            this.type = type;
        }
        public T headDirection(float headDirection) {
            this.headDirection = headDirection;
            return (T) this;
        }
        public Car build() {
            return new Car(this);
        }
    }

    private Car(PrivateBuilder builder) {
        super(builder);
        type = builder.type;
        headDirection = builder.headDirection;
        setRotation(headDirection);
    }

    @Override
    public void update() {
        if (!isUpdatePossible() || collisionResolveLeft > 0) {
            setSteeringForce(new Vector2D());
        }

        super.update();

        if (collisionResolveLeft == 0) {
            headDirection = getVelocity().direction();
            setRotation((headDirection + 90.0f) % 360.0f);
            setCollisionEnabled(true);
        }
        else {
            headDirection = (getRotation() - 90.0f) % 360.0f;
        }

        if (collisionResolveLeft > 0) {
            collisionResolveLeft--;
        }
    }

    public float getHeadDirection() {
        return headDirection;
    }

    @Override
    public void onCollisionOccured(CollidableObject targetObject) {
        collisionResolveLeft = COLLISION_RESOLVE_PERIOD;
        setCollisionEnabled(false);
    }

    public boolean avoidObstacles(ArrayList<CollidableObject> obstacles, float maxDistance,
                                  Track track) {
        float nearestDistance = Float.MAX_VALUE;
        CollidableObject nearestObstacle = null;
        for (CollidableObject obstacle : obstacles) {
            float distance = checkObstacleCanCollide(obstacle, maxDistance);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestObstacle = obstacle;
            }
        }

        if (nearestObstacle != null) {
            Vector2D[] avoidanceVectors = getAvoidanceVectorForObstacle(nearestObstacle, maxDistance);
            if (avoidanceVectors == null)
                return false;

            String[] testingOrder = new String[2];

            if (getForwardVector().ccw(avoidanceVectors[0]) > 0.0f) {
                testingOrder[0] = "cw";
                testingOrder[1] = "ccw";
            }
            else {
                testingOrder[0] = "ccw";
                testingOrder[1] = "cw";
            }

            boolean needBrake = true;
            for (int i = 0; i < testingOrder.length; ++i) {
                float directionDelta = 0.0f;
                if (testingOrder[i] == "ccw") {
                    directionDelta = -45.0f;
                }
                else if (testingOrder[i] == "cw") {
                    directionDelta = 45.0f;
                }
                float testingDirection = getHeadDirection() + directionDelta;

                float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(getPosition(),
                        testingDirection, maxDistance);

                if (distanceToRoadBlock == 0.0f || distanceToRoadBlock == Float.MAX_VALUE) {
                    // if it's already in road block or not close to road block, steer it
                    getSteeringForce().add(avoidanceVectors[i]);
                    needBrake = false;
                    break;
                }
            }
            if (needBrake) {
                // There's no safe path, brake it
                getSteeringForce().add(new Vector2D(getVelocity()).multiply(-1)
                        .multiply(brakePower));
            }

            return true;
        }

        return false;
    }

    @Override
    protected float checkObstacleCanCollide(CollidableObject obstacle, float maxDistance) {
        int maxUpdatesBeforeMaxDistance = (int) (maxDistance / getVelocity().length());
        for (int nUpdate = 0; nUpdate <= maxUpdatesBeforeMaxDistance; ++nUpdate) {
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
            Vector2D offForwardOffset = new Vector2D(localOffset).subtract(forwardOffset);

            // If it's not in cylinder
            if (offForwardOffset.length() >= totalRadius) {
                continue;
            }

            return forwardComponent;
        }

        return Float.MAX_VALUE;
    }

    /**
     * Return possible seering force to avoid an obstacle in opposite direction
     * @param obstacle
     * @param maxDistance
     * @return
     */
    public Vector2D[] getAvoidanceVectorForObstacle(CollidableObject obstacle, float maxDistance) {
        int maxUpdatesBeforeMaxDistance = (int) (maxDistance / getVelocity().length());
        for (int nUpdate = 0; nUpdate <= maxUpdatesBeforeMaxDistance; ++nUpdate) {
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
            Vector2D offForwardOffset = new Vector2D(localOffset).subtract(forwardOffset);

            // If it's in cylinder
            float offForwardOffsetlength = offForwardOffset.length();
            if (offForwardOffsetlength < totalRadius) {
                float intendedSteeringPower = totalRadius - offForwardOffsetlength;
                if (nUpdate > 0) {
                    intendedSteeringPower /= nUpdate / getUpdatePeriod();

                    // magic salt
                    intendedSteeringPower *= 1.3;
                }
                if (intendedSteeringPower > getMaxSteeringForce()) {
                    intendedSteeringPower = getMaxSteeringForce();
                }

                Vector2D[] steeringPowers = new Vector2D[2];
                steeringPowers[0] = new Vector2D(offForwardOffset.normalize()
                        .multiply(intendedSteeringPower).multiply(-1));
                steeringPowers[1] = new Vector2D(offForwardOffset.multiply(-1).normalize()
                        .multiply(obstacleRadius+offForwardOffsetlength));

                localOffsetLine.set(new PointF(getFuturePositionVector(nUpdate)), localOffset);
                localOffsetLine.commit();
                forwardOffsetLine.set(new PointF(getFuturePositionVector(nUpdate)), forwardOffset);
                forwardOffsetLine.commit();
                return steeringPowers;
            }
        }

        return null;
    }

    // spec
    private Type type;
    private float brakePower = 0.2f;

    // state
    private final int COLLISION_RESOLVE_PERIOD = 30;
    private int collisionResolveLeft = 0;

    private float headDirection;
}
