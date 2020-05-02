package com.lifejourney.engine2d;

import java.util.ArrayList;

public class CollisionPool {

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
        for (CollidableObject object : objects) {
            quadTree.insert(object);
        }

        ArrayList<CollidableObject> list = new ArrayList<>();

        for (CollidableObject refObject : objects) {
            list.clear();

            // Retreive candidates
            list = quadTree.retrieve(list, refObject);

            for (CollidableObject targetObject : list) {
                if (refObject == targetObject ||
                        targetObject.isCollisionChecked())
                    continue;

                // Collision check
                CollisionDetector collisionDetector = Engine2D.GetInstance().getCollisionDetector();
                collisionDetector.updateCollision(refObject, targetObject);
            }

            refObject.setCollistionChecked(true);
        }

        for (CollidableObject object : objects) {
            object.setCollistionChecked(false);
        }

        quadTree.clear();
    }

    private ArrayList<CollidableObject> objects;
    private QuadTree quadTree;
}
