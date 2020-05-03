package com.lifejourney.racingfever;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.MovableObject;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Vector2D;

public class Car extends CollidableObject {

    @SuppressWarnings("unchecked")
    private static class Builder<T extends Car.Builder<T>> extends CollidableObject.Builder<T> {
        // optional parameter

        public Builder(PointF position) {
            super(position);
        }
        public Car build() {
            return new Car(this);
        }
    }

    public Car(Builder builder) {
        super(builder);
    }
}
