package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.SteeringObject;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;

public class Car extends SteeringObject {

    private static final String LOG_TAG = "Car";

    enum Type {
        CAR1("CAR1");

        private static final String LOG_TAG = "Car.Type";
        private final String name;

        Type(String name) {
            this.name = name;
        }

        public Shape getShape(float scale) {
            if (name == "CAR1") {
                return new Shape(new PointF[]{
                        new PointF(5, 13),
                        new PointF(2, 18),
                        new PointF(2, 23),
                        new PointF(23, 23),
                        new PointF(29, 15),
                        new PointF(29, 13),
                        new PointF(23, 8),
                        new PointF(15, 8),
                        new PointF(8, 13)
                }).subtract(new PointF(16, 16)).multiply(scale);
            }
            else {
                Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                return null;
            }
        }

        public Sprite getSprite(float scale) {
            if (name == "CAR1") {
                Size spriteSize = new Size(32, 32).multiply(scale);
                return new Sprite.Builder("car1.png").size(spriteSize).build();
            }
            else {
                Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                return null;
            }
        }

        public float getInertia() {
            switch (name) {
                case "CAR1":
                    return 10.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getMass() {
            switch (name) {
                case "CAR1":
                    return 1.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getEnginePower() {
            switch (name) {
                case "CAR1":
                    return 20.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getMaxVelocity() {
            switch (name) {
                case "CAR1":
                    return 100.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }
    }

    /**
     * Builder for user
     */
    public static class Builder {
        private PointF position;
        private Type type;

        private float headDirection = 0.0f;
        private float scale = 1.0f;

        public Builder(PointF position, Type type) {
            this.position = position;
            this.type = type;
        }
        public Builder headDirection(float headDirection) {
            this.headDirection = headDirection;
            return this;
        }
        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }
        public Car build() {
            return new PrivateBuilder<>(position, type)
                    .depth(1.0f).friction(0.03f).inertia(type.getInertia()).mass(type.getMass())
                    .headDirection(headDirection).maxVelocity(type.getMaxVelocity())
                    .maxSteeringForce(type.getEnginePower())
                    .sprite(type.getSprite(scale)).shape(type.getShape(scale))
                    .visible(true).build();
        }
    }

    /**
     * Builder inherited from Engine2D
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static class PrivateBuilder<T extends Car.PrivateBuilder<T>>
            extends SteeringObject.Builder<T> {
        Type type;
        float headDirection = 0.0f;

        public PrivateBuilder(PointF position, Type type) {
            super(position);
            this.type = type;
        }
        public T headDirection(float headDirection) {
            this.headDirection = headDirection;
            return (T) this;
        }
        public Car build() {
            return new Car(this);
        }
    }

    private Car(PrivateBuilder builder) {
        super(builder);
        type = builder.type;
        headDirection = builder.headDirection;
        setRotation(headDirection);
    }

    @Override
    public void update() {
        if (!isUpdatePossible() || collisionResolveLeft > 0) {
            setSteeringForce(new Vector2D());
        }

        super.update();

        if (collisionResolveLeft == 0) {
            headDirection = getVelocity().direction();
            setRotation((headDirection + 90.0f) % 360.0f);
        }
        else {
            headDirection = (getRotation() - 90.0f) % 360.0f;
        }

        if (collisionResolveLeft > 0) {
            collisionResolveLeft--;
        }
    }

    public float getHeadDirection() {
        return headDirection;
    }

    @Override
    public void onCollisionOccured(CollidableObject targetObject) {
        collisionResolveLeft = COLLISION_RESOLVE_PERIOD;
    }

    // spec
    private Type type;
    private Vector2D steeringForce;

    // state
    private final int COLLISION_RESOLVE_PERIOD = 30;
    private int collisionResolveLeft = 0;

    private float headDirection;
}
