package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.Line;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;
import java.util.Iterator;

public class Car extends CollidableObject {

    private static final String LOG_TAG = "Car";

    enum Type {
        CAR1("CAR1");

        private static final String LOG_TAG = "Car.Type";
        private final String name;

        Type(String name) {
            this.name = name;
        }

        public Shape shape(float scale) {
            if (name.equals("CAR1")) {
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
            if (name.equals("CAR1")) {
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
                    return 200.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float agility() {
            switch (name) {
                case "CAR1":
                    return 70.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float maxVelocity() {
            switch (name) {
                case "CAR1":
                    return 18.0f;
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
                    .depth(1.0f).friction(0.05f).inertia(type.inertia()).mass(type.mass())
                    .headDirection(headDirection).maxVelocity(type.maxVelocity())
                    .maxForwardSteeringForce(type.power())
                    .maxLateralSteeringForce(type.agility())
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
            extends CollidableObject.Builder<T> {
        private String name;
        private Type type;

        private float headDirection = 0.0f;
        private float maxForwardSteeringForce = Float.MAX_VALUE;
        private float maxLateralSteeringForce = Float.MAX_VALUE;

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
        public T maxForwardSteeringForce(float maxForwardSteeringForce) {
            this.maxForwardSteeringForce = maxForwardSteeringForce;
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
        maxForwardSteeringForce = builder.maxForwardSteeringForce;
        maxLateralSteeringForce = builder.maxLateralSteeringForce;
        setRotation(headDirection);
        effects = new ArrayList<>();
        modifier = 1.0f;
    }

    @Override
    public void update() {
        // Update effect modifier
        modifier = updateEffects();

        // Reset force if it's not updatable period
        if (!isUpdatePossible()) {
            setForce(new Vector2D());
        }

        boolean wasUpdatePossible = isUpdatePossible();

        // Apply braking force to velocity
        if (brakingForce > 0.0f) {
            float velocityScalar = getVelocity().length() * (1.0f - (brakingForce /getUpdatePeriod()));
            getVelocity().truncate(velocityScalar);
        }

        // Update CollidableObject
        super.update();

        // Update head direction
        if (collisionRecoveryLeft == 0) {
            if (wasUpdatePossible) {
                headDirection =
                        lastSeekPosition.vectorize().subtract(getPositionVector()).direction();
                setRotation((headDirection + 90.0f) % 360.0f);
                brakingForce = 0.0f;
            }
        }
        else {
            headDirection = (getRotation() - 90.0f) % 360.0f;
        }

        // Resolve collision
        if (collisionRecoveryLeft > 0) {
            collisionRecoveryLeft--;
        }

        //Log.e(LOG_TAG, name + " velocity: " + getVelocity().length());
        //Log.e(LOG_TAG, name + " collisionRecoveryLeft: " + collisionRecoveryLeft);
    }

    public float getHeadDirection() {
        return headDirection;
    }

    @Override
    public void onCollisionOccurred(CollidableObject targetObject) {
        collisionRecoveryLeft = COLLISION_RECOVERY_PERIOD;
    }

    public void seek(PointF targetPosition, float weight) {
        Vector2D targetVector = targetPosition.vectorize().subtract(getPositionVector());
        Vector2D desiredForce =
                targetVector.clone().normalize().multiply(getMaxVelocity())
                        .subtract(getVelocity()).multiply(weight);
        addAdjustedForce(desiredForce, getMaxForwardSteeringForce());

        lastSeekPosition = targetPosition;
    }

    public void flee(PointF targetPosition, float weight) {
        Vector2D targetVector = getPositionVector().subtract(targetPosition.vectorize());
        Vector2D desiredForce =
                targetVector.clone().normalize().multiply(getMaxVelocity())
                        .subtract(getVelocity()).multiply(weight);
        addAdjustedForce(desiredForce, getMaxForwardSteeringForce());
    }

    enum AvoidingState {
        NO_OBSTACLE,
        AVOIDING,
        BRAKING,
        PUSHING
    }

    public AvoidingState avoidObstacles(ArrayList<CollidableObject> obstacles,
                                        float maxForwardDistance, float maxBackwardDistance,
                                        Track track) {
        // Check forward direction
        float nearestForwardDistance = Float.MAX_VALUE;
        CollidableObject nearestForwardObstacle = null;
        for (CollidableObject obstacle : obstacles) {
            float distance = checkObstacleCanBeCollided(obstacle, getVelocity().direction(),
                    maxForwardDistance, maxBackwardDistance);
            if (distance < nearestForwardDistance) {
                nearestForwardDistance = distance;
                nearestForwardObstacle = obstacle;
            }
        }

        if (nearestForwardObstacle == null) {
            return AvoidingState.NO_OBSTACLE;
        }

        Vector2D[] avoidanceVectors =
                getAvoidanceVectorForObstacle(nearestForwardObstacle, maxForwardDistance);
        if (avoidanceVectors == null) {
            return AvoidingState.NO_OBSTACLE;
        }

        for (int i = 0; i < 2; ++i) {
            float direction = avoidanceVectors[i].direction();

            // Check obstacles in avoidance direction
            float nearestDistance = Float.MAX_VALUE;
            for (CollidableObject obstacle : obstacles) {
                float distance = checkObstacleCanBeCollided(obstacle, direction, maxForwardDistance,
                        maxBackwardDistance);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                }
            }
            if (nearestDistance < maxForwardDistance) {
                continue;
            }

            // Check road block
            float maxRoadBlockDistance = getVelocity().length() * getUpdatePeriod() * 3;
            float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(getPosition(),
                    direction, maxRoadBlockDistance);
            if (distanceToRoadBlock > 0.0f && distanceToRoadBlock < Float.MAX_VALUE) {
                continue;
            }

            // If it need to go further than we can, brake it
            float avoidanceVectorLength = avoidanceVectors[i].length();
            if (avoidanceVectorLength > getMaxLateralSteeringForce()) {
                if (getVelocity().length() > getMaxVelocity() * 0.8f) {
                    float brakeWeight =
                            Math.max(0.2f, 1.0f - (getMaxLateralSteeringForce() / avoidanceVectorLength));
                    brake(brakeWeight);
                }
            }

            // If it's not car, avoid it
            if (!(nearestForwardObstacle instanceof Car)) {
                addAdjustedForce(avoidanceVectors[i], getMaxLateralSteeringForce());
                return AvoidingState.AVOIDING;
            }
        }

        // There's no safe path and front obstacle is car, brake it
        if (nearestForwardObstacle instanceof Car) {
            brake(nearestForwardObstacle, (float) ((Math.random()%0.1f-0.05f)+0.8f), 0.1f, 0.3f);
            setForce(new Vector2D());
            return AvoidingState.BRAKING;
        }

        return AvoidingState.PUSHING;
    }

    /**
     * Future prediction version
     * @param obstacle
     * @param maxForwardDistance
     * @param direction
     * @return
     */
    public float checkObstacleCanBeCollided(CollidableObject obstacle, float direction,
                                            float maxForwardDistance, float maxBackwardDistance) {

        float velocityScalar = getVelocity().length();
        int maxUpdatesBeforeMaxForwardDistance = (int) (maxForwardDistance / velocityScalar);
        int maxUpdatesBeforeMaxBackwardDistance = (int) (maxBackwardDistance / velocityScalar);

        for (int nUpdate = 0; nUpdate <= maxUpdatesBeforeMaxForwardDistance; nUpdate ++) {

            // if obstacle was at backward direction, only check near one
            if (nUpdate > maxUpdatesBeforeMaxBackwardDistance) {
                Vector2D unitOffset = obstacle.getPositionVector().subtract(getPositionVector()).normalize();
                float forwardness = getForwardVector().dot(unitOffset);
                if (forwardness < 0.0f) {
                    break;
                }
            }

            Vector2D futureObstaclePositionVector = obstacle.getFuturePositionVector(nUpdate);
            Vector2D futurePositionVector = getVirtualPositionVector(direction, nUpdate);
            Vector2D localOffset = futureObstaclePositionVector.clone().subtract(futurePositionVector);

            // Check it's close enough to collide
            /*
            float radius = getShape().getSupportPoint(localOffset.clone().multiply(-1))
                    .distance(getPositionVector());
            float obstacleRadius = obstacle.getShape().getSupportPoint(localOffset)
                    .distance(obstacle.getPositionVector());
             */
            float radius = getShape().getRadius();
            float obstacleRadius = obstacle.getShape().getRadius();
            float totalRadius = radius + obstacleRadius;

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
    protected Vector2D[] getAvoidanceVectorForObstacle(CollidableObject obstacle,
                                                       float maxDistance) {

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

                return steeringPowers;
            }
        }

        return null;
    }

    public void avoidObstacle(CollidableObject obstacle, float maxDistance) {
        Vector2D[] avoidancePower = getAvoidanceVectorForObstacle(obstacle, maxDistance);
        if (avoidancePower != null) {
            addAdjustedForce(avoidancePower[0], getMaxLateralSteeringForce());
        }
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setHeadDirection(float headDirection) {
        this.headDirection = headDirection;
    }

    public boolean isCollided() {
        return (collisionRecoveryLeft > 0);
    }

    public void brake(float weight) {
        brakingForce = weight;
    }

    public void brake(CollidableObject cautiousObject, float gapWeight,
                      float minWeight, float maxWeight) {
        float objectVelocity = cautiousObject.getVelocity().dot(getForwardVector());
        float myVelocity = getVelocity().length();
        float targetVelocity =
                Math.min(objectVelocity * gapWeight, myVelocity*(1.0f-minWeight));
        targetVelocity =
                Math.max(targetVelocity, myVelocity*(1.0f-maxWeight));

        brakingForce = 1.0f - targetVelocity/myVelocity;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    private float updateEffects() {
        float modifier = 1.0f;

        for(Iterator<Effect> it = effects.iterator(); it.hasNext() ; ) {
            Effect effect = it.next();

            modifier *= effect.getModifier();

            effect.tick();
            if (effect.isExpired()) {
                it.remove();
            }
        }

        return modifier;
    }

    @Override
    public float getMaxVelocity() {
        return super.getMaxVelocity() * modifier;
    }

    public float getMaxForwardSteeringForce() {
        return maxForwardSteeringForce * modifier;
    }

    public float getMaxLateralSteeringForce() {
        return maxLateralSteeringForce * modifier;
    }

    private final int COLLISION_RECOVERY_PERIOD = 5;

    // spec
    private String name;
    private Type type;
    private float maxLateralSteeringForce;
    private float maxForwardSteeringForce;

    // state
    private int collisionRecoveryLeft = 0;
    private PointF lastSeekPosition;
    private float headDirection;
    private Driver driver;
    private float brakingForce;
    private ArrayList<Effect> effects;
    private float modifier;
}
