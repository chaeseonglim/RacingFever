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

import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.Rect;

import java.util.Locale;

public class RacingFever extends FragmentActivity implements Choreographer.FrameCallback, SurfaceHolder.Callback {

    private static final long ONE_MS_IN_NS = 1000000;
    private static final long ONE_S_IN_NS = 1000 * ONE_MS_IN_NS;

    private static final String LOG_TAG = "RacingFever";

    /**
     *
     */
    protected void initEngine() {
        // Get display metrics
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        float refreshRateHz = display.getRefreshRate();
        Log.i(LOG_TAG, String.format("Refresh rate: %.1f Hz", refreshRateHz));

        // Initialize the surfaceView
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);

        Log.i(LOG_TAG, "Engine initialized: " + Engine2D.GetInstance().isInitialized());

        // Initialize Engine
        Engine2D.GetInstance().initEngine(this);

        // Set resolution of Engine
        Engine2D.GetInstance().setViewport(new Rect(0, 0, 1280, 720));
    }

    /**
     *
     */
    protected void finalizeEngine() {
        // Finalize Engine
        Engine2D.GetInstance().finalizeEngine();

        Log.i(LOG_TAG, "Engine finalized: " + Engine2D.GetInstance().isInitialized());
    }

    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize display & engine
        initEngine();
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");

        super.onDestroy();

        world.close();

        finalizeEngine();

    }

    /**
     *
     */
    protected void onEngine2DPrepared() {
        // Initialize resources
        initResources();
    }

    /**
     *
     */
    @Override
    protected void onStart() {
        Log.i(LOG_TAG, "onStart");

        super.onStart();

        isRunning = true;
        Engine2D.GetInstance().start();
        Choreographer.getInstance().postFrameCallback(this);
    }

    /**
     *
     */
    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "onStop");

        super.onStop();

        isRunning = false;
        Engine2D.GetInstance().stop();
    }

    /**
     *
     * @param frameTimeNanos
     */
    @Override
    public void doFrame(long frameTimeNanos) {
        Trace.beginSection("doFrame");

        TextView fpsView = findViewById(R.id.fps);
        fpsView.setText(String.format(Locale.US, "FPS: %.1f", Engine2D.GetInstance().getAverageFps()));

        if (isRunning) {
            if (isEngine2DSurfacePrepared) {
                // Update world
                world.update();
                world.commit();
            }

            Trace.beginSection("Requesting callback");
            Choreographer.getInstance().postFrameCallback(this);
            Trace.endSection();
        }

        Trace.endSection();
    }

    /**
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.e(LOG_TAG, "Touch");
        return world.onTouchEvent(event);
    }

    /**
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Do nothing here, waiting for surfaceChanged instead
    }

    /**
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Surface surface = holder.getSurface();
        Engine2D.GetInstance().setSurface(surface, width, height);

        if (!isEngine2DSurfacePrepared) {
            onEngine2DPrepared();
            isEngine2DSurfacePrepared = true;
        }
    }

    /**
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Engine2D.GetInstance().clearSurface();
    }

    /**
     *
     */
    protected void initResources() {
        world = new GameWorld(3.0f);
    }

    private GameWorld world;
    private boolean isRunning;
    private boolean isEngine2DSurfacePrepared = false;
}
