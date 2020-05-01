package com.lifejourney.racingfever;

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
    }

    @Override
    public void update() {
        // Update velocity
        velocity.add(acceleration);
        angularVelocity += angularAcceleration;

        // Update position
        position.add(new PointF(velocity));
        rotation += angularVelocity;
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

    protected Vector2D velocity;
    protected Vector2D acceleration;
    protected float angularVelocity;
    protected float angularAcceleration;
}
