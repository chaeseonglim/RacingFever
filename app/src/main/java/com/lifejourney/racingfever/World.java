package com.lifejourney.racingfever;

import android.view.MotionEvent;

import java.util.HashMap;

public abstract class World {

    public abstract void init();

    public void update() {
        updateView();
        updateWorld();
        updateViewport();
        commit();
    }

    protected abstract void updateView();

    protected void updateWorld() {
        // Check the time elapsed since last update
        long currentTime = System.currentTimeMillis();
        accumulatedTime += currentTime - lastUpdateStartTime;
        lastUpdateStartTime = currentTime;

        // Prevent accumulatedTime goes up to infinite..
        if (accumulatedTime > 200)
            accumulatedTime = 200;

        long dt = (long) (1000.0f / desiredFPS);
        while (accumulatedTime > dt) {
            updateObjects();
            accumulatedTime -= dt;
        }
    }

    protected void updateObjects() {
    }

    protected void updateViewport() {
        Rect viewport = Engine2D.GetInstance().getViewport();
        //viewport.offset(1, 1);
        Engine2D.GetInstance().setViewport(viewport);
    }

    protected void commit() {
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    protected float desiredFPS = 30.0f;
    protected long accumulatedTime;
    protected long lastUpdateStartTime = System.currentTimeMillis();

    protected HashMap<String, CollisionPool> collisionPools;
}
