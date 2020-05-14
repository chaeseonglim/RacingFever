package com.lifejourney.engine2d;

import android.util.Log;

import java.util.ArrayList;

public class SteeringObject extends CollidableObject {
    static final String LOG_TAG = "SteeringObject";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends SteeringObject.Builder<T>>
            extends CollidableObject.Builder<T> {
        // optional parameter
        private float maxSteeringForce = 10.0f;

        public Builder(PointF position) {
            super(position);
        }
        public T maxSteeringForce(float maxSteeringForce) {
            this.maxSteeringForce = maxSteeringForce;
            return (T)this;
        }
        public SteeringObject build() {
            return new SteeringObject(this);
        }
    }

    protected SteeringObject(Builder builder) {
        super(builder);

        maxSteeringForce = builder.maxSteeringForce;
        steeringForce = new Vector2D();

        localOffsetLine = new Line.Builder(new PointF(), new PointF()).visible(true)
                .color(0.0f, 0.0f, 1.0f, 1.0f).build();
        forwardOffsetLine = new Line.Builder(new PointF(), new PointF()).visible(true)
                .color(1.0f, 0.0f, 0.0f, 1.0f).build();
    }

    @Override
    public void update() {
        if (isUpdatePossible()) {
            adjustSteeringForce();
            addForce(steeringForce);
            steeringForce.reset();
        }

        super.update();
    }

    private void adjustSteeringForce() {
        // Set steeringForce limit to maxSteeringForce
        if (steeringForce.lengthSq() > Math.pow(maxSteeringForce, 2)) {
            steeringForce.normalize().multiply(maxSteeringForce);
        }

        // Reduce velocity and steering power if steering angle is stiff
        float steeringAngle = Math.abs(getVelocity().clone().add(steeringForce).angle(getVelocity()));
        if (steeringAngle > 90.0f) {
            steeringForce.multiply(0.5f);
            getVelocity().multiply(0.7f);
        }
        else if (steeringAngle > 60.0f) {
            steeringForce.multiply(0.7f);
            getVelocity().multiply(0.8f);
        }
        else if (steeringAngle > 30.0f) {
            steeringForce.multiply(0.8f);
            getVelocity().multiply(0.9f);
        }
    }

    public void seek(Object object, float weight) {
        seek(object.getPosition(), weight);
    }

    public void seek(PointF targetPosition, float weight) {
        Vector2D targetVector = targetPosition.vectorize().subtract(getPositionVector());
        Vector2D desiredVelocity =
                targetVector.clone().normalize().multiply(maxSteeringForce);
        addSteeringForce(desiredVelocity.subtract(getVelocity()).multiply(weight));
    }

    public void flee(Object object, float weight) {
        flee(object.getPosition(), weight);
    }

    public void flee(PointF targetPosition, float weight) {
        Vector2D targetVector = getPositionVector().subtract(targetPosition.vectorize());
        Vector2D desiredVelocity = targetVector.clone().normalize().multiply(getMaxVelocity());
        addSteeringForce(desiredVelocity.subtract(getVelocity()).multiply(weight));
    }

    public void brake(float weight) {
        // In case of brake, it's not an steering power so reduce velocity directly
        getVelocity().multiply(1.0f - weight);
        //addSteeringForce(getVelocity().clone().multiply(-1).multiply(weight));
    }

    public void cohension(ArrayList<CollidableObject> neighbors, float maxDistance, float weight) {
        int count = 0;
        Vector2D mean = new Vector2D();
        for (MovableObject boid : neighbors) {
            Vector2D offset = boid.getPositionVector().subtract(getPositionVector());
            float distanceSq = offset.lengthSq();
            if (distanceSq > maxDistance*maxDistance)
                continue;

            mean.add(boid.getPositionVector());
            count++;
        }

        if (count > 0) {
            addSteeringForce(mean.divide(count).subtract(getPositionVector())
                    .normalize().multiply(weight*maxSteeringForce));
        }
    }

    public void alignment(ArrayList<CollidableObject> neighbors, float maxDistance, float weight) {
        int count = 0;
        Vector2D mean = new Vector2D();
        for (MovableObject boid : neighbors) {
            Vector2D offset = boid.getPositionVector().subtract(getPositionVector());
            float distanceSq = offset.lengthSq();
            if (distanceSq > maxDistance*maxDistance)
                continue;

            mean.add(boid.getForwardVector());
            count++;
        }

        if (count > 0) {
            addSteeringForce(mean.divide(count).subtract(getForwardVector())
                    .multiply(weight*maxSteeringForce));
        }
    }

