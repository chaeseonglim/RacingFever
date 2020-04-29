package com.lifejourney.racingfever;

public class MovableObject extends Object {

    @SuppressWarnings("unchecked")
    public static class Builder<T extends MovableObject.Builder<T>> extends Object.Builder<T> {
        // optional parameter
        protected float velocity = 0.0f;
        protected float acceleration = 0.0f;
        protected float friction = 0.0f;
        protected float direction = 0.0f;    /* degree from (0, 1) */

        public Builder(Point position) {
            super(position);
        }
        public T velocity(float velocity) {
            this.velocity = velocity;
            return (T)this;
        }
        public T acceleration(float acceleration) {
            this.acceleration = acceleration;
            return (T)this;
        }
        public T friction(float friction) {
            // Assumes friction is always positive
            this.friction = friction;
            return (T)this;
        }
        public T direction(Float direction) {
            this.direction = direction;
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
        friction = builder.friction;
        direction = builder.direction;
    }

    @Override
    public void update() {
        // Apply direction and velocity to position
        if (velocity != 0.0f) {
            Vector2D positionV = getVelocityVector().add(position.vectorize());
            position.setTo((int)positionV.x, (int)positionV.y);
        }

        // Update velocity and acceleration
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
        this.friction = friction;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public void setDirection(Vector2D direction) {
        this.direction = direction.angle();
    }

    public void rotateDirection(float delta) {
        setDirection(direction + delta);
    }

    public Vector2D getDirectionVector() {
        return new Vector2D(0, -1).rotate(direction);
    }

    public Vector2D getVelocityVector() {
        return getDirectionVector().multiply(velocity);
    }

    public void offset(Point alpha) {
        position.offset(alpha);
    }

    public void stop() {
        velocity = 0.0f;
        acceleration = 0.0f;
    }

    protected float velocity;
    protected float acceleration;
    protected float friction;
    protected float direction;    /* degree from (0, 1) */
}
