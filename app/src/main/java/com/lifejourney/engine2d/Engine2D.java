package com.lifejourney.engine2d;

import android.app.Activity;
import android.view.Surface;

public class Engine2D {

    // Used to load the 'RacingFever' library on application startup.
    static {
        System.loadLibrary("Engine2D");
    }

    private Engine2D() {}
    private static class Singleton {
        private static final Engine2D instance = new Engine2D();
    }

    public static Engine2D GetInstance() {
        return Singleton.instance;
    }

    private static final long ONE_MS_IN_NS = 1000000;
    private static final long ONE_S_IN_NS = 1000 * ONE_MS_IN_NS;

    private static final String LOG_TAG = "Engine2D";

    public void initEngine(Activity activity) {
        if (!inited) {
            // Initialize Engine
            nEngineInit(activity);

            // Initialize resource manager
            resourceManager = new ResourceManager(activity.getApplicationContext());

            // Initialize collision detector
            collisionDetector = new CollisionDetector();

            inited = true;
        }
    }

    public void setSurface(Surface surface, int width, int height) {
        nEngineSetSurface(surface, width, height);
        screenSize = new Size(width, height);
    }

    public void clearSurface() {
        nEngineClearSurface();
    }

    public void setViewport(Rect viewport) {
        this.viewport = viewport;
        nEngineSetViewport(viewport.x, viewport.y, viewport.width, viewport.height);
    }

    public Rect getViewport() {
        return viewport;
    }

    public void start() {
        nEngineStart();
    }

    public void stop() {
        nEngineStop();
    }

    public float getAverageFps() {
        return nEngineGetAverageFps();
    }

    public float[] translateScreenToGameCoord(float[] xy) {
        return new float[] { xy[0] / screenSize.width * viewport.width + viewport.x,
                xy[1] / screenSize.height * viewport.height  + viewport.y };
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public CollisionDetector getCollisionDetector() {
        return collisionDetector;
    }

    private Size screenSize;
    private Rect viewport;
    private ResourceManager resourceManager;
    private CollisionDetector collisionDetector;
    private boolean inited = false;

    private native void nEngineInit(Activity activity);
    private native void nEngineSetSurface(Surface surface, int width, int height);
    private native void nEngineClearSurface();
    private native void nEngineStart();
    private native void nEngineStop();
    private native void nEngineSetAutoSwapInterval(boolean enabled);
    private native float nEngineGetAverageFps();
    private native int nEngineGetSwappyStats(int stat, int bin);
    private native void nEngineSetViewport(int x, int y, int width, int height);
}
