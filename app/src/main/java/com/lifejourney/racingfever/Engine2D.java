package com.lifejourney.racingfever;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Trace;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.util.Locale;

// TODO: Fix speed issue on MapView
// TODO: Add level attributes in sprite

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
        // Initialize Engine
        nEngineInit(activity);

        // Initialize resource manager
        resourceManager = new ResourceManager(activity.getApplicationContext());
    }

    public void setSurface(Surface surface, int width, int height) {
        nEngineSetSurface(surface, width, height);
        screenSize = new Size(width, height);
    }

    public void clearSurface() {
        nEngineClearSurface();
    }

    public void setViewport(int x, int y, int width, int height) {
        viewport = new Rect(x, y, x + width, y + height);
        nEngineSetViewport(x, y, width, height);
    }

    public void setViewport(Rect viewport) {
        this.viewport = viewport;
        nEngineSetViewport(viewport.left, viewport.top, viewport.width(), viewport.height());
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
        return new float[] { xy[0] / screenSize.width * viewport.width(),
                xy[1] / screenSize.height * viewport.height() };
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    private Size screenSize;
    private Rect viewport;
    private ResourceManager resourceManager;

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
