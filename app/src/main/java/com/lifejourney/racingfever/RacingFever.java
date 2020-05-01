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
                testObject1.setPosition(new PointF(newXY[0], newXY[1]));
                testObject1.setVelocity(new Vector2D(225.0f).multiply(3.0f));

                testObject2.setPosition(new PointF(500, 500));
                testObject2.setVelocity(new Vector2D(45.0f).multiply(0.5f));
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

    private CollidableObject testObject1, testObject2;
    private MapData testMapData;
    private MapView testMapView;

    protected void initResources() {
        testMapData = new MapData("maps/istanbul-park.png");
        testMapView = new MapView(testMapData);
        testMapView.show();

        float scale = 3.0f;
        Size objSize = new Size(32, 32).multiply(scale);
        Shape objShape = new Shape(new PointF[] {
                new PointF(5,13),
                new PointF(2,18),
                new PointF(2,23),
                new PointF(23,23),
                new PointF(29,15),
                new PointF(29,13),
                new PointF(23,8),
                new PointF(15,8),
                new PointF(8,13)
        }).subtract(new PointF(16, 16)).multiply(scale);

        testObject1 =
                new CollidableObject.Builder<>(new PointF(100, 100))
                        .size(objSize).depth(1.0f).asset("car1.png")
                        .velocity(new Vector2D(225.0f).multiply(2.0f))
                        .friction(0.01f)
                        .shape(new Shape(objShape)).visible(true).build();

        testObject2 =
                new CollidableObject.Builder<>(new PointF(500, 500))
                        .size(objSize).depth(1.0f).asset("car1.png")
                        .velocity(new Vector2D(45.0f).multiply(0.0f))
                        .friction(0.01f).rotation(45.0f)
                        .shape(new Shape(objShape)).visible(true).build();
    }

    void updateWorld() {
        testMapView.update();
        testMapView.commit();

        //testObject1.setRotation(testObject1.getRotation()+10.0f);
        testObject1.update();
        testObject2.update();

        Engine2D.GetInstance().getCollisionDetector().updateCollision(testObject1, testObject2);

        testObject1.commit();
        testObject2.commit();

        Rect viewport = Engine2D.GetInstance().getViewport();
        //viewport.offset(1, 1);
        Engine2D.GetInstance().setViewport(viewport);
    }

    private boolean isRunning;
}
