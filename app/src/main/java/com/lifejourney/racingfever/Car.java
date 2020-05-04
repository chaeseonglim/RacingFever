package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.Vector2D;

public class Car extends CollidableObject {

    private static final String LOG_TAG = "Car";

    enum Type {
        CAR1("CAR1");

        private static final String LOG_TAG = "Car.Type";
        private final String name;

        Type(String name) {
            this.name = name;
        }

        public float getScaleFactor() {
            switch (name) {
                case "CAR1":
                    return 3.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public Shape getShape() {
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
                }).subtract(new PointF(16, 16)).multiply(getScaleFactor());
            }
            else {
                Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                return null;
            }
        }

        public Sprite getSprite() {
            if (name == "CAR1") {
                Size spriteSize = new Size(32, 32).multiply(getScaleFactor());
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

        public float getEnginePower() {
            switch (name) {
                case "CAR1":
                    return 3.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getBrakePower() {
            switch (name) {
                case "CAR1":
                    return 3.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }

        public float getMaxSteeringAngle() {
            switch (name) {
                case "CAR1":
                    return 30.0f;
                default:
                    Log.e(LOG_TAG, "Unrecognized type for car!!! " + name);
                    return 1.0f;
            }
        }
    }

    // Builder for user
    public static class Builder {
        private PointF position;
        private Type type;

        public Builder(PointF position, Type type) {
            this.position = position;
            this.type = type;
        }

        public Car build() {
            return new PrivateBuilder<>(position, type)
                    .depth(1.0f).sprite(type.getSprite())
                    .friction(0.05f).inertia(type.getInertia())
                    .shape(type.getShape()).visible(true).build();
        }
    }

    // Builder provided by Engine2D
    @SuppressWarnings("unchecked")
    public static class PrivateBuilder<T extends Car.PrivateBuilder<T>> extends CollidableObject.Builder<T> {
        Type type;

        public PrivateBuilder(PointF position, Type type) {
            super(position);
            this.type = type;
        }
        public Car build() {
            return new Car(this);
        }
    }

    private Car(PrivateBuilder builder) {
        super(builder);
        type = builder.type;
        enginePower = type.getEnginePower();
        brakePower = type.getBrakePower();
        maxSteeringAngle = type.getMaxSteeringAngle();

    }

    @Override
    public void update() {
        super.update();

        // update rotation of car body using velocity sterring angle
        rotation = velocity.direction() + steeringAngle;
    }

    /**
     *
     * @param pedalPower pedal power percentage (0~1.0)
     * @param steeringAngle steering angle
     */
    public void accelerate(float pedalPower, float steeringAngle) {
        if (Math.abs(steeringAngle) > maxSteeringAngle) {
            steeringAngle = maxSteeringAngle * ((steeringAngle < 0.0f) ? -1 : 1);
        }

        addForce(new Vector2D(velocity.direction() + steeringAngle)
                .multiply(enginePower*pedalPower));
    }

    /**
     *
     * @param pedalPower pedal power percenage (0~1.0)
     */
    public void brake(float pedalPower) {

    }

    // spec
    private Type type;
    private float enginePower;
    private float brakePower;
    private float maxSteeringAngle;

    // state
    private float steeringAngle = 0.0f;
}
