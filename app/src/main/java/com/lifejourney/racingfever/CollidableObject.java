package com.lifejourney.racingfever;

import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

public class CollidableObject extends MovableObject {

    static final String LOG_TAG = "CollidableObject";

    @SuppressWarnings("unchecked")
    public static class Builder<T extends CollidableObject.Builder<T>> extends MovableObject.Builder<T> {
        // optional parameter
        protected float mass = 1.0f;
        protected Shape shape = new Shape();

        public Builder(Point position) {
            super(position);
        }
        public T mass(float mass) {
            this.mass = mass;
            return (T)this;
        }
        public T shape(Shape shape) {
            this.shape = shape;
            return (T)this;
        }
        public CollidableObject build() {
            return new CollidableObject(this);
        }
    }

    protected CollidableObject(Builder builder) {
        super(builder);

        mass = builder.mass;
        shape = builder.shape;
    }

    @Override
    public void update() {
        super.update();

        // update shape before collision check
        shape.setPosition(new PointF(position));
        shape.setRotation(rotation);
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    private float mass;
    private Shape shape;

    public static boolean updateCollision(CollidableObject A, CollidableObject B) {
        // Check if collision occurs
        Vector2D mtv = checkCollision(A, B);

        if (mtv == null) {
            return false;
        }

        adjustToMtv(A, B, mtv);

        reactCollision(A, B);

        return true;
    }

    public static void reactCollision(CollidableObject A, CollidableObject B) {
        float aDirection = A.getDirection();
        float bDirection = B.getDirection();

        A.setDirection(bDirection);
        B.setDirection(aDirection);

        // TODO: Need some force calculation
    }

    public static void adjustToMtv(CollidableObject A, CollidableObject B, Vector2D mtv) {
        // Share MTV between A and B
        Vector2D aMtv = new Vector2D((float)((A.getPosition().x>B.getPosition().x)?Math.abs(mtv.x):-Math.abs(mtv.x)),
                (float)((A.getPosition().y>B.getPosition().y)?Math.abs(mtv.y):-Math.abs(mtv.y)));
        Vector2D bMtv = new Vector2D((float)((A.getPosition().x>B.getPosition().x)?-Math.abs(mtv.x):Math.abs(mtv.x)),
                (float)((A.getPosition().y>B.getPosition().y)?-Math.abs(mtv.y):Math.abs(mtv.y)));

        Vector2D aVelocityV = A.getVelocityVector();
        Vector2D bVelocityV = B.getVelocityVector();

        Vector2D aMtvNorm = new Vector2D(aMtv).normalize();
        Vector2D bMtvNorm = new Vector2D(bMtv).normalize();
        float aVelocityForMtv = Math.abs(aMtvNorm.dot(aVelocityV));
        float bVelocityForMtv = Math.abs(bMtvNorm.dot(bVelocityV));

        float aPotionMtv, bPotionMtv;
        if (aVelocityForMtv + bVelocityForMtv == 0.0f) {
            if (A.getVelocity() == 0.0f && B.getVelocity() > 0.0f) {
                aPotionMtv = 0.0f;
                bPotionMtv = 1.0f;
            }
            else if (A.getVelocity() > 0.0f && B.getVelocity() == 0.0f) {
                aPotionMtv = 1.0f;
                bPotionMtv = 0.0f;
            }
            else {
                aPotionMtv = 0.5f;
                bPotionMtv = 0.5f;
            }
        }
        else {
            aPotionMtv = aVelocityForMtv/(aVelocityForMtv+bVelocityForMtv);
            bPotionMtv = bVelocityForMtv/(aVelocityForMtv+bVelocityForMtv);
        }
        aMtv.multiply(aPotionMtv);
        bMtv.multiply(bPotionMtv);

        A.offset(new Point(new PointF(aMtv).expandToNextInt()));
        B.offset(new Point(new PointF(bMtv).expandToNextInt()));
    }

    public static Vector2D checkCollision(CollidableObject A, CollidableObject B) {
        // Check round area
        if (!checkRadius(A, B)) {
            return null;
        }

        // if both are circle, no need to go sat
        if (A.getShape().isCircle() && B.getShape().isCircle()) {
            return new Vector2D();
        }

        // check sat
        Vector2D mtv = checkSAT(A, B);
        if (mtv == null) {
            return null;
        }

        return mtv;
    }

    private static boolean checkRadius(CollidableObject A, CollidableObject B) {
        if (!A.getShape().isValid() || !B.getShape().isValid()) {
            return false;
        }

        Vector2D aCenter = A.getPosition().vectorize();
        Vector2D bCenter = B.getPosition().vectorize();

        if (aCenter.distanceSq(bCenter) >
                Math.pow(A.getShape().getRadius() + B.getShape().getRadius(), 2)) {
            return false;
        }

        return true;
    }

    private static Vector2D checkSAT(CollidableObject A, CollidableObject B) {
        // TODO: Add handling for circle vs polygon

        // Get Axes for testing
        ArrayList<Vector2D> axes = A.getShape().getAxes();
        axes.addAll(B.getShape().getAxes());

        // Project vertices to each axes and test if overlap
        Vector2D smallestAxis = null;
        float minOverlap = Float.MAX_VALUE;
        for (int i = 0; i < axes.size(); ++i) {
            Vector2D axis = axes.get(i);
            Projection p1 = A.getShape().projectToAxis(axis);
            Projection p2 = B.getShape().projectToAxis(axis);

            if (!p1.isOverlap(p2)) {
                return null;
            }
            else {
                float o = p1.getOverlap(p2);
                if (o < minOverlap) {
                    minOverlap = o;
                    smallestAxis = axis;
                }
            }
        }

        return new Vector2D(smallestAxis).multiply(minOverlap);
    }
}
