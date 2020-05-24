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
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class GameWorld extends World{

    static final String LOG_TAG = "GameWorld";

    GameWorld(float scale) {
        Track track = new Track("maps/track3.png", scale);
        track.show();

        initCollisionPool(track.getView().getSize());
        addMainView(track.getView());

        cars = new ArrayList<>();
        drivers = new ArrayList<>();
        obstacles = new ArrayList<>();
        int numberOfCars = track.getData().getStartPointCount();
        for (int i = 0; i < numberOfCars; ++i) {
            Point startDataPosition = track.getData().getStartPoint(i);
            Car car = new Car.Builder("Chaeseong"+i,
                    track.getView().getScreenRegionfromTrackCoord(startDataPosition).center(),
                    Car.Type.CAR1).scale(scale).headDirection(270.0f).build();
            cars.add(car);
            obstacles.add(car);

            Driver driver = new Driver.Builder("Chaeseong"+i)
                    .obstacles(obstacles).cars(cars).build();
            driver.ride(car);
            driver.learn(track);
            driver.start();
            drivers.add(driver);

            addObject(car);
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
        addObject(testObject3);
        obstacles.add(testObject3);
    }

    void close() {
        for (Driver driver: drivers) {
            driver.close();
        }
        for (CollidableObject obstacle: obstacles) {
            obstacle.close();
        }

        getMainView().close();

    }

    /**
     *
     * @param event
     * @return
     */
    boolean onTouchEvent(MotionEvent event)
    {
        int eventAction = event.getAction();

        switch (eventAction)
        {
            case MotionEvent.ACTION_DOWN:
                float[] newXY = Engine2D.GetInstance().translateScreenToGameCoord(
                        new float[] {event.getX(), event.getY()});

                testObject3.setPosition(new PointF(newXY[0], newXY[1]));
                testObject3.setVelocity(new Vector2D(90.0f).multiply(2.0f));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    /**
     *
     */
    @Override
    public void preupdate() {
        updateDrivers();
    }

    /**
     *
     */
    @Override
    public void postupdate() {
        updateViewport();
    }

    /**
     *
     */
    private void updateDrivers() {
        // Sort by rank
        Collections.sort(drivers, new Comparator<Driver>() {
            @Override
            public int compare(Driver s1, Driver s2) {
                return s1.compareRanking(s2);
            }
        });

        int rank = 0;
        for (Driver driver : drivers) {
            driver.setRank(rank);
            rank++;
        }

        // Update driver state
        for (Driver driver : drivers) {
            driver.update();
        }
    }

    /**
     *
     */
    private void updateViewport() {
        // Set ego vehicle in center
        if (cars.size() > 0) {
            Rect viewport = Engine2D.GetInstance().getViewport();
            Point egoCarPosition = new Point(cars.get(cars.size()/2).getPosition());
            viewport.offsetTo(egoCarPosition.subtract(viewport.width / 2, viewport.height / 2));
            Engine2D.GetInstance().setViewport(viewport);
        }
    }

    private ArrayList<Driver> drivers;
    private ArrayList<Car> cars;
    private ArrayList<CollidableObject> obstacles;

    // to be deleted
    private CollidableObject testObject3;
}
