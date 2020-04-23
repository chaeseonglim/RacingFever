package com.lifejourney.racingfever;

import androidx.fragment.app.FragmentActivity;

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

        // Set resolution of Engine
        Camera.GetInstance().setViewport(0, 0, 1280, 728);
        Rect viewport = Camera.GetInstance().getViewport();
        nEngineSetResolution(viewport.getWidth(), viewport.getHeight());
    }

    private Object testObject;
    private MapData testMapData;
    private MapView testMapView;

    protected void initResources() {
        ResourceManager.GetInstance().addContext(getApplicationContext());

        testMapData = new MapData("maps/istanbul-park.png");
        testMapView = new MapView(testMapData);

        testObject = new Object(100, 100, 96, 96, "car1.png");
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

    private float[] translateScreenToGameCoord(float[] xy) {
        Rect viewport = Camera.GetInstance().getViewport();
        return new float[] { xy[0] / screenWidth * viewport.getWidth(),
            xy[1] / screenHeight * viewport.getHeight() };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int eventAction = event.getAction();

        switch (eventAction)
        {
            case MotionEvent.ACTION_DOWN:
                float[] newXY = translateScreenToGameCoord(new float[] {event.getX(), event.getY()});
                testObject.setPos((int)newXY[0], (int)newXY[1]);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Do nothing here, waiting for surfaceChanged instead
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Surface surface = holder.getSurface();
        nEngineSetSurface(surface, width, height);
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        nEngineClearSurface();
    }

    private boolean isRunning;
    private int screenWidth = 1;
    private int screenHeight = 1;

    private native void nEngineInit();
    private native void nEngineSetSurface(Surface surface, int width, int height);
    private native void nEngineClearSurface();
    private native void nEngineStart();
    private native void nEngineStop();
    private native void nEngineSetAutoSwapInterval(boolean enabled);
    private native float nEngineGetAverageFps();
    private native int nEngineGetSwappyStats(int stat, int bin);
    private native void nEngineSetResolution(int width, int height);
}
