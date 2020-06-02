package com.lifejourney.racingfever;

import android.util.Log;
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
import java.util.Random;

public class GameWorld extends World{

    static final String LOG_TAG = "GameWorld";

    GameWorld() {
        random = new Random();

        Track track = new Track("maps/Track3.png", 3.0f);
        track.show();

        initCollisionPool(track.getView().getSize());
        addMainView(track.getView());

        cars = new ArrayList<>();
        drivers = new ArrayList<>();
        obstacles = new ArrayList<>();
        int numberOfCars = track.getData().getStartPointCount();
        for (int i = 0; i < numberOfCars; ++i) {
            Point startDataPosition = track.getData().getStartPoint(i);
            Car.Type type = Car.Type.values()[random.nextInt(Car.Type.values().length)];
            Car car = new Car.Builder("Chaeseong"+i,
                    track.getView().getScreenRegionfromTrackCoord(startDataPosition).center(),
                    type).scale(1.5f).headDirection(0.0f).build();
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

        Size objSize = new Size(32, 32).multiply(1.5f);
        Sprite.Builder awesomeFaceSpriteBuilder =
                new Sprite.Builder("awesomeface.png").size(objSize);
/*
        testObject =
                new CollidableObject.Builder<>(new PointF(800, 500))
                        .depth(1.0f).sprite(awesomeFaceSpriteBuilder.build())
                        .velocity(new Vector2D(45.0f).multiply(0.0f))
                        .friction(0.01f)
                        .shape(new Shape(15.0f*1.5f)).visible(true).build();
        addObject(testObject);
        obstacles.add(testObject);

        dummyCar = new Car.Builder("Chaeseongk", new PointF(), Car.Type.MARTOZ)
                .scale(1.5f).headDirection(270.0f).build();
        addObject(dummyCar);
        obstacles.add(dummyCar);
 */
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

                //dummyCar.setPosition(new PointF(newXY[0], newXY[1]));
                //dummyCar.setVelocity(new Vector2D(90.0f).multiply(2.0f));
                if (cars.get(cars.size()/2).getDriver() != null) {
                    cars.get(cars.size() / 2).getDriver().ride(null);
                    cars.get(cars.size() / 2).setDriver(null);
                    cars.get(cars.size() / 2).stop();
                }
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
        // Sort drivers by rank
        Collections.sort(drivers, new Comparator<Driver>() {
            @Override
            public int compare(Driver s1, Driver s2) {
                return s1.compareRanking(s2);
            }
        });

        // Set rank and rank effect to driver
        int rank = 0;
        for (Driver driver : drivers) {
            driver.setRank(rank);
            /*
            Log.i(LOG_TAG, driver.getName() + " lap: " + driver.getLap() +
                    " rank: " + driver.getRank() + " lastWaypointPassedIndex " +
                    driver.getLastWaypointPassedIndex());
             */
            float modifier = 1.0f + rank*(0.1f/8);
            driver.addEffect(new Effect.Builder("rank", 1)
                    .modifierCarGeneral(modifier).build());
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
    private Random random;

    // to be deleted
    private CollidableObject testObject;
    private Car dummyCar;
}
