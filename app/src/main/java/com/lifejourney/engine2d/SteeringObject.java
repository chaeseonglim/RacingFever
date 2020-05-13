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
            float distance = checkObstacleCanCollide(obstacle, maxDistance);
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

    public void avoidObstacle(CollidableObject obstacle, float maxDistance) {
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
                if (intendedSteeringPower > maxSteeringForce) {
                    intendedSteeringPower = maxSteeringForce;
                }

                offForwardOffset.normalize().multiply(intendedSteeringPower).multiply(-1);
                steeringForce.add(offForwardOffset);

                localOffsetLine.set(new PointF(getFuturePositionVector(nUpdate)), localOffset);
                localOffsetLine.commit();
                forwardOffsetLine.set(new PointF(getFuturePositionVector(nUpdate)), forwardOffset);
                forwardOffsetLine.commit();
                break;
            }
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
        this.steeringForce.add(steeringForce);
    }

    private float maxSteeringForce;
    private Vector2D steeringForce;

    // debugging
    protected Line localOffsetLine;
    protected Line forwardOffsetLine;
}
