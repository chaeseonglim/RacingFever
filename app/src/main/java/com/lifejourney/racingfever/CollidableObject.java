package com.lifejourney.racingfever;

import android.graphics.Rect;

public class CollidableObject extends MovableObject {

    @SuppressWarnings("unchecked")
    public static class Builder<T extends CollidableObject.Builder<T>> extends MovableObject.Builder<T> {
        // optional parameter
        protected float mass = 1.0f;
        protected float collidableRadius = 0.0f;
        protected Rect collidableArea = new Rect(0, 0, size.width, size.height);

        public Builder(Point position) {
            super(position);
        }
        public T mass(float mass) {
            this.mass = mass;
            return (T)this;
        }
        public T collidableRadius(float collidableRadius) {
            this.collidableRadius = collidableRadius;
            return (T)this;
        }
        public T collidableArea(Rect collidableRegion) {
            this.collidableArea = collidableRegion;
            return (T)this;
        }
        public CollidableObject build() {
            return new CollidableObject(this);
        }
    }

    protected CollidableObject(Builder builder) {
        super(builder);

        mass = builder.mass;
        collidableRadius = builder.collidableRadius;
        collidableArea = builder.collidableArea;
    }

    @Override
    public void update() {
        super.update();
    }

    public static boolean updateCollision(CollidableObject A, CollidableObject B) {
        // Check if collision occurs
        if (!checkCollision(A, B)) {
            return false;
        }

        // Collision should be checked before updating the coordination of object
        Vector2D aPositionVector = A.getPositionVector();
        Vector2D bPositionVector = B.getPositionVector();
        Vector2D aCollidableRegionCenter = A.getCollidableAreaCenter().add(aPositionVector);
        Vector2D bCollidableRegionCenter = A.getCollidableAreaCenter().add(bPositionVector);

        // Let's do some physics...
        Vector2D distance = aCollidableRegionCenter.subtract(bCollidableRegionCenter);
        float r = distance.lengthSq();
        if (r == 0.0f)
            r = 0.1f;

        final float K = 1000.0f;
        float f = (K * A.mass * B.mass) / r;
        Vector2D force = new Vector2D(distance).multiply(f).divide(r);
        Vector2D aForce = new Vector2D(force).divide(A.mass).multiply(-1);
        Vector2D bForce = new Vector2D(force).divide(B.mass);

        Vector2D aVelocity = A.getVelocityVector().add(aForce);
        Vector2D bVelocity = B.getVelocityVector().add(bForce);
        A.setDirection(aVelocity);
        B.setDirection(bVelocity);
        A.acceleration = 0.0f;
        B.acceleration = 0.0f;
        A.velocity = 0; //aVelocity.length();
        B.velocity = bVelocity.length();

        return true;
    }

    public static boolean checkCollision(CollidableObject A, CollidableObject B) {
        // Check round area
        if (!checkRadius(A, B)) {
            return false;
        }

        // Check OBB
        if (!checkOBB(A, B)) {
            return false;
        }

        return true;
    }

    private static boolean checkRadius(CollidableObject A, CollidableObject B) {
        // radius is invalid if it's 0.0f, so it's always collidable
        if (A.collidableRadius == 0.0f || B.collidableRadius == 0.0f) {
            return true;
        }

        Vector2D aPositionVector = A.getPositionVector();
        Vector2D bPositionVector = B.getPositionVector();
        Vector2D aCollidableRegionCenter = A.getCollidableAreaCenter().add(aPositionVector);
        Vector2D bCollidableRegionCenter = A.getCollidableAreaCenter().add(bPositionVector);

        if (aCollidableRegionCenter.distanceSq(bCollidableRegionCenter) >
                Math.pow(A.collidableRadius + B.collidableRadius, 2)) {
            return false;
        }

        return true;
    }

    private static boolean checkOBB(CollidableObject A, CollidableObject B) {
        float aRotationRadian = (float) (Math.PI / 180 * A.rotation);
        float bRotationRadian = (float) (Math.PI / 180 * B.rotation);

        Vector2D aPositionVector = A.getPositionVector();
        Vector2D bPositionVector = B.getPositionVector();

        // Create distance vector
        Vector2D aCollidableRegionCenter = A.getCollidableAreaCenter().add(aPositionVector);
        Vector2D bCollidableRegionCenter = A.getCollidableAreaCenter().add(bPositionVector);
        Vector2D distance = aCollidableRegionCenter.subtract(bCollidableRegionCenter);

        // Create axis vectors
        Vector2D aDir1 = new Vector2D((float)Math.cos(aRotationRadian),
                (float)Math.sin(aRotationRadian));
        Vector2D aDir2 = new Vector2D((float)Math.cos(aRotationRadian - Math.PI * 0.5f),
                (float)Math.sin(aRotationRadian - Math.PI * 0.5f));
        Vector2D bDir1 = new Vector2D((float)Math.cos(bRotationRadian),
                (float)Math.sin(bRotationRadian));
        Vector2D bDir2 = new Vector2D((float)Math.cos(bRotationRadian - Math.PI * 0.5f),
                (float)Math.sin(bRotationRadian - Math.PI * 0.5f));

        // Create length vector
        Vector2D aLen1 = new Vector2D(aDir1).multiply(A.collidableArea.width()*0.5f);
        Vector2D aLen2 = new Vector2D(aDir2).multiply(A.collidableArea.height()*0.5f);
        Vector2D bLen1 = new Vector2D(bDir1).multiply(B.collidableArea.width()*0.5f);
        Vector2D bLen2 = new Vector2D(bDir2).multiply(B.collidableArea.height()*0.5f);

        // Check aDir1
        {
            float aLength = aLen1.length();
            float bLength = Math.abs(new Vector2D(aDir1).dot(bLen1)) +
                    Math.abs(new Vector2D(aDir1).dot(bLen2));
            float length = Math.abs(aDir1.dot(distance));
            if (length > aLength + bLength)
                return false;
        }

        // Check aDir2
        {
            float aLength = aLen2.length();
            float bLength = Math.abs(new Vector2D(aDir2).dot(bLen1)) +
                    Math.abs(new Vector2D(aDir2).dot(bLen2));
            float length = Math.abs(aDir2.dot(distance));
            if (length > aLength + bLength)
                return false;
        }

        // Check bDir1
        {
            float aLength = bLen1.length();
            float bLength = Math.abs(new Vector2D(bDir1).dot(aLen1)) +
                    Math.abs(new Vector2D(bDir1).dot(aLen2));
            float length = Math.abs(bDir1.dot(distance));
            if (length > aLength + bLength)
                return false;
        }

        // Check bDir2
        {
            float aLength = bLen1.length();
            float bLength = Math.abs(new Vector2D(bDir2).dot(aLen1)) +
                    Math.abs(new Vector2D(bDir2).dot(aLen2));
            float length = Math.abs(bDir2.dot(distance));
            if (length > aLength + bLength)
                return false;
        }

        return true;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getCollidableRadius() {
        return collidableRadius;
    }

    public void setCollidableRadius(float collidableRadius) {
        this.collidableRadius = collidableRadius;
    }

    public Vector2D getCollidableAreaCenter() {
        return new Vector2D(collidableArea.exactCenterX(), collidableArea.exactCenterY());
    }

    private float mass;
    private float collidableRadius;
    private Rect collidableArea;
}
