package com.lifejourney.engine2d;

import android.app.Activity;
import android.util.Log;
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

    /**
     *
     * @param activity
     */
    public void initEngine(Activity activity) {
        if (!initialized) {
            Log.e(LOG_TAG, "activity: " + activity);

            // Initialize Engine
            nEngineInit(activity);

            // Initialize resource manager
            resourceManager = new ResourceManager(activity.getApplicationContext());

            // Initialize collision detector
            collisionDetector = new CollisionDetector();

            initialized = true;
        }
    }

    /**
     *
     */
    public void finalizeEngine() {
        if (initialized) {

            // Finalize resource manager
            resourceManager.releaseAll();

            // Finalize Engine
            nEngineFinalize();

            initialized = false;
        }
    }

    /**
     *
     * @param surface
     * @param width
     * @param height
     */
    public void setSurface(Surface surface, int width, int height) {
        nEngineSetSurface(surface, width, height);
        screenSize = new Size(width, height);
    }

    /**
     *
     */
    public void clearSurface() {
        nEngineClearSurface();
    }

    /**
     *
     * @param viewport
     */
    public void setViewport(Rect viewport) {
        this.viewport = viewport;
        nEngineSetViewport(viewport.x, viewport.y, viewport.width, viewport.height);
    }

    /**
     *
     * @return
     */
    public Rect getViewport() {
        return viewport;
    }

    /**
     *
     */
    public void start() {
        nEngineStart();
    }

    /**
     *
     */
    public void stop() {
        nEngineStop();
    }

    /**
     *
     */
    void lockDraw() {
        nEngineLockDraw();
    }

    /**
     *
     */
    void unlockDraw() {
        nEngineUnlockDraw();
    }

    /**
     *
     * @return
     */
    public float getAverageFps() {
        return nEngineGetAverageFps();
    }

    /**
     *
     * @param xy
     * @return
     */
    public float[] translateScreenToGameCoord(float[] xy) {
        return new float[] { xy[0] / screenSize.width * viewport.width + viewport.x,
                xy[1] / screenSize.height * viewport.height  + viewport.y };
    }

    /**
     *
     * @return
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     *
     * @return
     */
    CollisionDetector getCollisionDetector() {
        return collisionDetector;
    }

    /**
     *
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    private Size screenSize;
    private Rect viewport;
    private ResourceManager resourceManager;
    private CollisionDetector collisionDetector;
    private boolean initialized = false;

    private native void nEngineInit(Activity activity);
    private native void nEngineFinalize();
    private native void nEngineSetSurface(Surface surface, int width, int height);
    private native void nEngineClearSurface();
    private native void nEngineStart();
    private native void nEngineStop();
    private native void nEngineSetAutoSwapInterval(boolean enabled);
    private native float nEngineGetAverageFps();
    private native int nEngineGetSwappyStats(int stat, int bin);
    private native void nEngineSetViewport(int x, int y, int width, int height);
    private native void nEngineLockDraw();
    private native void nEngineUnlockDraw();
}
