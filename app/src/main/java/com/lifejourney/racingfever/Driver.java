package com.lifejourney.racingfever;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.Line;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.RectF;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;
import java.util.Collections;

public class Driver implements Comparable<Driver> {

    private static final String LOG_TAG = "Driver";

    public static class Builder {
        String name;
        ArrayList<CollidableObject> obstacles;
        ArrayList<Car> cars;

        // Optional parameter
        float reflection = 1.0f;

        public Builder(String name) {
            this.name = name;
        }
        public Builder reflection(float reflection) {
            this.reflection = reflection;
            return this;
        }
        public Builder obstacles(ArrayList<CollidableObject> obstacles) {
            this.obstacles = obstacles;
            return this;
        }
        public Builder cars(ArrayList<Car> cars) {
            this.cars = cars;
            return this;
        }
        public Driver build() {
            return new Driver(this);
        }
    }

    private Driver(Builder builder) {
        name = builder.name;
        reflection = builder.reflection;
        obstacles = builder.obstacles;
        cars = builder.cars;
    }

    private enum State {
        STOP,
        CRUISING,
        DEFENSE_DRIVING,
        AGGRESSIVE_DRIVING,
        BULLDOZER_DRIVING,
    }

    public void ride(Car car) {
        this.myCar = car;
    }

    public void learn(Track track) {
        this.track = track;
    }

    public void start() {
        state = State.CRUISING;
        setWaypointToTarget(currentWaypointTargetIndex);
    }

    public void stop() {
    }

    @Override
    public int compareTo(Driver d) {
        if (d == this) {
            return 0;
        }
        else {
            if (d.lastPassedWaypointIndex > lastPassedWaypointIndex) {
                return -1;
            }
            else if (d.lastPassedWaypointIndex < lastPassedWaypointIndex) {
                return 1;
            }
            else {
                int totaNumberOfWaypoints = track.getOptimalPath().size();
                int nextPassingWaypointIndex = (lastPassedWaypointIndex + 1) % totaNumberOfWaypoints;
                PointF nextWaypointCenter = getWaypointRegion(nextPassingWaypointIndex).center();

                float targetDistanceSqOfD = d.myCar.getPosition().distanceSq(nextWaypointCenter);
                float targetDistanceSqOfThis = myCar.getPosition().distanceSq(nextWaypointCenter);

                return Float.compare(targetDistanceSqOfThis, targetDistanceSqOfD);
            }
        }
    }

    public void update() {
        if (myCar == null || !myCar.isUpdatePossible()) {
            return;
        }

        drive();
    }

    public void setTargetRegion(RectF targetRegion) {
        this.targetRegion = targetRegion;
    }

    private boolean checkIfCarArrivesTargetRegion() {
        if (targetRegion == null)
            return false;

        return targetRegion.includes(myCar.getPosition());
    }

    private int getIndexDistanceBetweenWaypoints(int waypoint1, int waypoint2) {
        int totaNumberOfWaypoints = track.getOptimalPath().size();

        return Math.min(Math.abs(waypoint1-waypoint2),
                Math.abs(totaNumberOfWaypoints-Math.max(waypoint1, waypoint2)+
                        Math.min(waypoint1, waypoint2)));
    }

    private void updateLastPassedWaypoint() {
        int totaNumberOfWaypoints = track.getOptimalPath().size();
        int numberOfWaypointsToTest;
        if (currentWaypointTargetIndex < lastPassedWaypointIndex) {
            numberOfWaypointsToTest = totaNumberOfWaypoints -
                            lastPassedWaypointIndex + currentWaypointTargetIndex;
        }
        else {
            numberOfWaypointsToTest = currentWaypointTargetIndex - lastPassedWaypointIndex;
        }

        PointF position = myCar.getPosition();
        float nearestDistance = Float.MAX_VALUE;
        int nearestWaypointIndex = -1;
        for (int i = numberOfWaypointsToTest - 1; i >= 0; --i) {
            int currentWaypointIndex = (lastPassedWaypointIndex + i) % totaNumberOfWaypoints;
            RectF region = getWaypointRegion(currentWaypointIndex);

            if (region.includes(position)) {
                lastPassedWaypointIndex = currentWaypointIndex;
                return;
            }
            else {
                float distance = region.center().distance(position);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestWaypointIndex = currentWaypointIndex;
                }
            }
        }

