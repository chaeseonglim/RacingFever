package com.lifejourney.engine2d;

import java.util.ArrayList;

public class World {

    private static final String LOG_TAG = "World";

    public World(Size worldSize) {
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
            updateViews();
            updateObjects();
            updateViewport();
            accumulatedTime -= dt;
        }
    }

    public void commit() {
        commitViews();
        commitObjects();
    }

    protected void updateViews() {
        mainView.update();
        for (View view : subViews) {
            view.update();
        }
    }

    protected void updateObjects() {
        for (Object object : objects) {
            object.update();
        }

        // Check collision
        collisionPool.checkCollision();
    }

    protected void updateViewport() {
        Rect viewport = Engine2D.GetInstance().getViewport();
        //viewport.offset(1, 1);
        Engine2D.GetInstance().setViewport(viewport);
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

    protected float desiredFPS = 30.0f;
    protected long accumulatedTime;
    protected long lastUpdateStartTime = System.currentTimeMillis();

    protected View mainView;
    protected ArrayList<View> subViews = new ArrayList<>();
    protected ArrayList<Object> objects = new ArrayList<>();
    protected CollisionPool collisionPool;
}
