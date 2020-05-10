package com.lifejourney.engine2d;

import android.util.Log;

public class MovableObject extends Object {

    private static final String LOG_TAG = "MovableObject";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends MovableObject.Builder<T>> extends Object.Builder<T> {
        // optional parameter
        protected Vector2D velocity = new Vector2D();
        protected float angularVelocity = 0.0f;
        protected float maxVelocity = Float.MAX_VALUE;
        protected float maxAngularVelocity = Float.MAX_VALUE;

        public Builder(PointF position) {
            super(position);
        }
        public T velocity(Vector2D velocity) {
            this.velocity = velocity;
            return (T)this;
        }
        public T angularVelocity(float angularVelocity) {
            this.angularVelocity = angularVelocity;
            return (T)this;
        }
        public T maxVelocity(float maxVelocity) {
            this.maxVelocity = maxVelocity;
            return (T)this;
        }
        public T maxAngularVelocity(float maxAngularVelocity) {
            this.maxVelocity = maxAngularVelocity;
            return (T)this;
        }
        public MovableObject build() {
            return new MovableObject(this);
        }
    }

    protected MovableObject(Builder builder) {
        super(builder);

        velocity = builder.velocity;
        angularVelocity = builder.angularVelocity;
        maxVelocity = builder.maxVelocity;
        maxAngularVelocity = builder.maxAngularVelocity;
    }

    @Override
    public void update() {
        // Apply max velocity
        if (maxVelocity != Float.MAX_VALUE && velocity.lengthSq() > Math.pow(maxVelocity, 2)) {
            velocity.normalize().multiply(maxVelocity);
        }
        if (angularVelocity > maxAngularVelocity) {
            angularVelocity = maxAngularVelocity;
        }

        // Update position & rotation
        getPosition().add(new PointF(velocity));
        setRotation(getRotation() + angularVelocity);

        super.update();
    }

    public void offset(PointF alpha) {
        getPosition().offset(alpha);
    }

    public void stopMoving() {
        velocity.reset();
    }

    public void stopRotating() {
        angularVelocity = 0.0f;
    }

    public void stop() {
        stopMoving();
        stopRotating();
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public float getMaxAngularVelocity() {
        return maxAngularVelocity;
    }

    public void setMaxAngularVelocity(float maxAngularVelocity) {
        this.maxAngularVelocity = maxAngularVelocity;
    }

    private Vector2D velocity;
    private float angularVelocity;
    private float maxVelocity;
    private float maxAngularVelocity;
}
