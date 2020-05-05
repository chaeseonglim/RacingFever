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

import java.util.ArrayList;

public class GameWorld {

    static final String LOG_TAG = "GameWorld";

    public GameWorld() {
        float scale = 4.0f;

        Track track = new Track("maps/track3.png", scale);
        track.show();

        world = new World(track.getView().getSize());
        world.addMainView(track.getView());

        cars = new ArrayList<>();
        drivers = new ArrayList<>();
        int startPointCount = track.getData().getStartPointCount();
        startPointCount = 1;
        for (int i = 0; i < startPointCount; ++i) {
            Point startDataPosition = track.getData().getStartPoint(i);
            Car car = new Car.Builder(
                    track.getView().getScreenRegionOfMapDataCoord(startDataPosition).exactCenter(),
                    Car.Type.CAR1).scale(scale).headDirection(270.0f).build();
            cars.add(car);

            Driver driver = new Driver.Builder("Chaeseong").reflection(15.0f).build();
            driver.ride(car);
            driver.learn(track);
            driver.start();
            drivers.add(driver);

            world.addObject(car);
        }

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

                testObject3.setPosition(new PointF(newXY[0], newXY[1]));
                testObject3.setVelocity(new Vector2D(90.0f).multiply(5.0f));

                testObject4.setPosition(new PointF(newXY[0] + 100, newXY[1] + 100));
                testObject4.setVelocity(new Vector2D(270.0f).multiply(5.0f));
                testObject4.setRotation(90.0f);
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
        // Update driver
        for (Driver driver: drivers) {
            driver.update();
        }

        // Update world
        world.update();
    }

    private void updateViewport() {
        if (cars.size() > 0) {
            Rect viewport = Engine2D.GetInstance().getViewport();
            viewport.offsetTo(new Point(cars.get(0).getPosition())
                    .subtract(viewport.width / 2, viewport.height / 2));
            Engine2D.GetInstance().setViewport(viewport);
        }
    }

    public void commit() {
        world.commit();
    }

    private World world;
    private ArrayList<Driver> drivers;
    private ArrayList<Car> cars;

    // to be deleted
    private CollidableObject testObject3, testObject4;
}
