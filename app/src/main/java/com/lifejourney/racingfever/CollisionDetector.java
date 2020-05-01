package com.lifejourney.racingfever;

import java.util.ArrayList;

public class CollisionDetector {

    public CollisionDetector() {
    }

    public boolean updateCollision(CollidableObject A, CollidableObject B) {
        // Check if collision occurs
        Vector2D mtv = checkCollision(A, B);

        if (mtv == null) {
            return false;
        }

        adjustToMtv(A, B, mtv);
        resolveImpulse(A, B, mtv);

        return true;
    }

    private void resolveImpulse(CollidableObject A, CollidableObject B, Vector2D mtv) {
        Vector2D normal = B.getPositionVector().subtract(A.getPositionVector()).
                normalize();

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

        A.addForce(massImpulseA);
        B.addForce(massImpulseB);
    }

    private void adjustToMtv(CollidableObject A, CollidableObject B, Vector2D mtv) {
        // Share MTV between A and B
        Vector2D aMtv =
                new Vector2D((float)((A.getPosition().x>B.getPosition().x)?Math.abs(mtv.x):-Math.abs(mtv.x)),
                        (float)((A.getPosition().y>B.getPosition().y)?Math.abs(mtv.y):-Math.abs(mtv.y)));
        Vector2D bMtv =
                new Vector2D((float)((A.getPosition().x>B.getPosition().x)?-Math.abs(mtv.x):Math.abs(mtv.x)),
                        (float)((A.getPosition().y>B.getPosition().y)?-Math.abs(mtv.y):Math.abs(mtv.y)));

        Vector2D aMtvNorm = new Vector2D(aMtv).normalize();
        Vector2D bMtvNorm = new Vector2D(bMtv).normalize();
        float velocityForMtvA = Math.abs(aMtvNorm.dot(A.getVelocity()));
        float velocityForMtvB = Math.abs(bMtvNorm.dot(B.getVelocity()));
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
        aMtv.multiply(potionMtvA);
        bMtv.multiply(potionMtvB);

        A.offset(new PointF(aMtv));
        B.offset(new PointF(bMtv));
    }

    public Vector2D checkCollision(CollidableObject A, CollidableObject B) {
        // Check round area
        if (!checkRadius(A, B)) {
            return null;
        }

        // if both are circle, no need to go sat
        if (A.getShape().isCircle() && B.getShape().isCircle()) {
            return new Vector2D();
        }

        // check SAT
        Vector2D mtv = checkSAT(A, B);
        if (mtv == null) {
            return null;
        }

        return mtv;
    }

    private boolean checkRadius(CollidableObject A, CollidableObject B) {
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

    private Vector2D checkSAT(CollidableObject A, CollidableObject B) {
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