        if (nearestWaypointIndex != -1) {
            lastPassedWaypointIndex = nearestWaypointIndex;
        }
    }

    private int findSuitableWaypointForNewTarget() {
        ArrayList<Integer> candidatesWaypoints = new ArrayList<>();

        if (getIndexDistanceBetweenWaypoints(lastPassedWaypointIndex, currentWaypointTargetIndex) >
            MAX_WAYPOINT_SEARCH_RANGE) {
            return -1;
        }

        // Search through waypoints
        int waypointCount = track.getOptimalPath().size();
        for (int i = 1; i <= MAX_WAYPOINT_SEARCH_RANGE; ++i) {
            candidatesWaypoints.add((lastPassedWaypointIndex + i) % waypointCount);
        }

        // Raytracing waypoints to find possible one
        Collections.reverse(candidatesWaypoints);
        for (int waypointIndex : candidatesWaypoints) {
            PointF targetCenter = getWaypointRegion(waypointIndex).center();
            float distanceToWaypoint = targetCenter.distance(myCar.getPosition());

            if (track.getNearestDistanceToRoadBlock(myCar.getPosition(), targetCenter) >=
                    distanceToWaypoint) {
                return waypointIndex;
            }
        }

        // Failed to find suitable waypoint index
        return -1;
    }

    private void setNewWaypointToTarget() {
        int newWaypoint = findSuitableWaypointForNewTarget();
        if (newWaypoint == -1) {
            int waypointCount = track.getOptimalPath().size();
            newWaypoint = (lastPassedWaypointIndex + 1) % waypointCount;
        }

        setWaypointToTarget(newWaypoint);
    }

    private void setNewWaypointToTargetOnlyIfSuitable() {
        int newWaypoint = findSuitableWaypointForNewTarget();
        if (newWaypoint != -1) {
            setWaypointToTarget(newWaypoint);
        }
    }

    private void setWaypointToTarget(int waypointIndex) {
        setTargetRegion(getWaypointRegion(waypointIndex));
        currentWaypointTargetIndex = waypointIndex;

        //Log.e(LOG_TAG, name + " currentWaypointIndex: " + currentWaypointTargetIndex +
        //        " " + lastPassedWaypointIndex);
    }

    private RectF getWaypointRegion(int waypointIndex) {
        Point targetMap = track.getOptimalPath().get(waypointIndex);
        return track.getView().getScreenRegionfromTrackCoord(targetMap);
    }

    private void updateWaypoint() {
        // Check if target region is achieved
        if (track == null) {
            if (checkIfCarArrivesTargetRegion())
                targetRegion = null;
        }
        else {
            updateLastPassedWaypoint();
            if (lastPassedWaypointIndex == currentWaypointTargetIndex) {
                // Go to next waypoint
                setNewWaypointToTarget();
            }
            else {
                // Go to next waypoint only if possible
                if (waypointSearchTimeLeft <= 0) {
                    setNewWaypointToTargetOnlyIfSuitable();
                    waypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;
                }
                else {
                    waypointSearchTimeLeft--;
                }
            }
        }
    }

    private void driveThroughWay(float weight) {
        myCar.seek(targetRegion.center(), weight);
    }

    private void transitionTo(State state) {
        this.state = state;
        if (state == State.DEFENSE_DRIVING) {
            defenseDrivingLeft = DEFENSE_DRIVING_DURATION;
            defenseDrivingStayingTime = 0;
        }
        else if (state == State.BULLDOZER_DRIVING) {
            bulldozerDrivingLeft = BULLDOZER_DRIVING_DURATION;
        }
    }

    private void avoidCollisions() {
        float maxFowardDistance = myCar.getVelocity().length() * myCar.getUpdatePeriod() * 6;
        float maxBackwardDistance = myCar.getVelocity().length() * myCar.getUpdatePeriod() * 2;
        ArrayList<CollidableObject> neighborObstacles = new ArrayList<>();
        Vector2D myPositionVector = myCar.getPositionVector();
        float cosForwardAngle = -1.0f; // cos(90')

        if (obstacles != null) {
            for (CollidableObject obstacle : obstacles) {
                if (obstacle == myCar) {
                    continue;
                }

                Vector2D offset = obstacle.getPositionVector().subtract(myPositionVector);
                Vector2D unitOffset = offset.normalize();
                float forwardness = myCar.getForwardVector().dot(unitOffset);
                if (forwardness > cosForwardAngle) {
                    if (offset.length() <= maxFowardDistance) {
                        neighborObstacles.add(obstacle);
                    }
                }
                else {
                    if (offset.length() <= maxBackwardDistance) {
                        neighborObstacles.add(obstacle);
                    }
                }
            }
        }

        PointF targetPoint = targetRegion.center();
        if (myCar.avoidObstacles(neighborObstacles, maxFowardDistance, track,
                new Vector2D(targetPoint.x, targetPoint.y).subtract(myCar.getPositionVector()))) {
            transitionTo(State.DEFENSE_DRIVING);
        }
        else {
            defenseDrivingLeft--;
        }
    }

    private void stateCruising() {
        // Drive to the target waypoint
        driveThroughWay(1.0f);

        // Avoid collision
        avoidCollisions();
    }

    private void stateDefenseDriving() {
        // Drive to the target waypoint
        driveThroughWay(0.3f);

        // Avoid collision
        avoidCollisions();

        if (defenseDrivingLeft == 0) {
            transitionTo(State.CRUISING);
        }
        else {
            defenseDrivingStayingTime++;
        }

        // if we stay here too long, let's go to bulldozer mode
        if (defenseDrivingStayingTime > DEFENSE_DRIVING_STAYING_LIMIT &&
            myCar.getVelocity().length() < BULLDOZER_STATE_TRIGGER_VELOCITY) {
            transitionTo(State.BULLDOZER_DRIVING);
        }
    }

    private void stateBulldozerDriving() {
        driveThroughWay(1.3f);

        bulldozerDrivingLeft--;
        if (bulldozerDrivingLeft == 0) {
            transitionTo(State.DEFENSE_DRIVING);
        }
    }

    private void drive() {
        updateWaypoint();

        if (targetRegion == null) {
            return;
        }

        RectF region = getWaypointRegion(lastPassedWaypointIndex);
        if (waypointLine == null) {
            waypointLine = new Line.Builder(myCar.getPosition(), targetRegion.center())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            waypointLineL = new Line.Builder(region.topLeft(), region.bottomLeft())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            waypointLineR = new Line.Builder(region.topRight(), region.bottomRight())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            waypointLineT = new Line.Builder(region.topLeft(), region.topRight())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            waypointLineB = new Line.Builder(region.bottomLeft(), region.bottomRight())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
        }
        else {
            waypointLine.set(myCar.getPosition(), targetRegion.center());
            waypointLineL.set(region.topLeft(), region.bottomLeft());
            waypointLineR.set(region.topRight(), region.bottomRight());
            waypointLineT.set(region.topLeft(), region.topRight());
            waypointLineB.set(region.bottomLeft(), region.bottomRight());
        }
        waypointLine.commit();
        waypointLineL.commit();
        waypointLineR.commit();
        waypointLineT.commit();
        waypointLineB.commit();


        if (state == State.CRUISING) {
            stateCruising();
        }
        else if (state == State.DEFENSE_DRIVING) {
            stateDefenseDriving();
        }
        else if (state == State.BULLDOZER_DRIVING) {
            stateBulldozerDriving();
        }
    }

    private final int STARTING_WAYPOINT_INDEX = 30;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 1;
    private final int MAX_WAYPOINT_SEARCH_RANGE = 10;
    private final int DEFENSE_DRIVING_DURATION = 30;
    private final int DEFENSE_DRIVING_STAYING_LIMIT = 100;
    private final float BULLDOZER_STATE_TRIGGER_VELOCITY = 0.5f;
    private final int BULLDOZER_DRIVING_DURATION = 100;

    private String name;
    private float reflection;

    private Car myCar;
    private Track track;
    private RectF targetRegion;

    private int lastPassedWaypointIndex = 0;
    private int currentWaypointTargetIndex = STARTING_WAYPOINT_INDEX;
    private int waypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;

    private ArrayList<Car> cars;
    private ArrayList<CollidableObject> obstacles;

    private State state;
    private int defenseDrivingLeft;
    private int defenseDrivingStayingTime;
    private int bulldozerDrivingLeft;

    // debugging
    Line waypointLine;
    Line waypointLineL, waypointLineR, waypointLineT, waypointLineB;
}
