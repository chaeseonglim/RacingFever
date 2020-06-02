package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;

public class Car extends CollidableObject {

    private static final String LOG_TAG = "Car";

    enum Type {
        MARTOZ("Martoz"),
        AVANTEDUL("Avantedul"),
        BARELA119("119 Barela");

        private static final String LOG_TAG = "Car.Type";
        private final String name;

        Type(String name) {
            this.name = name;
        }

        public Shape shape(float scale) {
            switch (this) {
                case MARTOZ:
                    return new Shape(new PointF[]{
                            new PointF(20, 12),
                            new PointF(20, 48),
                            new PointF(40, 48),
                            new PointF(40, 10)
                    }).subtract(new PointF(32, 32)).multiply(scale);
                case AVANTEDUL:
                    return new Shape(new PointF[]{
                            new PointF(24, 8),
                            new PointF(18, 14),
                            new PointF(18, 37),
                            new PointF(24, 53),
                            new PointF(39, 53),
                            new PointF(45, 37),
                            new PointF(44, 14),
                            new PointF(38, 8)
                    }).subtract(new PointF(32, 32)).multiply(scale);
                case BARELA119:
                    return new Shape(new PointF[]{
                            new PointF(26, 8),
                            new PointF(18, 16),
                            new PointF(18, 47),
                            new PointF(24, 53),
                            new PointF(39, 53),
                            new PointF(45, 47),
                            new PointF(45, 14),
                            new PointF(37, 8)
                    }).subtract(new PointF(32, 32)).multiply(scale);
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return null;
            }
        }

        public Sprite sprite(float scale) {
            Size spriteSize;
            switch (this) {
                case MARTOZ:
                    spriteSize = new Size(64, 64).multiply(scale);
                    return new Sprite.Builder("martoz.png").size(spriteSize)
                            .gridSize(new Size(1, 1)).build();
                case AVANTEDUL:
                    spriteSize = new Size(64, 64).multiply(scale);
                    return new Sprite.Builder("avantedul.png").size(spriteSize)
                            .gridSize(new Size(1, 1)).build();
                case BARELA119:
                    spriteSize = new Size(64, 64).multiply(scale);
                    return new Sprite.Builder("119_barela.png").size(spriteSize)
                            .gridSize(new Size(1, 1)).build();
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return null;
            }
        }

