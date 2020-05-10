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
        if (steeringForce.lengthSq() > Math.pow(maxSteeringForce, 2)) {
            steeringForce.normalize().multiply(maxSteeringForce);
        }
    }

    public void seek(Object object) {
        seek(object.getPosition());
    }

    public void seek(PointF targetPosition) {
        Vector2D targetVector = targetPosition.vectorize().subtract(getPositionVector());
        Vector2D desiredVelocity =
                new Vector2D(targetVector).normalize().multiply(maxSteeringForce);
        steeringForce.add(desiredVelocity.subtract(getVelocity()));
    }

    public void flee(Object object) {
        flee(object.getPosition());
    }

    public void flee(PointF targetPosition) {
        Vector2D targetVector = getPositionVector().subtract(targetPosition.vectorize());
        Vector2D desiredVelocity = new Vector2D(targetVector).normalize().multiply(getMaxVelocity());
        Vector2D adjustedForce = desiredVelocity.subtract(getVelocity());
        steeringForce.add(adjustedForce);
    }

    public float checkObstacleIntersected(CollidableObject obstacle) {
        float radius = getShape().getRadius();
        float obstacleRadius = obstacle.getShape().getRadius();
        float totalRadius = radius + obstacleRadius;

        float minDistanceToCollision = getVelocity().length() * getUpdatePeriod() * 3;
        float minDistanceToCenter = minDistanceToCollision + obstacleRadius;

        Vector2D futureObstaclePositionVector = obstacle.getPositionVector()
                .add(obstacle.getVelocity());
        Vector2D futurePositionVector = getPositionVector().add(getVelocity());
        Vector2D localOffset = futureObstaclePositionVector.subtract(futurePositionVector);

        float forwardComponent = localOffset.dot(new Vector2D(getVelocity()).normalize());

        // Obstacle is not at front
        if (forwardComponent <= 0) {
            return Float.MAX_VALUE;
        }

        // Obstacle is far from here
        if (forwardComponent >= minDistanceToCenter) {
            return Float.MAX_VALUE;
        }

        Vector2D forwardOffset = new Vector2D(getVelocity()).normalize().multiply(forwardComponent);
        Vector2D offForwardOffset = new Vector2D(localOffset).subtract(forwardOffset);

        // If it's not in cylinder
        if (offForwardOffset.length() >= totalRadius) {
            return Float.MAX_VALUE;
        }

        return forwardComponent;
    }

    public void avoidObstacles(ArrayList<CollidableObject> obstacles) {
        float minDistanceToCollision = getVelocity().length() * getUpdatePeriod() * 5;
        float nearestDistance = Float.MAX_VALUE;
        CollidableObject nearestObstacle = null;
        for (CollidableObject obstacle : obstacles) {

            float distance = checkObstacleIntersected(obstacle);
            //float distance = findIntersectionWithObject(obstacle);
            if (distance < nearestDistance && distance <= minDistanceToCollision) {
                nearestDistance = distance;
                nearestObstacle = obstacle;
            }
        }

        if (nearestObstacle != null) {
            avoidObstacle(nearestObstacle);
        }
    }

    public void avoidObstacle(CollidableObject obstacle) {
        float radius = getShape().getRadius();
        float obstacleRadius = obstacle.getShape().getRadius();
        float minDistanceToCollision = getVelocity().length() * 5;
        float minDistanceToCenter = minDistanceToCollision + obstacleRadius;
        float totalRadius = radius + obstacleRadius;

        Vector2D futureObstaclePositionVector = obstacle.getPositionVector()
                .add(obstacle.getVelocity());
        Vector2D futurePositionVector = getPositionVector().add(getVelocity());
        Vector2D localOffset = futureObstaclePositionVector.subtract(futurePositionVector);

        float forwardComponent = localOffset.dot(new Vector2D(getVelocity()).normalize());

        // Obstacle is not at front
        if (forwardComponent <= 0) {
            return;
        }

        // Obstacle is far from here
        if (forwardComponent >= minDistanceToCenter) {
            return;
        }

        Vector2D forwardOffset = new Vector2D(getVelocity()).normalize().multiply(forwardComponent);
        Vector2D offForwardOffset = new Vector2D(localOffset).subtract(forwardOffset);

        // If it's in cylinder
        if (offForwardOffset.length() < totalRadius) {
            steeringForce.add(offForwardOffset.multiply(-1.0f));
        }
    }

    public void avoidObstacle(PointF obstaclePosition) {
        Vector2D offset = getPositionVector().subtract(obstaclePosition.vectorize());
        Vector2D avoidance = offset.perpendicularComponent(new Vector2D(getVelocity()).normalize());
        avoidance.normalize().multiply(maxSteeringForce);
        avoidance.add(new Vector2D(getVelocity()).normalize().multiply(maxSteeringForce*0.75f));
        steeringForce.add(avoidance);
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
        this.steeringForce.add(steeringForce);
    }

    private float maxSteeringForce;
    private Vector2D steeringForce;
}
