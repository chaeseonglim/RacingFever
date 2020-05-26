package com.lifejourney.engine2d;

import android.util.Log;

import java.util.ArrayList;

class CollisionDetector {

    private static final String LOG_TAG = "CollisionDetector";

    private static class Manifold {
        public int index;
        public Vector2D normal;
        float penetration;

        Manifold(int index, Vector2D normal, float penetration) {
            this.index = index;
            this.normal = normal;
            this.penetration = penetration;
        }
    }

    /**
     *
     * @param A
     * @param B
     * @return
     */
    boolean checkAndReponseCollision(CollidableObject A, CollidableObject B) {
        // Check if collision occurs
        Manifold manifold = getCollisionState(A, B);
        if (manifold == null) {
            return false;
        }

        // Post collision handling
        correctPosition(A, B, manifold);
        resolveImpulse(A, B, manifold);

        // Call event handlers of each objects
        A.onCollisionOccurred(B);
        B.onCollisionOccurred(A);

        return true;
    }

    /**
     *
     * @param A
     * @param B
     * @return
     */
    boolean checkCollision(CollidableObject A, CollidableObject B) {
        return getCollisionState(A, B) != null;
    }

    /**
     *
     * @param A
     * @param B
     * @param manifold
     */
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

        // Calculate impulse scalar
        float j = -(1 + e) * vecAlongNormal;

        float invMassA = A.getInvMass();
        float invMassB = B.getInvMass();
        j /= invMassA + invMassB;

        // Apply impulse
        Vector2D impulse = new Vector2D(normal).multiply(j);
        Vector2D massImpulseA = new Vector2D(impulse).multiply(-1);
        Vector2D massImpulseB = new Vector2D(impulse);

        Shape shapeA = A.getShape();
        Shape shapeB = B.getShape();

        // Get contact point of collide objects
        Vector2D contactPointWorld, contactPointA, contactPointB;
        if (shapeA.isCircle() && shapeB.isCircle()) {
            contactPointA = new Vector2D(B.getPositionVector()).subtract(A.getPositionVector());

            contactPointA.normalize().multiply(shapeA.getRadius());
            contactPointB = new Vector2D(A.getPositionVector()).subtract(B.getPositionVector());
            contactPointB.normalize().multiply(shapeB.getRadius());
        }
        else if (shapeA.isCircle() || shapeB.isCircle()) {
            contactPointWorld = (shapeA.isCircle())?
                    new Vector2D(shapeB.getVertices().get(manifold.index).vectorize()) :
                    new Vector2D(shapeA.getVertices().get(manifold.index).vectorize());
            contactPointA = new Vector2D(contactPointWorld).subtract(A.getPositionVector());
            contactPointB = new Vector2D(contactPointWorld).subtract(B.getPositionVector());
        }
        else {
            contactPointWorld = new Vector2D(shapeA.getVertices().get(manifold.index).vectorize());
            contactPointA = new Vector2D(contactPointWorld).subtract(A.getPositionVector());
            contactPointB = new Vector2D(contactPointWorld).subtract(B.getPositionVector());
        }

