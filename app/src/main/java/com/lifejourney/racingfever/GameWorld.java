package com.lifejourney.racingfever;

import android.view.MotionEvent;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.CollisionDetector;
import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Shape;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Vector2D;
import com.lifejourney.engine2d.World;

public class GameWorld {

    static final String LOG_TAG = "GameWorld";

    public GameWorld() {
        testTrackData = new TrackData("maps/istanbul-park.png");
        testTrackView = new TrackView(testTrackData);
        testTrackView.show();

        world = new World(testTrackView.getSize());
        world.addMainView(testTrackView);

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
        world.addObject(testObject1);

        testObject2 =
                new CollidableObject.Builder<>(new PointF(500, 500))
                        .size(objSize).depth(1.0f).asset("car1.png")
                        .velocity(new Vector2D(45.0f).multiply(0.0f))
                        .friction(0.01f).rotation(45.0f)
                        .shape(new Shape(objShape)).visible(true).build();
        world.addObject(testObject2);

        testObject3 =
                new CollidableObject.Builder<>(new PointF(800, 500))
                        .size(objSize).depth(1.0f).asset("awesomeface.png")
                        .velocity(new Vector2D(45.0f).multiply(0.0f))
                        .friction(0.01f)
                        .shape(new Shape(15.0f*scale)).visible(true).build();
        world.addObject(testObject3);

        testObject4 =
                new CollidableObject.Builder<>(new PointF(1000, 530))
                        .size(objSize).depth(1.0f).asset("awesomeface.png")
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
                testObject1.setPosition(new PointF(newXY[0], newXY[1]));
                testObject1.setVelocity(new Vector2D(225.0f).multiply(10.0f));

                testObject2.setPosition(new PointF(500, 500));
                testObject2.setVelocity(new Vector2D(45.0f).multiply(5.0f));

                testObject3.setPosition(new PointF(800, 500));
                testObject3.setVelocity(new Vector2D(90.0f).multiply(1.0f));

                testObject4.setPosition(new PointF(1000, 540));
                testObject4.setVelocity(new Vector2D(270.0f).multiply(1.0f));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    public void update() {
        world.update();

        testObject3.update();
        testObject4.update();
    }

    public void commit() {
        world.commit();

        testObject3.commit();
        testObject4.commit();
    }

    private World world;

    // to be deleted
    private CollidableObject testObject1, testObject2, testObject3, testObject4;
    private TrackData testTrackData;
    private TrackView testTrackView;
}
