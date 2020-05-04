package com.lifejourney.engine2d;

import android.util.Log;

public class MovableObject extends Object {

    private static final String LOG_TAG = "MovableObject";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends MovableObject.Builder<T>> extends Object.Builder<T> {
        // optional parameter
        protected Vector2D velocity = new Vector2D();
        protected Vector2D acceleration = new Vector2D();
        protected float angularVelocity = 0.0f;
        protected float angularAcceleration = 0.0f;
        protected float maxVelocity = Float.MAX_VALUE;
        protected float maxAngularVelocity = Float.MAX_VALUE;

        public Builder(PointF position) {
            super(position);
        }
        public T velocity(Vector2D velocity) {
            this.velocity = velocity;
            return (T)this;
        }
        public T acceleration(Vector2D acceleration) {
            this.acceleration = acceleration;
            return (T)this;
        }
        public T angularVelocity(float angularVelocity) {
            this.angularVelocity = angularVelocity;
            return (T)this;
        }
        public T angularAcceleration(float angularAcceleration) {
            this.angularAcceleration = angularAcceleration;
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
        acceleration = builder.acceleration;
        angularVelocity = builder.angularVelocity;
        angularAcceleration = builder.angularAcceleration;
        maxVelocity = builder.maxVelocity;
        maxAngularVelocity = builder.maxAngularVelocity;
    }

    @Override
    public void update() {
        // Update velocity
        velocity.add(acceleration);
        angularVelocity += angularAcceleration;

        // Apply max velocity
        if (maxVelocity != Float.MAX_VALUE && velocity.lengthSq() > Math.pow(maxVelocity, 2)) {
            velocity.normalize().multiply(maxVelocity);
        }
        if (angularVelocity > maxAngularVelocity) {
            angularVelocity = maxAngularVelocity;
        }

        // Update position
        position.add(new PointF(velocity));
        rotation += angularVelocity;
    }

    public void offset(PointF alpha) {
        position.offset(alpha);
    }

    public void stopMove() {
        velocity.reset();
        acceleration.reset();
    }

    public void stopRotate() {
        angularVelocity = 0.0f;
        angularAcceleration = 0.0f;
    }

    public void stop() {
        stopMove();
        stopRotate();
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    public Vector2D getPositionVector() {
        return new Vector2D(position.x, position.y);
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getAngularAcceleration() {
        return angularAcceleration;
    }

    public void setAngularAcceleration(float angularAcceleration) {
        this.angularAcceleration = angularAcceleration;
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

    protected Vector2D velocity;
    protected Vector2D acceleration;
    protected float angularVelocity;
    protected float angularAcceleration;
    protected float maxVelocity;
    protected float maxAngularVelocity;
}
