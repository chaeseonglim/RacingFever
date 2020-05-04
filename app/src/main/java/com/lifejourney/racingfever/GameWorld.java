package com.lifejourney.racingfever;

import android.view.MotionEvent;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.Vector2D;
import com.lifejourney.engine2d.World;

public class GameWorld {

    static final String LOG_TAG = "GameWorld";

    public GameWorld() {
        testTrackData = new TrackData("maps/track1.png");
        testTrackView = new TrackView(testTrackData);
        testTrackView.show();

        world = new World(testTrackView.getSize());
        world.addMainView(testTrackView);

        testCar1 = new Car.Builder(new PointF(300, 300), Car.Type.CAR1).build();
        world.addObject(testCar1);

        testCar2 = new Car.Builder(new PointF(500, 500), Car.Type.CAR1).build();
        world.addObject(testCar2);

        testDriver1 = new Driver.Builder("Chaeseong").reflection(6.0f).build();
        testDriver1.ride(testCar1);
        testDriver1.setTargetPosition(testTrackView.getPositionOfMap(117, 96));

        float scale = 3.0f;
        Size objSize = new Size(32, 32).multiply(scale);

        Sprite.Builder awesomeFaceSpriteBuilder =
                new Sprite.Builder("awesomeface.png").size(objSize);
        testObject3 =
                new CollidableObject.Builder<>(new PointF(800, 500))
                        .depth(1.0f).sprite(awesomeFaceSpriteBuilder.build())
                        .velocity(new Vector2D(45.0f).multiply(0.0f))
                        .friction(0.01f)
                        .shape(new Shape(15.0f*scale)).visible(true).build();
        world.addObject(testObject3);

        testObject4 =
                new CollidableObject.Builder<>(new PointF(1000, 530))
                        .depth(1.0f).sprite(awesomeFaceSpriteBuilder.build())
                        .velocity(new Vector2D(270.0f).multiply(0.0f))
                        .friction(0.01f)
                        .shape(new Shape(15.0f*scale)).visible(true).build();
        world.addObject(testObject4);
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        int eventAction = event.getAction();

        switch (eventAction)
        {
            case MotionEvent.ACTION_DOWN:
                float[] newXY = Engine2D.GetInstance().translateScreenToGameCoord(
                        new float[] {event.getX(), event.getY()});
                /*
                testCar1.setPosition(new PointF(newXY[0], newXY[1]));
                testCar1.setVelocity(new Vector2D(45.0f).multiply(10.0f));
                */

                testCar2.setPosition(new PointF(500, 200));
                testCar2.setVelocity(new Vector2D(225.0f).multiply(5.0f));

                testObject3.setPosition(new PointF(800, 500));
                testObject3.setVelocity(new Vector2D(90.0f).multiply(5.0f));

                testObject4.setPosition(new PointF(1000, 440));
                testObject4.setVelocity(new Vector2D(270.0f).multiply(5.0f));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    public void update() {
        updateWorld();
        updateViewport();
    }

    private void updateWorld() {
        // To be deleted
        testDriver1.update();

        world.update();
    }

    private void updateViewport() {
        Rect viewport = Engine2D.GetInstance().getViewport();
        viewport.offsetTo(new Point(testCar1.getPosition()).subtract(viewport.width/2, viewport.height/2));
        Engine2D.GetInstance().setViewport(viewport);
    }

    public void commit() {
        world.commit();
    }

    private World world;

    // to be deleted
    private Car testCar1, testCar2;
    private Driver testDriver1;
    private CollidableObject testObject3, testObject4;
    private TrackData testTrackData;
    private TrackView testTrackView;
}
