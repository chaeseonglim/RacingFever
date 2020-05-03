package com.lifejourney.engine2d;

import java.util.ArrayList;

public class CollisionDetector {

    private static final String LOG_TAG = "CollisionDetector";

    public CollisionDetector() {
    }

    private class Manifold {
        public Vector2D normal;
        public float penetration;

        public Manifold(Vector2D normal, float penetration) {
            this.normal = normal;
            this.penetration = penetration;
        }
    }

    public boolean updateCollision(CollidableObject A, CollidableObject B) {
        // Check if collision occurs
        Manifold manifold = checkCollision(A, B);
        if (manifold == null) {
            return false;
        }

        // Post collision handling
        correctPosition(A, B, manifold);
        resolveImpulse(A, B, manifold);

        // Call event handlers of each objects
        A.onCollisionOccured(B);
        B.onCollisionOccured(A);

        return true;
    }

    private void resolveImpulse(CollidableObject A, CollidableObject B, Manifold manifold) {
        Vector2D normal = manifold.normal;

        // Calculate relative velocity
        Vector2D rv = new Vector2D(B.getVelocity()).subtract(A.getVelocity());

        // Calculate relative velocity in terms of the normal direction
        float vecAlongNormal = rv.dot(normal);

        // Do not resolve if velocities are separating
        if (vecAlongNormal > 0)
            return;

        // Calculate restitution
        float e = Math.min(A.getRestitution(), B.getRestitution());

        // Calculate impluse scalar
        float j = -(1 + e) * vecAlongNormal;

        float invMassA = A.getInvMass();
        float invMassB = B.getInvMass();
        j /= invMassA + invMassB;

        // Apply impulse
        Vector2D impulse = new Vector2D(normal).multiply(j);
        Vector2D massImpulseA = new Vector2D(impulse).multiply(invMassA).multiply(-1);
        Vector2D massImpulseB = new Vector2D(impulse).multiply(invMassB);

        if (A.getShape().isCircle() && B.getShape().isCircle()) {
            // Find contact point (optional)
            Vector2D pos = new Vector2D(A.getPositionVector()).subtract(B.getPositionVector());
            pos.normalize().multiply(A.getShape().getRadius()).add(A.getPositionVector());

            A.addForce(massImpulseA, pos);
            B.addForce(massImpulseB, pos);
        }
        else {
            // NOTE: Someday we can try to find contact point between polygons..
            A.addForce(massImpulseA);
            B.addForce(massImpulseB);
        }
    }

    private void correctPosition(CollidableObject A, CollidableObject B, Manifold manifold) {
        // Calculate MTV
        Vector2D mtv = new Vector2D(manifold.normal).multiply(manifold.penetration);

        // Decide MTV direction of object A and B (it's a bit vague...)
        Vector2D mtvA =
            new Vector2D((float)((A.getPosition().x>B.getPosition().x)?Math.abs(mtv.x):-Math.abs(mtv.x)),
                    (float)((A.getPosition().y>B.getPosition().y)?Math.abs(mtv.y):-Math.abs(mtv.y)));
        Vector2D mtvB =
            new Vector2D((float)((A.getPosition().x>B.getPosition().x)?-Math.abs(mtv.x):Math.abs(mtv.x)),
                    (float)((A.getPosition().y>B.getPosition().y)?-Math.abs(mtv.y):Math.abs(mtv.y)));

        // Distribute mtv length between A and B
        Vector2D mtvNormA = new Vector2D(mtvA).normalize();
        Vector2D mtvNormB = new Vector2D(mtvB).normalize();
        float velocityForMtvA = Math.abs(mtvNormA.dot(A.getVelocity()));
        float velocityForMtvB = Math.abs(mtvNormB.dot(B.getVelocity()));
        float scalarVelocityA = A.getVelocity().length();
        float scalarVelocityB = B.getVelocity().length();

        float potionMtvA, potionMtvB;
        if (velocityForMtvA + velocityForMtvB == 0.0f) {
            potionMtvA = scalarVelocityA / (scalarVelocityA + scalarVelocityB);
            potionMtvB = scalarVelocityB / (scalarVelocityA + scalarVelocityB);
        }
        else {
            potionMtvA = velocityForMtvA / (velocityForMtvA+velocityForMtvB);
            potionMtvB = velocityForMtvB / (velocityForMtvA+velocityForMtvB);
        }
        mtvA.multiply(potionMtvA);
        mtvB.multiply(potionMtvB);

        // Set new offset to the position of A and B
        A.offset(new PointF(mtvA).expandToNextInt());
        B.offset(new PointF(mtvB).expandToNextInt());
    }

