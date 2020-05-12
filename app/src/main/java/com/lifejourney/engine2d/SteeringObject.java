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
    }

    public void seek(Object object, float weight) {
        seek(object.getPosition(), weight);
    }

    public void seek(PointF targetPosition, float weight) {
        Vector2D targetVector = targetPosition.vectorize().subtract(getPositionVector());
        Vector2D desiredVelocity =
                new Vector2D(targetVector).normalize().multiply(maxSteeringForce);
        steeringForce.add(desiredVelocity.subtract(getVelocity()).multiply(weight));
    }

    public void flee(Object object, float weight) {
        flee(object.getPosition(), weight);
    }

    public void flee(PointF targetPosition, float weight) {
        Vector2D targetVector = getPositionVector().subtract(targetPosition.vectorize());
        Vector2D desiredVelocity = new Vector2D(targetVector).normalize().multiply(getMaxVelocity());
        steeringForce.add(desiredVelocity.subtract(getVelocity()).multiply(weight));
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
            steeringForce.add(mean.divide(count).subtract(getPositionVector())
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
            steeringForce.add(mean.divide(count).subtract(getForwardVector())
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
            steeringForce.add(mean.divide(count).normalize()
                    .multiply(weight*maxSteeringForce));
        }
    }

    public boolean avoidObstacles(ArrayList<CollidableObject> obstacles, float maxDistance) {
        float nearestDistance = Float.MAX_VALUE;
        CollidableObject nearestObstacle = null;
        for (CollidableObject obstacle : obstacles) {
            float distance = checkObstacleIntersected(obstacle);
            if (distance < nearestDistance && distance <= maxDistance) {
                nearestDistance = distance;
                nearestObstacle = obstacle;
            }
        }

        if (nearestObstacle != null) {
            avoidObstacle(nearestObstacle);
            return true;
        }

        return false;
    }

    private float checkObstacleIntersected(CollidableObject obstacle) {
        float radius = getShape().getRadius();
        float obstacleRadius = obstacle.getShape().getRadius();
        float totalRadius = radius + obstacleRadius;

        float minDistanceToCollision = getVelocity().length() * getUpdatePeriod() * 5;

        Vector2D futureObstaclePositionVector = obstacle.getFuturePositionVector();
        Vector2D futurePositionVector = getFuturePositionVector();
        Vector2D localOffset = futureObstaclePositionVector.subtract(futurePositionVector);

        float forwardComponent = localOffset.dot(getForwardVector());

        // Obstacle is not at front
        if (forwardComponent <= 0) {
            return Float.MAX_VALUE;
        }

        // Obstacle is far from here
        if (forwardComponent >= minDistanceToCollision) {
            return Float.MAX_VALUE;
        }

        Vector2D forwardOffset = getForwardVector().multiply(forwardComponent);
        Vector2D offForwardOffset = new Vector2D(localOffset).subtract(forwardOffset);

        // If it's not in cylinder
        if (offForwardOffset.length() >= totalRadius) {
            return Float.MAX_VALUE;
        }

        return forwardComponent;
    }


    public void avoidObstacle(CollidableObject obstacle) {
        float radius = getShape().getRadius();
        float obstacleRadius = obstacle.getShape().getRadius();
        float totalRadius = radius + obstacleRadius;

        Vector2D futureObstaclePositionVector = obstacle.getFuturePositionVector();
        Vector2D futurePositionVector = getFuturePositionVector();
        Vector2D localOffset = futureObstaclePositionVector.subtract(futurePositionVector);

        float forwardComponent = localOffset.dot(getForwardVector());

        // Obstacle is not at front
        if (forwardComponent <= 0) {
            return;
        }

        Vector2D forwardOffset = getForwardVector().multiply(forwardComponent);
        Vector2D offForwardOffset = new Vector2D(localOffset).subtract(forwardOffset);

        // If it's in cylinder
        float offForwardOffsetlength = offForwardOffset.length();
        if (offForwardOffsetlength < totalRadius) {
            if (offForwardOffsetlength > maxSteeringForce) {
                offForwardOffset.normalize().multiply(maxSteeringForce);
            }
            steeringForce.add(offForwardOffset.multiply(-1.0f));

            localOffsetLine.set(new PointF(getFuturePositionVector()), localOffset);
            localOffsetLine.commit();
            forwardOffsetLine.set(new PointF(getFuturePositionVector()), forwardOffset);
            forwardOffsetLine.commit();
        }
    }

    public void avoidObstacle(PointF obstaclePosition) {
        Vector2D offset = getFuturePositionVector().subtract(obstaclePosition.vectorize());
        Vector2D avoidance = offset.perpendicularComponent(getForwardVector());
        avoidance.normalize().multiply(maxSteeringForce);
        avoidance.add(getForwardVector().multiply(maxSteeringForce*0.75f));
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

    // debugging
    private Line localOffsetLine;
    private Line forwardOffsetLine;
}
