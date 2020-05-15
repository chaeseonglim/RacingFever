package com.lifejourney.engine2d;

import android.util.Log;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class World {

    private static final String LOG_TAG = "World";

    public void initCollisionPool(Size worldSize) {
        collisionPool = new CollisionPool(worldSize);
    }

    public void update() {
        // Check the time elapsed since last update
        long currentTime = System.currentTimeMillis();
        accumulatedTime += currentTime - lastUpdateStartTime;
        lastUpdateStartTime = currentTime;

        // Prevent accumulatedTime goes up to infinite..
        if (accumulatedTime > 200)
            accumulatedTime = 200;

        long dt = (long) (1000.0f / desiredFPS);
        while (accumulatedTime > dt) {
            preUpdate();
            updateViews();
            updateObjects();
            postUpdate();
            accumulatedTime -= dt;
        }
    }

    public void commit() {
        Engine2D.GetInstance().lockDraw();
        commitViews();
        commitObjects();
        Engine2D.GetInstance().unlockDraw();
    }

    protected void preUpdate() {
    }

    protected void postUpdate() {
    }


    protected void updateViews() {
        mainView.update();
        for (View view : subViews) {
            view.update();
        }
    }

    protected void updateObjects() {
        PriorityQueue<Object> updateQueue = new PriorityQueue<>();
        for (Object object : objects) {
            updateQueue.offer(object);
        }
        while (!updateQueue.isEmpty()) {
            updateQueue.poll().update();
        }

        // Check collision
        collisionPool.checkCollision();
    }

    protected void commitViews() {
        mainView.commit();
        for (View view : subViews) {
            view.commit();
        }
    }

    protected void commitObjects() {
        for (Object object : objects) {
            object.commit();
        }
    }

    public void addObject(Object object) {
        objects.add(object);
    }

    public void removeObject(Object object) {
        objects.remove(object);
    }

    public void addObject(CollidableObject object) {
        objects.add(object);
        collisionPool.addObject(object);
    }

    public void removeObject(CollidableObject object) {
        collisionPool.removeObject(object);
        objects.remove(object);
    }

    public void addMainView(View view) {
        mainView = view;
    }

    public void removeMainView(View view) {
        mainView = null;
    }

    public void addSubView(View view) {
        subViews.add(view);
    }

    public void removeSubView(View view) {
        subViews.remove(view);
    }

    public CollisionPool getCollisionPool() {
        return collisionPool;
    }

    protected float desiredFPS = 10.0f;
    protected long accumulatedTime;
    protected long lastUpdateStartTime = System.currentTimeMillis();

    protected View mainView;
    protected ArrayList<View> subViews = new ArrayList<>();
    protected ArrayList<Object> objects = new ArrayList<>();
    protected CollisionPool collisionPool;
}
