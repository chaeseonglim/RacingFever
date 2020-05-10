package com.lifejourney.engine2d;

import android.util.Log;

import java.util.ArrayList;

public class CollisionPool {

    private static final String LOG_TAG = "CollisionPool";

    public CollisionPool(Size regionSize) {
        RectF region = new RectF(0, 0, (float)regionSize.width, (float)regionSize.height);
        objects = new ArrayList<>();
        quadTree = new QuadTree(0, region);
    }

    public void addObject(CollidableObject object) {
        objects.add(object);
    }

    public void removeObject(CollidableObject object) {
        objects.remove(object);
    }

    public void checkCollision() {
        CollisionDetector collisionDetector = Engine2D.GetInstance().getCollisionDetector();

        for (CollidableObject object : objects) {
            quadTree.insert(object);
        }

        ArrayList<CollidableObject> candidatesList = new ArrayList<>();

        for (CollidableObject refObject : objects) {
            candidatesList.clear();

            // Retreive candidates
            candidatesList = quadTree.retrieve(candidatesList, refObject);

            for (CollidableObject candidateObject : candidatesList) {
                if (refObject == candidateObject ||
                        candidateObject.isCollisionChecked())
                    continue;

                // Collision check
                collisionDetector.checkAndReponseCollision(refObject, candidateObject);
            }

            refObject.setCollistionChecked(true);
        }

        for (CollidableObject object : objects) {
            object.setCollistionChecked(false);
        }

        quadTree.clear();
    }

    public boolean testCollision(CollidableObject egoObject, PointF newPosition) {
        CollisionDetector collisionDetector = Engine2D.GetInstance().getCollisionDetector();

        PointF oldPosition = egoObject.getPosition();
        egoObject.setPosition(newPosition);

        boolean result = false;
        for (CollidableObject candidateObject : objects) {
            if (egoObject == candidateObject)
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