    public Manifold checkCollision(CollidableObject A, CollidableObject B) {
        Shape shapeA = A.getShape();
        Shape shapeB = B.getShape();

        if (!shapeA.isValid() || !shapeB.isValid()) {
            return null;
        }

        // Broader check
        Manifold manifold = testCircle(A, B);
        if (manifold == null) {
            return null;
        }

        // Close test
        if (shapeA.isCircle() && shapeB.isCircle()) {
            return manifold;
        }
        else if (shapeA.isCircle() && !shapeB.isCircle()) {
            return testCirclePolygon(A, B);
        }
        else if (!shapeA.isCircle() && shapeB.isCircle()) {
            manifold = testCirclePolygon(B, A);
            if (manifold != null)
                manifold.normal.multiply(-1);
            return manifold;
        }
        else {
            return testPolygon(A, B);
        }
    }

    private Manifold testCircle(CollidableObject A, CollidableObject B) {
        Vector2D centerA = A.getPositionVector();
        Vector2D centerB = B.getPositionVector();

        float radiusA = A.getShape().getRadius();
        float radiusB = B.getShape().getRadius();
        float radiusSum = radiusA + radiusB;

        if (centerA.distanceSq(centerB) > Math.pow(radiusSum,2)) {
            return null;
        }

        float d = centerA.distance(centerB);

        if (d != 0) {
            return new Manifold(new Vector2D(centerB).subtract(centerA).normalize(),
                    radiusSum - d);
        }
        else {
            return new Manifold(new Vector2D(1.0f, 0.0f), radiusA);
        }
    }

    private Manifold testCirclePolygon(CollidableObject circle, CollidableObject polygon) {
        ArrayList<PointF> polygonVertices = polygon.getShape().getVertices();
        Vector2D circleCenter = circle.getPositionVector();
        float circleRadius = circle.getShape().getRadius();

        // Check distance between circle center and polygon vertices
        Vector2D nearestVertex = null;
        float minDistance = Float.MAX_VALUE;
        for (int i = 0; i < polygonVertices.size(); ++i) {
            Vector2D vertex = polygonVertices.get(i).vectorize();
            float distanceSq = vertex.distanceSq(circleCenter);
            if (distanceSq < minDistance) {
                nearestVertex = vertex;
                minDistance = distanceSq;
            }
        }

        minDistance = (float) Math.sqrt(minDistance);
        if (minDistance > circleRadius) {
            return null;
        }

        return new Manifold(new Vector2D(nearestVertex).subtract(circleCenter).normalize(),
                circleRadius - minDistance);
    }

    private Manifold testPolygon(CollidableObject A, CollidableObject B) {
        // Get Axes for testing
        ArrayList<Vector2D> axes = A.getShape().getAxes();
        axes.addAll(B.getShape().getAxes());

        // Project vertices to each axes and test if overlap
        Vector2D smallestAxis = null;
        float minOverlap = Float.MAX_VALUE;
        for (int i = 0; i < axes.size(); ++i) {
            Vector2D axis = axes.get(i);
            SATProjection p1 = A.getShape().projectToAxis(axis);
            SATProjection p2 = B.getShape().projectToAxis(axis);

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

        return new Manifold(smallestAxis, minOverlap);
    }
}
