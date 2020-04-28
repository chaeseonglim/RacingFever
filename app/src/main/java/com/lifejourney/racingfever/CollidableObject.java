package com.lifejourney.racingfever;

import android.util.Log;

import java.util.ArrayList;

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
    }

    public static boolean updateCollision(CollidableObject A, CollidableObject B) {
        Vector2D mtv = new Vector2D();

        // Check if collision occurs
        if (!checkCollision(A, B, mtv)) {
            return false;
        }


        Log.e(LOG_TAG, "Collision!!! " + mtv.x + " " + mtv.y);

        A.offset(new Point((int)mtv.x, (int)mtv.y));
        A.stop();

        return true;
    }

    public static boolean checkCollision(CollidableObject A, CollidableObject B, Vector2D mtv) {
        // Check round area
        if (!checkRadius(A, B)) {
            return false;
        }

        // If both are circle, no need to go SAT
        if (A.shape.isCircle() && B.shape.isCircle()) {
            return true;
        }

        // Check SAT
        if (!checkSAT(A, B, mtv)) {
            return false;
        }

        return true;
    }

    private static boolean checkRadius(CollidableObject A, CollidableObject B) {
        if (!A.shape.isValid() || !B.shape.isValid()) {
            return false;
        }

        Vector2D aShapeCenter = A.shape.getCentroid().vectorize().add(A.getPosition().vectorize());
        Vector2D bShapeCenter = B.shape.getCentroid().vectorize().add(B.getPosition().vectorize());

        if (aShapeCenter.distanceSq(bShapeCenter) >
                Math.pow(A.shape.getRadius() + B.shape.getRadius(), 2)) {
            return false;
        }

        return true;
    }

    private static ArrayList<Vector2D> getAxes(CollidableObject obj) {
        ArrayList<Vector2D> axes = new ArrayList<>();

        // FIXME: We can reduce axes if shape is certain types (such as rectangle)
        ArrayList<PointF> vertices = obj.shape.getVertices();
        for (int i = 0; i < vertices.size(); ++i) {
            Vector2D p1 = vertices.get(i).vectorize().add(obj.getPosition().vectorize());
            Vector2D p2 =
                    vertices.get((i+1==vertices.size())?0:i+1).vectorize().add(obj.getPosition().vectorize());
            Vector2D edge = p1.subtract(p2);
            Vector2D normal = edge.perpendicular().normalize();
            axes.add(normal);
        }

        return axes;
    }

    public static Projection projectToAxis(CollidableObject obj, Vector2D axis) {
        ArrayList<PointF> vertices = obj.shape.getVertices();
        float min = axis.dot(vertices.get(0).vectorize().add(obj.getPosition().vectorize()));
        float max = min;
        for (int i = 1; i < vertices.size(); ++i) {
            float p = axis.dot(vertices.get(i).vectorize().add(obj.getPosition().vectorize()));
            if (p < min) {
                min = p;
            }
            else if (p > max) {
                max = p;
            }
        }

        return new Projection(min, max);
    }

    private static boolean checkSAT(CollidableObject A, CollidableObject B, Vector2D mtv) {
        // Get Axes for testing
        ArrayList<Vector2D> axes = getAxes(A);
        axes.addAll(getAxes(B));

        // Project vertices to each axes and test if overlap
        Vector2D smallestAxis = null;
        float minOverlap = Float.MAX_VALUE;
        for (int i = 0; i < axes.size(); ++i) {
            Vector2D axis = axes.get(i);
            Projection p1 = projectToAxis(A, axis);
            Projection p2 = projectToAxis(B, axis);

            if (!p1.isOverlap(p2)) {
                return false;
            }
            else {
                float o = p1.getOverlap(p2);
                if (o < minOverlap) {
                    minOverlap = o;
                    smallestAxis = axis;
                }
            }
        }

        if (mtv != null) {
            mtv = new Vector2D(smallestAxis).multiply(minOverlap);
        }

        return true;
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
}
