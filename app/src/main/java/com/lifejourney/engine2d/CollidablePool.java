package com.lifejourney.engine2d;

import java.util.ArrayList;

public class CollidablePool {

    private static final String LOG_TAG = "CollidablePool";

    CollidablePool(Size regionSize) {
        RectF region = new RectF(0, 0, (float)regionSize.width, (float)regionSize.height);
        objects = new ArrayList<>();
        quadTree = new QuadTree(0, region);
    }

    /**
     *
     * @param object
     */
    void addObject(CollidableObject object) {
        objects.add(object);
    }

    /**
     *
     * @param object
     */
    void removeObject(CollidableObject object) {
        objects.remove(object);
    }

    /**
     *
     */
    void checkCollision() {
        CollisionDetector collisionDetector = Engine2D.GetInstance().getCollisionDetector();

        for (CollidableObject object : objects) {
            quadTree.insert(object);
        }

        ArrayList<CollidableObject> candidatesList = new ArrayList<>();

        for (CollidableObject refObject : objects) {
            if (!refObject.isCollisionEnabled())
                continue;

            // Retreive candidates
            candidatesList.clear();
            candidatesList = quadTree.retrieve(candidatesList, refObject);

            for (CollidableObject candidateObject : candidatesList) {
                if (refObject == candidateObject ||
                        candidateObject.isCollisionChecked() ||
                        !candidateObject.isCollisionEnabled())
                    continue;

                // Collision check
                collisionDetector.checkAndReponseCollision(refObject, candidateObject);
            }

            refObject.setCollisionChecked(true);
        }

        for (CollidableObject object : objects) {
            object.setCollisionChecked(false);
        }

        quadTree.clear();
    }

    /**
     *
     * @param egoObject
     * @param newPosition
     * @return
     */
    public boolean testCollision(CollidableObject egoObject, PointF newPosition) {
        CollisionDetector collisionDetector = Engine2D.GetInstance().getCollisionDetector();

        PointF oldPosition = egoObject.getPosition();
        egoObject.setPosition(newPosition);

        boolean result = false;
        for (CollidableObject candidateObject : objects) {
            if (egoObject == candidateObject || !candidateObject.isCollisionEnabled())
                continue;

            // Collision check
            if (collisionDetector.checkCollision(egoObject, candidateObject)) {
                result = true;
                break;
            }
        }

        egoObject.setPosition(oldPosition);
        return result;
    }

    private ArrayList<CollidableObject> objects;
    private QuadTree quadTree;
}