        A.addForce(massImpulseA, contactPointA);
        B.addForce(massImpulseB, contactPointB);
    }

    /**
     *
     * @param A
     * @param B
     * @param manifold
     */
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
            float scalarVelocitySum = scalarVelocityA + scalarVelocityB;
            if (scalarVelocitySum == 0.0f) {
                potionMtvA = potionMtvB = 0.0f;
            }
            else {
                potionMtvA = scalarVelocityA / scalarVelocitySum;
                potionMtvB = scalarVelocityB / scalarVelocitySum;
            }
        }
        else {
            potionMtvA = velocityForMtvA / (velocityForMtvA + velocityForMtvB);
            potionMtvB = velocityForMtvB / (velocityForMtvA + velocityForMtvB);
        }
        mtvA.multiply(potionMtvA);
        mtvB.multiply(potionMtvB);

        // Set new offset to the position of A and B
        A.offset(new PointF(mtvA).expandToNextInt());
        B.offset(new PointF(mtvB).expandToNextInt());
    }

    /**
     *
     * @param A
     * @param B
     * @return
     */
    private Manifold getCollisionState(CollidableObject A, CollidableObject B) {
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

    /**
     *
     * @param A
     * @param B
     * @return
     */
    private Manifold testCircle(CollidableObject A, CollidableObject B) {
        Vector2D centerA = A.getPositionVector();
        Vector2D centerB = B.getPositionVector();

        float radiusA = A.getShape().getRadius();
        float radiusB = B.getShape().getRadius();
        float radiusSum = radiusA + radiusB;

        float distanceSq = centerA.distanceSq(centerB);

        if (distanceSq > Math.pow(radiusSum,2)) {
            return null;
        }

        if (distanceSq != 0) {
            float distance = (float) Math.sqrt(distanceSq);
            return new Manifold(-1, new Vector2D(centerB).subtract(centerA).normalize(),
                    radiusSum - distance);
        }
        else {
            return new Manifold(-1, new Vector2D(1.0f, 0.0f), radiusA);
        }
    }

    /**
     *
     * @param circle
     * @param polygon
     * @return
     */
    private Manifold testCirclePolygon(CollidableObject circle, CollidableObject polygon) {
        ArrayList<PointF> polygonVertices = polygon.getShape().getVertices();
        Vector2D circleCenter = circle.getPositionVector();
        float circleRadius = circle.getShape().getRadius();

        // Check distance between circle center and polygon vertices
        float minDistance = Float.MAX_VALUE;
        Vector2D nearestVertex = null;
        int nearestVertexIndex = -1;
        for (int i = 0; i < polygonVertices.size(); ++i) {
            Vector2D vertex = polygonVertices.get(i).vectorize();
            float distanceSq = vertex.distanceSq(circleCenter);
            if (distanceSq < minDistance) {
                nearestVertexIndex = i;
                nearestVertex = vertex;
                minDistance = distanceSq;
            }
        }

        if (nearestVertex == null) {
            return null;
        }

        minDistance = (float) Math.sqrt(minDistance);
        if (minDistance > circleRadius) {
            return null;
        }

        return new Manifold(nearestVertexIndex,
                nearestVertex.clone().subtract(circleCenter).normalize(),
                circleRadius - minDistance);
    }

    /**
     *
     * @param A
     * @param B
     * @return
     */
    private Manifold testPolygon(CollidableObject A, CollidableObject B) {
        Manifold manifoldAB = findAxisLeastPenetration(A, B);
        if (manifoldAB == null || manifoldAB.penetration > 0.0f)
            return null;

        Manifold manifoldBA = findAxisLeastPenetration(B, A);
        if (manifoldBA == null || manifoldBA.penetration > 0.0f)
            return null;

        return manifoldAB;
    }

    /**
     *
     * @param A
     * @param B
     * @return
     */
    private Manifold findAxisLeastPenetration(CollidableObject A, CollidableObject B) {
        float bestDistance = -Float.MAX_VALUE;
        int bestIndex = -1;

        Shape shapeA = A.getShape();
        ArrayList<PointF> verticesA = shapeA.getVertices();
        ArrayList<Vector2D> axesA = shapeA.getAxes();
        for (int i = 0; i < verticesA.size(); ++i) {
            // Get axes normal vector
            Vector2D normalA = axesA.get(i);

            // Get support vector of B along -normalA
            Vector2D supportB = B.getShape().getSupportPoint(new Vector2D(normalA).multiply(-1));
            if (supportB == null) {
                Log.e(LOG_TAG, "What happend here??? " + A.getPosition().x + " " +
                        A.getPosition().y + " " + B.getPosition().x + " " + B.getPosition().y);
                continue;
            }

            // Compute penetration distance (in B's model space)
            Vector2D vertexA = verticesA.get(i).vectorize();
            float distance = normalA.dot(supportB.subtract(vertexA));

            // Store greatest distance
            if (distance > bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }

        if (bestIndex == -1) {
            return null;
        }
        else {
            return new Manifold(bestIndex, axesA.get(bestIndex), bestDistance);
        }
    }
}
