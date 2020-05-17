package com.lifejourney.engine2d;

import java.util.ArrayList;

public class SteeringObject extends CollidableObject {
    static final String LOG_TAG = "SteeringObject";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends SteeringObject.Builder<T>>
            extends CollidableObject.Builder<T> {
        // optional parameter
        private float maxSteeringForce = 5.0f;

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
        /*
        // Set steeringForce limit to maxSteeringForce
        if (steeringForce.lengthSq() > Math.pow(maxSteeringForce, 2)) {
            steeringForce.normalize().multiply(maxSteeringForce);
        }
         */

        // Apply mass for making steering force affect as it intend to
        steeringForce.multiply(getMass());

        // Reduce velocity and steering power if steering angle is stiff
        float steeringAngle = Math.abs(getVirtualVelocityByForce(steeringForce).angle(getVelocity()));
        if (steeringAngle > 90.0f) {
            steeringForce.multiply(0.4f);
            //getVelocity().multiply(0.6f);
        }
        else if (steeringAngle > 60.0f) {
            steeringForce.multiply(0.6f);
            //getVelocity().multiply(0.7f);
        }
        else if (steeringAngle > 30.0f) {
            steeringForce.multiply(0.7f);
            //getVelocity().multiply(0.8f);
        }
    }

    public void seek(Object object, float weight) {
        seek(object.getPosition(), weight);
    }

    public void seek(PointF targetPosition, float weight) {
        Vector2D targetVector = targetPosition.vectorize().subtract(getPositionVector());
        Vector2D desiredVelocity =
                targetVector.clone().normalize().multiply(getMaxVelocity())
                        .subtract((getVelocity())).truncate(maxSteeringForce).multiply(weight);
        addSteeringForce(desiredVelocity);
    }

    public void flee(Object object, float weight) {
        flee(object.getPosition(), weight);
    }

    public void flee(PointF targetPosition, float weight) {
        Vector2D targetVector = getPositionVector().subtract(targetPosition.vectorize());
        Vector2D desiredVelocity =
                targetVector.clone().normalize().multiply(getMaxVelocity())
                        .subtract((getVelocity())).truncate(maxSteeringForce).multiply(weight);
        addSteeringForce(desiredVelocity);
    }

    public void brake(float weight) {
        // In case of brake, it's not an steering power so reduce velocity directly
        getVelocity().multiply(1.0f - weight);
    }

    public void brake(MovableObject cautiousObject, float gapWeight, float minWeight, float maxWeight) {
        float objectVelocity = cautiousObject.getVelocity().dot(getForwardVector());
        float myVelocity = getVelocity().length();
        float targetVelocity = Math.min(objectVelocity * gapWeight, myVelocity*(1.0f-minWeight));
        targetVelocity = Math.max(targetVelocity, myVelocity*(1.0f-maxWeight));
        getVelocity().normalize().multiply(targetVelocity);
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
                    .normalize().multiply(weight).multiply(maxSteeringForce));
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
                    .multiply(weight).multiply(maxSteeringForce));
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
            addSteeringForce(mean.divide(count).normalize().multiply(weight)
                    .multiply(maxSteeringForce));
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

    @Override
    public Vector2D getFuturePositionVector(int numberOfUpdate) {
        Vector2D futureSteeringForce = steeringForce.clone();
        if (numberOfUpdate < getUpdatePeriod()) {
            futureSteeringForce.multiply(numberOfUpdate).divide(getUpdatePeriod());
        }
        return getPositionVector().add(getVirtualVelocityByForce(futureSteeringForce)
                .multiply(numberOfUpdate));
    }

    protected Vector2D getVirtualVelocityByForce(Vector2D virtualForce) {
        return getVelocity().clone().add(virtualForce.clone().multiply(getInvMass()));
    }

    private float maxSteeringForce;
    private Vector2D steeringForce;

    // debugging
    protected Line localOffsetLine;
    protected Line forwardOffsetLine;
}
