package com.lifejourney.racingfever;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.os.Trace;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Locale;

public class RacingFever extends FragmentActivity implements Choreographer.FrameCallback, SurfaceHolder.Callback {

    // Used to load the 'RacingFever' library on application startup.
    static {
        System.loadLibrary("Engine2D");
    }

    private static final long ONE_MS_IN_NS = 1000000;
    private static final long ONE_S_IN_NS = 1000 * ONE_MS_IN_NS;

    private static final String LOG_TAG = "RacingFever.java";

    protected void initEngine() {
        // Get display metrics
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        float refreshRateHz = display.getRefreshRate();
        Log.i(LOG_TAG, String.format("Refresh rate: %.1f Hz", refreshRateHz));

        // Initialize the surfaceView
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);

        // Initialize Engine
        nEngineInit();
    }

    private Object testObject;

    protected void initResources() {
        ResourceManager.GetInstance().addContext(getApplicationContext());

        testObject = new Object(0, 0, 300, 300, "car1.png");
        testObject.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize display & engine
        initEngine();

        // Initialize resources
        initResources();
    }

    @Override
    protected void onStart() {
        super.onStart();

        isRunning = true;
        nEngineStart();
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        isRunning = false;
        nEngineStop();
    }
    @Override
    public void doFrame(long frameTimeNanos) {
        Trace.beginSection("doFrame");

        TextView fpsView = findViewById(R.id.fps);
        fpsView.setText(String.format(Locale.US, "FPS: %.1f", nEngineGetAverageFps()));

        long now = System.nanoTime();

        if (isRunning) {
            Trace.beginSection("Requesting callback");
            Choreographer.getInstance().postFrameCallback(this);
            Trace.endSection();
        }

        Trace.endSection();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Do nothing here, waiting for surfaceChanged instead
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Surface surface = holder.getSurface();
        nEngineSetSurface(surface, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        nEngineClearSurface();
    }

    private boolean isRunning;
    private ResourceManager resourceManager;

    private native void nEngineInit();
    private native void nEngineSetSurface(Surface surface, int width, int height);
    private native void nEngineClearSurface();
    private native void nEngineStart();
    private native void nEngineStop();
    private native void nEngineSetAutoSwapInterval(boolean enabled);
    private native float nEngineGetAverageFps();
    private native int nEngineGetSwappyStats(int stat, int bin);
}
