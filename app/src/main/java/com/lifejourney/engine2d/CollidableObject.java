package com.lifejourney.engine2d;

import android.util.Log;

public class CollidableObject extends MovableObject {

    static final String LOG_TAG = "CollidableObject";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends CollidableObject.Builder<T>> extends MovableObject.Builder<T> {
        // optional parameter
        protected Shape shape = new Shape();
        protected Vector2D force = new Vector2D();
        protected float torque = 0.0f;
        protected float mass = 1.0f;
        protected float inertia = 1.0f;
        protected float friction = 0.0f;
        protected float restitution = 0.5f;

        public Builder(PointF position) {
            super(position);
        }
        public T shape(Shape shape) {
            this.shape = shape;
            return (T)this;
        }
        public T force(Vector2D force) {
            this.force = force;
            return (T)this;
        }
        public T torque(float torque) {
            this.torque = torque;
            return (T)this;
        }
        public T mass(float mass) {
            this.mass = mass;
            return (T)this;
        }
        public T inertia(float inertia) {
            this.inertia = inertia;
            return (T)this;
        }
        public T friction(float friction) {
            this.friction = friction;
            return (T)this;
        }
        public T restitution(float restitution) {
            this.restitution = restitution;
            return (T)this;
        }
        public CollidableObject build() {
            return new CollidableObject(this);
        }
    }

    protected CollidableObject(Builder builder) {
        super(builder);

        shape = builder.shape;
        force = builder.force;
        torque = builder.torque;
        setMass(builder.mass);
        setInertia(builder.inertia);
        friction = builder.friction;
        restitution = builder.restitution;
    }

    @Override
    public void update() {
        // Caculate frictional forces
        Vector2D frictionalForce = new Vector2D(velocity).multiply(-1.0f * friction);
        float fricionalAngularForce = angularVelocity * -1.0f * friction;

        // Force determines the acceleration
        angularAcceleration += torque * invInertia + fricionalAngularForce;
        acceleration.add(force.multiply(invMass)).add(frictionalForce);

        // Update object's velocity, position
        super.update();

        // force will be valid only for single update duration
        acceleration.reset();
        force.reset();
        angularAcceleration = 0.0f;
        torque = 0.0f;

        // Update shape before collision check
        shape.setPosition(new PointF(position));
        shape.setRotation(rotation);
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Vector2D getForce() {
        return force;
    }

    public void setForce(Vector2D force) {
        this.force = force;
    }

    public float getTorque() {
        return torque;
    }

    public void setTorque(float torque) {
        this.torque = torque;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
        if (mass == 0.0f)
            invMass = 0.0f;
        else
            invMass = 1.0f / mass;
    }

    public float getInvMass() {
        return invMass;
    }

    public float getInertia() {
        return inertia;
    }

    public void setInertia(float inertia) {
        this.inertia = inertia;
        if (inertia == 0.0f)
            invInertia = 0.0f;
        else
            invInertia = 1.0f / inertia;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }

    public void addForce(Vector2D force) {
        this.force.add(force);
    }

    public void addForce(Vector2D force, Vector2D pos) {
        addForce(force);
        addTorque(pos.cross(force));
    }

    public void addTorque(float torque) {
        this.torque += torque;
    }

    public boolean isCollisionChecked() {
        return collisionChecked;
    }

    public void setCollistionChecked(boolean collisionChecked) {
        this.collisionChecked = collisionChecked;
    }

    public void onCollisionOccured(CollidableObject targetObject) {
        // To be implemented by an user
    }

    private Shape shape;
    private Vector2D force;
    private float torque;
    private float mass;
    private float inertia;
    private float friction;
    private float restitution;

    private float invMass;
    private float invInertia;
    private boolean collisionChecked = false;
}
