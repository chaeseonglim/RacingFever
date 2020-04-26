package com.lifejourney.racingfever;

import android.graphics.PointF;
import android.graphics.Rect;

import java.util.Vector;

public class MovableObject extends Object {

    public static class Builder extends Object.Builder<Builder> {

        // optional parameter
        private float velocity = 0.0f;
        private float acceleration = 0.0f;
        private float friction = 0.0f;
        private float direction = 0.0f;    /* degree from (0, 1) */

        public Builder(Rect region) {
            super(region);
        }
        public Builder velocity(float velocity) {
            this.velocity = velocity;
            return this;
        }
        public Builder acceleration(float acceleration) {
            this.acceleration = acceleration;
            return this;
        }
        public Builder friction(float friction) {
            this.friction = friction;
            return this;
        }
        public Builder direction(Float direction) {
            this.direction = direction;
            return this;
        }
        public MovableObject build() {
            return new MovableObject(this);
        }
    }

    private MovableObject(Builder builder) {
        super(builder);

        velocity = builder.velocity;
        acceleration = builder.acceleration;
        friction = Math.abs(builder.friction);
        direction = builder.direction;
    }

    @Override
    public void update() {
        if (velocity != 0.0f) {
            // Apply velocity and direction to position
            Vector2D forwardV = new Vector2D(0, -1);
            forwardV.rotate(direction).multiply(velocity).add(new Vector2D(region.left, region.top));
            region.offsetTo((int) forwardV.x, (int) forwardV.y);
        }

        // Apply acceleration and velocity
        if (((velocity > 0.0f && acceleration < 0.0f) ||
                (velocity < 0.0f && acceleration > 0.0f))
            && (Math.abs(velocity) < Math.abs(acceleration))) {
            velocity = 0.0f;
            acceleration = 0.0f;
        }
        else {
            velocity += acceleration;
            // friction is always against acceleration
            if (velocity > 0.0f) {
                acceleration -= friction;
            }
            else if (velocity < 0.0f) {
                acceleration += friction;
            }
        }
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        // Assumes friction is always positive
        this.friction = Math.abs(friction);
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    private float velocity;
    private float acceleration;
    private float friction;
    private float direction;    /* degree from (0, 1) */
}
