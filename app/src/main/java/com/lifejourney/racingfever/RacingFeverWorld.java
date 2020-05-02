package com.lifejourney.racingfever;

import android.util.Log;
import android.view.MotionEvent;

public class RacingFeverWorld extends World {

    static final String LOG_TAG = "RacingFeverWorld";

    @Override
    public void init() {
        testTrackData = new TrackData("maps/istanbul-park.png");
        testTrackView = new TrackView(testTrackData);
        testTrackView.show();

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

        testObject3 =
                new CollidableObject.Builder<>(new PointF(800, 500))
                        .size(objSize).depth(1.0f).asset("awesomeface.png")
                        .velocity(new Vector2D(45.0f).multiply(0.0f))
                        .friction(0.01f)
                        .shape(new Shape(15.0f*scale)).visible(true).build();

        testObject4 =
                new CollidableObject.Builder<>(new PointF(1000, 530))
                        .size(objSize).depth(1.0f).asset("awesomeface.png")
                        .velocity(new Vector2D(270.0f).multiply(0.0f))
                        .friction(0.01f)
                        .shape(new Shape(15.0f*scale)).visible(true).build();
    }

    @Override
    protected void updateView() {
        testTrackView.update();
    }

    @Override
    protected void updateObjects() {
        Log.e(LOG_TAG, "TTTT");

        testObject1.update();
        testObject2.update();
        testObject3.update();
        testObject4.update();

        CollisionDetector collisionDetector = Engine2D.GetInstance().getCollisionDetector();
        collisionDetector.updateCollision(testObject1, testObject2);
        collisionDetector.updateCollision(testObject1, testObject3);
        collisionDetector.updateCollision(testObject3, testObject4);
    }

    @Override
    protected void commit() {
        testTrackView.commit();
        testObject1.commit();
        testObject2.commit();
        testObject3.commit();
        testObject4.commit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);

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

                testObject3.setPosition(new PointF(800, 500));
                testObject3.setVelocity(new Vector2D(90.0f).multiply(1.0f));

                testObject4.setPosition(new PointF(1000, 540));
                testObject4.setVelocity(new Vector2D(270.0f).multiply(3.0f));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    // to be deleted
    private CollidableObject testObject1, testObject2, testObject3, testObject4;
    private TrackData testTrackData;
    private TrackView testTrackView;
}