    public void separation(ArrayList<CollidableObject> neighbors, float maxDistance, float weight) {
        int count = 0;
        Vector2D mean = new Vector2D();
        for (MovableObject boid : neighbors) {
            Vector2D offset = boid.getPositionVector().subtract(getPositionVector());
            float distanceSq = offset.lengthSq();
            if (distanceSq > maxDistance*maxDistance)
                continue;

            mean.add(offset.divide(distanceSq).multiply(-1));
            count++;
        }

        if (count > 0) {
            addSteeringForce(mean.divide(count).normalize().multiply(weight*maxSteeringForce));
        }
    }

    public boolean avoidObstacles(ArrayList<CollidableObject> obstacles, float maxDistance) {
        float nearestDistance = Float.MAX_VALUE;
        CollidableObject nearestObstacle = null;
        for (CollidableObject obstacle : obstacles) {
            float distance = checkObstacleCanCollide(obstacle, maxDistance,
                    getVelocity().direction(), Integer.MAX_VALUE);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestObstacle = obstacle;
            }
        }

        if (nearestObstacle != null) {
            avoidObstacle(nearestObstacle, maxDistance);
            return true;
        }

        return false;
    }

    protected float checkObstacleCanCollide(CollidableObject obstacle, float maxDistance,
                                            float direction, int maxUpdatesToPredict) {
        int maxUpdatesBeforeMaxDistance =
                Math.min((int) (maxDistance / getVelocity().length()), maxUpdatesToPredict);
        int updateStep = 1;
        for (int nUpdate = 0; nUpdate <= maxUpdatesBeforeMaxDistance; nUpdate += updateStep) {
            if (nUpdate >= getUpdatePeriod()) {
                updateStep = 2;
            }

            float radius = getShape().getRadius();
            float obstacleRadius = obstacle.getShape().getRadius();
            float totalRadius = radius + obstacleRadius;

            Vector2D futureObstaclePositionVector = obstacle.getFuturePositionVector(nUpdate);
            Vector2D futurePositionVector = getVirtualPositionVector(direction, nUpdate);
            Vector2D localOffset = futureObstaclePositionVector.subtract(futurePositionVector);

            float forwardComponent = localOffset.dot(new Vector2D(direction));

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
    protected Vector2D[] getAvoidanceVectorForObstacle(CollidableObject obstacle, float maxDistance) {
        int maxUpdatesBeforeMaxDistance = (int) (maxDistance / getVelocity().length());
        int updateStep = 1;
        for (int nUpdate = 0; nUpdate <= maxUpdatesBeforeMaxDistance; nUpdate += updateStep) {
            if (nUpdate >= getUpdatePeriod()) {
                updateStep = 2;
            }

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
                Vector2D newSteeringPower = offForwardOffset;
                float intendedSteeringPower = totalRadius - offForwardOffsetlength;

                if (nUpdate > 0) {
                    intendedSteeringPower /= nUpdate / getUpdatePeriod();

                    // magic salt
                    intendedSteeringPower *= 1.5;
                }
                if (intendedSteeringPower > getMaxSteeringForce()) {
                    intendedSteeringPower = getMaxSteeringForce();
                }

                Vector2D[] steeringPowers = new Vector2D[2];
                steeringPowers[0] = new Vector2D(newSteeringPower.normalize()
                        .multiply(intendedSteeringPower).multiply(-1));
                steeringPowers[1] = new Vector2D(newSteeringPower.multiply(-1).normalize()
                        .multiply(obstacleRadius+offForwardOffsetlength));

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

    public float getMaxSteeringForce() {
        return maxSteeringForce;
    }

    public void setMaxSteeringForce(float maxSteeringForce) {
        this.maxSteeringForce = maxSteeringForce;
    }

    public Vector2D getSteeringForce() {
        return steeringForce;
    }

    public void setSteeringForce(Vector2D steeringForce) {
        this.steeringForce = steeringForce;
    }

    public void addSteeringForce(Vector2D steeringForce) {
        this.steeringForce.add(steeringForce.clone());
    }

    private float maxSteeringForce;
    private Vector2D steeringForce;

    // debugging
    protected Line localOffsetLine;
    protected Line forwardOffsetLine;
}
