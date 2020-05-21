package com.lifejourney.engine2d;

import android.util.Log;

public class CollidableObject extends Object {

    static final String LOG_TAG = "CollidableObject";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends CollidableObject.Builder<T>>
            extends Object.Builder<T> {
        // optional parameter
        protected Shape shape = new Shape();
        protected Vector2D force = new Vector2D();
        protected float torque = 0.0f;
        protected float maxForce = Float.MAX_VALUE;
        protected float maxTorque = Float.MAX_VALUE;
        protected float mass = 1.0f;
        protected float inertia = 1.0f;
        protected float friction = 0.0f;
        protected float restitution = 0.5f;
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
        public T maxForce(float maxForce) {
            this.maxForce = maxForce;
            return (T)this;
        }
        public T maxTorque(float maxTorque) {
            this.maxTorque = maxTorque;
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

        velocity = builder.velocity;
        angularVelocity = builder.angularVelocity;
        maxVelocity = builder.maxVelocity;
        maxAngularVelocity = builder.maxAngularVelocity;

        shape = builder.shape;
        force = builder.force;
        torque = builder.torque;
        maxForce = builder.maxForce;
        maxTorque = builder.maxTorque;
        mass = builder.mass;
        invMass = 1.0f / mass;
        inertia = builder.inertia;
        invInertia = 1.0f / inertia;
        friction = builder.friction;
        restitution = builder.restitution;

        // debugging
        lineVelicity = new Line.Builder(getPosition(),
                new PointF(getPositionVector().add(getVelocity())))
                .color(1.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
        circleShape =
                new Circle.Builder(shape.getPosition(), shape.getRadius())
                        .color(1.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
    }

    @Override
    public void update() {
        // Caculate next velocity
        setVelocity(estimateFutureVelocityUsingForce(1, force));
        setAngularVelocity(estimageFutureAngularVelocityUsingTorque(1, torque));

        // Update position & rotatio
        getPosition().add(new PointF(velocity));
        setRotation(getRotation() + angularVelocity);

        super.update();

        if (isUpdatePossible()) {
            force.reset();
            torque = 0.0f;
        }

        // Update shape before collision check
        shape.setPosition(new PointF(getPosition()));
        shape.setRotation(getRotation());
    }

    @Override
    public void commit() {
        super.commit();

        // debugging
        lineVelicity.set(getPosition(), new PointF(getFuturePositionVector(getUpdatePeriod())));
        lineVelicity.commit();
        circleShape.set(shape.getPosition(), shape.getRadius());
        circleShape.commit();
    }

    protected Vector2D estimateFutureVelocityUsingForce(int numberOfUpdate, Vector2D force) {
        Vector2D acceleration = force.clone().truncate(maxForce).multiply(invMass)
                .divide(getUpdatePeriod());
        Vector2D velocity = getVelocity().clone();

        for (int nUpdate = 0; nUpdate < numberOfUpdate; ++nUpdate) {
            velocity.multiply(1.0f - friction);
            if (nUpdate < getUpdatePeriod()) {
                velocity.add(acceleration);
            }
        }

        return velocity;
    }

    protected float estimageFutureAngularVelocityUsingTorque(int numberOfUpdate, float torque) {
        torque = Math.min(maxTorque, torque);
        float angularAcceleration = torque * invInertia / getUpdatePeriod();
        float angularVelocity = getAngularVelocity();

        for (int nUpdate = 0; nUpdate < numberOfUpdate; ++nUpdate) {
            angularVelocity *= 1.0f - friction;
            if (nUpdate < getUpdatePeriod()) {
                angularVelocity += angularAcceleration;
            }
        }

        return angularVelocity;
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

    public Vector2D getFuturePositionVector(int numberOfUpdate) {
        Vector2D position = getPositionVector();
        Vector2D acceleration = force.clone().multiply(invMass).divide(getUpdatePeriod());
        Vector2D velocity = getVelocity().clone();

        for (int nUpdate = 0; nUpdate < numberOfUpdate; ++nUpdate) {
            if (nUpdate < getUpdatePeriod()) {
                velocity.multiply(1.0f - friction);
                velocity.add(acceleration);
            }

            position.add(velocity);
        }

        return position;
    }

    public Vector2D getVirtualPositionVector(float direction, int numberOfUpdate) {
        Vector2D position = getPositionVector();
        Vector2D virtualVelocity = new Vector2D(direction).multiply(getVelocity().length());

        for (int nUpdate = 0; nUpdate < numberOfUpdate; ++nUpdate) {
            //virtualVelocity.multiply(1.0f - friction);
            position.add(virtualVelocity);
        }

        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public Vector2D getForwardVector() {
        return velocity.clone().normalize();
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

    public void addAdjustedForce(Vector2D force, float maxForce) {
        this.force.add(force.multiply(getMass()).truncate(maxForce));
    }

    public boolean isCollisionChecked() {
        return collisionChecked;
    }

    public void setCollistionChecked(boolean collisionChecked) {
        this.collisionChecked = collisionChecked;
    }

    public void onCollisionOccurred(CollidableObject targetObject) {
        // To be implemented by an user
    }

    public void setCollisionEnabled(boolean collisionEnabled) {
        this.collisionEnabled = collisionEnabled;
    }

    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }

    private Vector2D velocity;
    private float angularVelocity;
    private float maxVelocity;
    private float maxAngularVelocity;

    private Shape shape;
    private Vector2D force;
    private float torque;
    private float maxForce;
    private float maxTorque;
    private float mass;
    private float inertia;
    private float friction;
    private float restitution;

    private float invMass;
    private float invInertia;
    private boolean collisionChecked = false;
    private boolean collisionEnabled = true;

    // debugging
    private Line lineVelicity;
    public Circle circleShape;
}
