package com.lifejourney.racingfever;

import androidx.fragment.app.FragmentActivity;

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

import java.util.Locale;

public class RacingFever extends FragmentActivity implements Choreographer.FrameCallback, SurfaceHolder.Callback {

    private static final long ONE_MS_IN_NS = 1000000;
    private static final long ONE_S_IN_NS = 1000 * ONE_MS_IN_NS;

    private static final String LOG_TAG = "RacingFever";

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
        Engine2D.GetInstance().initEngine(this);

        // Set resolution of Engine
        Engine2D.GetInstance().setViewport(new Rect(0, 0, 1280, 720));
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
        Engine2D.GetInstance().start();
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        isRunning = false;
        Engine2D.GetInstance().stop();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        Trace.beginSection("doFrame");

        TextView fpsView = findViewById(R.id.fps);
        fpsView.setText(String.format(Locale.US, "FPS: %.1f", Engine2D.GetInstance().getAverageFps()));

        long now = System.nanoTime();

        if (isRunning) {
            // Update world
            updateWorld();

            Trace.beginSection("Requesting callback");
            Choreographer.getInstance().postFrameCallback(this);
            Trace.endSection();
        }

        Trace.endSection();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int eventAction = event.getAction();

        switch (eventAction)
        {
            case MotionEvent.ACTION_DOWN:
                float[] newXY = Engine2D.GetInstance().translateScreenToGameCoord(
                        new float[] {event.getX(), event.getY()});
                testObject.setPos((int)newXY[0], (int)newXY[1]);
                testObject.setVelocity(2.0f);
                testObject.setAcceleration(0.5f);
                testObject.setFriction(0.03f);
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
        Engine2D.GetInstance().setSurface(surface, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Engine2D.GetInstance().clearSurface();
    }

    private MovableObject testObject;
    private MapData testMapData;
    private MapView testMapView;

    protected void initResources() {
        testMapData = new MapData("maps/istanbul-park.png");
        testMapView = new MapView(testMapData);
        testMapView.show();

        MovableObject.Builder objBuilder =
                new MovableObject.Builder(new Rect(100, 100, 196, 196))
                        .depth(1.0f).spriteAsset("car1.png")
                        .velocity(2.0f).acceleration(0.5f).friction(0.03f)
                        .visible(true);
        testObject = objBuilder.build();
    }

    void updateWorld() {
        testMapView.update();
        testObject.update();

        testMapView.commit();
        testObject.commit();

        Rect viewport = Engine2D.GetInstance().getViewport();
        //viewport.offset(1, 1);
        Engine2D.GetInstance().setViewport(viewport);
    }

    private boolean isRunning;
}