        public float inertia() {
            switch (this) {
                case MARTOZ:
                    return 800.0f;
                case AVANTEDUL:
                    return 800.0f;
                case BARELA119:
                    return 800.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float mass() {
            switch (this) {
                case MARTOZ:
                    return 3.0f;
                case AVANTEDUL:
                    return 10.0f;
                case BARELA119:
                    return 9.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float power() {
            switch (this) {
                case MARTOZ:
                    return 13.0f;
                case AVANTEDUL:
                    return 35.0f;
                case BARELA119:
                    return 30.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float agility() {
            switch (this) {
                case MARTOZ:
                    return 0.3f;
                case AVANTEDUL:
                    return 0.6f;
                case BARELA119:
                    return 0.65f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float maxSpeed() {
            switch (this) {
                case MARTOZ:
                    return 12.0f;
                case AVANTEDUL:
                    return 20.0f;
                case BARELA119:
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
                    .depth(1.0f).friction(0.1f).inertia(type.inertia()).mass(type.mass())
                    .headDirection(headDirection).maxSpeed(type.maxSpeed())
                    .enginePower(type.power())
                    .agility(type.agility())
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
        private float maxSpeed = Float.MAX_VALUE;
        private float enginePower = Float.MAX_VALUE;
        private float agility = Float.MAX_VALUE;

        PrivateBuilder(PointF position, Type type) {
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
        public T enginePower(float enginePower) {
            this.enginePower = enginePower;
            return (T) this;
        }
        public T maxSpeed(float maxSpeed) {
            this.maxSpeed = maxSpeed;
            return (T) this;
        }
        public T agility(float agility) {
            this.agility = agility;
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
        maxSpeed = builder.maxSpeed;
        enginePower = builder.enginePower;
        agility = builder.agility;
        modifierGeneral = 1.0f;
        lastSeekPosition = new PointF();
        collisionRecoveryLeft = 0;
        setRotation(headDirection);
        setMaxAngularVelocity(0.0f);
    }

    /**
     *
     */
    @Override
    public void update() {
        boolean wasUpdatePossible = isUpdatePossible();

        // Apply braking force to velocity
        if (brakingForce > 0.0f) {
            float velocityScalar = getVelocity().length() * (1.0f - (brakingForce /getUpdatePeriod()));
            getVelocity().truncate(velocityScalar);
        }

        // Update CollidableObject
        super.update();

        if (collisionRecoveryLeft <= 0) {
            // Update head direction
            if (wasUpdatePossible) {
                headDirection =
                        lastSeekPosition.vectorize().subtract(getPositionVector()).direction();
                setRotation(headDirection % 360.0f);
                brakingForce = 0.0f;
            }
        }
        else {
            // Resolve collision
            collisionRecoveryLeft--;
        }

        //Log.e(LOG_TAG, name + " velocity: " + getVelocity().length());
        //Log.e(LOG_TAG, name + " collisionRecoveryLeft: " + collisionRecoveryLeft);
    }

    /**
     *
     * @param collidedObject
     */
    @Override
    public void onCollisionOccurred(CollidableObject collidedObject) {
        collisionRecoveryLeft = COLLISION_RECOVERY_PERIOD;
    }

    /**
     *
     * @param targetPosition
     * @param weight
     */
    void seek(PointF targetPosition, float weight) {
        Vector2D targetVector = targetPosition.vectorize().subtract(getPositionVector());
        Vector2D desiredForce =
                targetVector.clone().normalize().multiply(getMaxSpeed())
                        .subtract(getVelocity()).multiply(weight);
        float steeringAngle = desiredForce.angle(targetVector);
        float steeringPower = (1.0f - (1.0f - getAgility()) * (steeringAngle/90.0f)) * getEnginePower();
        addForce(desiredForce.normalize().multiply(steeringPower));

        lastSeekPosition = targetPosition;
    }

    /**
     *
     * @param targetPosition
     * @param weight
     */
    void flee(PointF targetPosition, float weight) {
        Vector2D targetVector = getPositionVector().subtract(targetPosition.vectorize());
        Vector2D desiredForce =
                targetVector.clone().normalize().multiply(getMaxSpeed())
                        .subtract(getVelocity()).multiply(weight);
        float steeringAngle = desiredForce.angle(targetVector);
        float steeringPower = (1.0f - (1.0f - getAgility()) * (steeringAngle/90.0f)) * getEnginePower();
        addForce(desiredForce.normalize().multiply(steeringPower));
    }

    enum AvoidingState {
        NO_OBSTACLE,
        AVOIDING,
        BRAKING,
        PUSHING
    }

    /**
     *
     * @param obstacles
     * @param maxForwardDistance
     * @param maxBackwardDistance
     * @param track
     * @return
     */
    AvoidingState avoidObstacles(ArrayList<CollidableObject> obstacles,
                                 float maxForwardDistance,
                                 float maxBackwardDistance,
                                 Track track,
                                 float avoidingPossibility,
                                 float brakingPossibility) {
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
            float maxRoadBlockDistance = getMovingDistanceForOneUpdate() * 3;
            float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(getPosition(),
                    direction, maxRoadBlockDistance);
            if (distanceToRoadBlock > 0.0f && distanceToRoadBlock < Float.MAX_VALUE) {
                continue;
            }

            float corneringPower = getEnginePower() * getAgility();

            // If it need to go further than we can, brake it
            float avoidanceVectorLength = avoidanceVectors[i].length();
            if (avoidanceVectorLength > corneringPower) {
                if (getVelocity().length() > getMaxSpeed() * 0.8f) {
                    float brakeWeight =
                            Math.max(0.2f, 1.0f - (corneringPower / avoidanceVectorLength));
                    brake(brakeWeight);
                }
            }

            // Avoid it
            if (Math.random() < avoidingPossibility) {
                addForce(avoidanceVectors[i].truncate(corneringPower));
                return AvoidingState.AVOIDING;
            }
        }

        // There's no safe path and front obstacle is car, brake it
        if (Math.random() < brakingPossibility) {
            brake(nearestForwardObstacle, (float) ((Math.random()%0.1f-0.05f)+0.8f), 0.03f, 0.1f);
            setForce(new Vector2D());
            return AvoidingState.BRAKING;
        }

        return AvoidingState.PUSHING;
    }

    /**
     *
     * @param obstacle
     * @param direction
     * @param maxForwardDistance
     * @param maxBackwardDistance
     * @return
     */
    public float checkObstacleCanBeCollided(CollidableObject obstacle, float direction,
                                            float maxForwardDistance, float maxBackwardDistance) {

        float velocityScalar = getVelocity().length();
        if( velocityScalar == 0.0f) {
            return Float.MAX_VALUE;
        }

        int maxUpdatesBeforeMaxForwardDistance =
                Math.min((int)(maxForwardDistance / velocityScalar), getUpdatePeriod()*3);
        int maxUpdatesBeforeMaxBackwardDistance =
                Math.min((int)(maxBackwardDistance / velocityScalar), getUpdatePeriod()*2);

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
     *
     * @param obstacle
     * @param maxDistance
     * @return
     */
    private Vector2D[] getAvoidanceVectorForObstacle(CollidableObject obstacle,
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

    /**
     *
     * @param weight
     */
    private void brake(float weight) {
        brakingForce = weight;
    }

    /**
     *
     * @param cautiousObject
     * @param gapWeight
     * @param minWeight
     * @param maxWeight
     */
    private void brake(CollidableObject cautiousObject, float gapWeight, float minWeight,
                       float maxWeight) {
        float objectVelocity = cautiousObject.getVelocity().dot(getForwardVector());
        float myVelocity = getVelocity().length();
        float targetVelocity =
                Math.min(objectVelocity * gapWeight, myVelocity*(1.0f-minWeight));
        targetVelocity =
                Math.max(targetVelocity, myVelocity*(1.0f-maxWeight));

        brakingForce = 1.0f - targetVelocity/myVelocity;
    }

    /**
     *
     * @param driver
     */
    void setDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     *
     * @return
     */
    Driver getDriver() {
        return driver;
    }

    /**
     *
     * @return
     */
    boolean isCollided() {
        return (collisionRecoveryLeft > 0);
    }

    /**
     *
     * @return
     */
    float getMaxSpeed() {
        return maxSpeed * modifierGeneral;
    }

    /**
     *
     * @return
     */
    float getEnginePower() {
        return enginePower * modifierGeneral;
    }

    /**
     *
     * @return
     */
    private float getAgility() {
        return agility * modifierGeneral;
    }

    /**
     *
     * @return
     */
    float getMovingDistanceForOneUpdate() {
        return getVelocity().length() * getUpdatePeriod();
    }

    /**
     *
     * @return
     */
    public float getHeadDirection() {
        return headDirection;
    }

    /**
     *
     * @param headDirection
     */
    public void setHeadDirection(float headDirection) {
        this.headDirection = headDirection;
    }

    /**
     *
     * @return
     */
    public float getModifierGeneral() {
        return modifierGeneral;
    }

    /**
     *
     * @param modifierGeneral
     */
    public void setModifierGeneral(float modifierGeneral) {
        this.modifierGeneral = modifierGeneral;
    }

    private final int COLLISION_RECOVERY_PERIOD = 5;

    // spec
    private String name;
    private Type type;
    private float maxSpeed;
    private float agility;
    private float enginePower;

    // state
    private Driver driver;
    private int collisionRecoveryLeft = 0;
    private PointF lastSeekPosition;
    private float headDirection;
    private float brakingForce;
    private float modifierGeneral;
}
