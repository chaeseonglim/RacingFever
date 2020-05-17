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
        DEFENSIVE_DRIVING,
        AGGRESSIVE_DRIVING,
        EMERGENCY_ESCAPING,
        OVERTAKING;

        private int maxStayingTime;
    }

    public void ride(Car car) {
        this.myCar = car;
    }

    public void learn(Track track) {
        this.track = track;
    }

    public void start() {
        state = State.CRUISING;
        setWaypointToTarget(currentTargetWaypointIndex);
    }

    public void stop() {
    }

    @Override
    public int compareTo(Driver other) {
        if (other == this) {
            return 0;
        }
        else {
            if (other.lastWaypointPassedIndex > lastWaypointPassedIndex) {
                return -1;
            }
            else if (other.lastWaypointPassedIndex < lastWaypointPassedIndex) {
                return 1;
            }
            else {
                int waypointCount = track.getPath(currentPathSelection).size();
                int nextPassingWaypointIndex = (lastWaypointPassedIndex + 1) % waypointCount;
                PointF nextWaypointPt = getWaypointRegion(nextPassingWaypointIndex).center();

                float distanceSqToNextWaypointForOther =
                        other.myCar.getPosition().distanceSq(nextWaypointPt);
                float distanceSqToNextWaypointForThis =
                        myCar.getPosition().distanceSq(nextWaypointPt);

                return Float.compare(distanceSqToNextWaypointForThis,
                        distanceSqToNextWaypointForOther);
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
        int totaNumberOfWaypoints = track.getPath(currentPathSelection).size();

        return Math.min(Math.abs(waypoint1-waypoint2),
                Math.abs(totaNumberOfWaypoints-Math.max(waypoint1, waypoint2)+
                        Math.min(waypoint1, waypoint2)));
    }

    private void updateLastPassedWaypoint() {
        int totaNumberOfWaypoints = track.getPath(currentPathSelection).size();
        int numberOfWaypointsToTest;
        if (currentTargetWaypointIndex < lastWaypointPassedIndex) {
            numberOfWaypointsToTest = totaNumberOfWaypoints -
                    lastWaypointPassedIndex + currentTargetWaypointIndex + 1;
        }
        else {
            numberOfWaypointsToTest = currentTargetWaypointIndex - lastWaypointPassedIndex + 1;
        }

        PointF position = myCar.getPosition();
        float nearestDistance = Float.MAX_VALUE;
        int nearestWaypointIndex = -1;
        for (int i = numberOfWaypointsToTest - 1; i >= 0; --i) {
            int currentWaypointIndex = (lastWaypointPassedIndex + i) % totaNumberOfWaypoints;
            RectF region = getWaypointRegion(currentWaypointIndex);

            if (region.includes(position)) {
                lastWaypointPassedIndex = currentWaypointIndex;
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
            lastWaypointPassedIndex = nearestWaypointIndex;
        }
    }

    private int findSuitableWaypointForNewTarget() {
        ArrayList<Integer> candidatesWaypoints = new ArrayList<>();

        if (getIndexDistanceBetweenWaypoints(lastWaypointPassedIndex, currentTargetWaypointIndex) >
            MAX_WAYPOINT_SEARCH_RANGE) {
            return -1;
        }

        // Search through waypoints
        int waypointCount = track.getPath(currentPathSelection).size();
        for (int i = 1; i <= MAX_WAYPOINT_SEARCH_RANGE; ++i) {
            candidatesWaypoints.add((lastWaypointPassedIndex + i) % waypointCount);
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
            int waypointCount = track.getPath(currentPathSelection).size();
            newWaypoint = (lastWaypointPassedIndex + 1) % waypointCount;
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
        currentTargetWaypointIndex = waypointIndex;

        //Log.e(LOG_TAG, name + " currentTargetWaypointIndex: " + currentTargetWaypointIndex +
        //        " " + lastPassedWaypointIndex);
    }

    private RectF getWaypointRegion(int waypointIndex) {
        Point targetMap = track.getPath(currentPathSelection).get(waypointIndex);
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
            if (lastWaypointPassedIndex == currentTargetWaypointIndex) {
                // Go to next waypoint
                setNewWaypointToTarget();
            }
            else {
                // Go to next waypoint only if possible
                if (nextWaypointSearchTimeLeft <= 0) {
                    setNewWaypointToTargetOnlyIfSuitable();
                    nextWaypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;
                }
                else {
                    nextWaypointSearchTimeLeft--;
                }
            }
        }
    }

    private void driveAlongWay(float weight) {
        myCar.seek(targetRegion.center(), weight);
    }

    private void transition(State state) {
        this.state = state;
        if (state == State.DEFENSIVE_DRIVING) {
            defenseDrivingTimeLeft = DEFENSE_DRIVING_DURATION;
            defenseDrivingStayingTime = 0;
        }
        else if (state == State.EMERGENCY_ESCAPING) {
            bulldozerDrivingTimeLeft = BULLDOZER_DRIVING_DURATION;
        }
        else if (state == State.OVERTAKING) {
            overtakingTimeLeft = OVERTAKING_DURATION;
        }
    }

    private void avoidObstacles() {
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
            transition(State.DEFENSIVE_DRIVING);
        }
        else {
            defenseDrivingTimeLeft--;
        }
    }

    private void onCruising() {
        currentPathSelection = Track.PathSelection.OPTIMAL_PATH;

        // Drive to the target waypoint
        driveAlongWay(1.0f);

        // Avoid collision
        avoidObstacles();

        /*
        if (Math.random() < OVERTAKING_POSSIBILITY) {
            transitionTo(State.OVERTAKING);
        }
         */
    }

    private void onDefensiveDriving() {
        currentPathSelection = Track.PathSelection.OPTIMAL_PATH;

        // Drive to the target waypoint
        driveAlongWay(0.3f);

        // Avoid collision
        avoidObstacles();

        if (defenseDrivingTimeLeft == 0) {
            transition(State.CRUISING);
        }
        else {
            defenseDrivingStayingTime++;
        }

        // if we stay here too long, let's go to bulldozer mode
        if (defenseDrivingStayingTime > DEFENSE_DRIVING_STAYING_LIMIT &&
            myCar.getVelocity().length() < BULLDOZER_STATE_TRIGGER_VELOCITY) {
            transition(State.EMERGENCY_ESCAPING);
        }
    }

    private void stateBulldozerDriving() {
        currentPathSelection = Track.PathSelection.OPTIMAL_PATH;

        driveAlongWay(1.3f);

        bulldozerDrivingTimeLeft--;
        if (bulldozerDrivingTimeLeft == 0) {
            transition(State.DEFENSIVE_DRIVING);
        }
    }

    private void stateOvertaking() {
        currentPathSelection = Track.PathSelection.LEFT_BOUNDARY_PATH;

        // Go faster
        driveAlongWay(2.0f);

        // Avoid collision
        avoidObstacles();

        overtakingTimeLeft--;
        if (overtakingTimeLeft == 0) {
            transition(State.CRUISING);
        }
    }

    private void drive() {
        updateWaypoint();

        if (targetRegion == null) {
            return;
        }

        RectF lastPassedRegion = getWaypointRegion(lastWaypointPassedIndex);
        RectF targetRegion = getWaypointRegion(currentTargetWaypointIndex);
        if (waypointLine == null) {
            waypointLine = new Line.Builder(myCar.getPosition(), targetRegion.center())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            lastPassedWaypointLineL = new Line.Builder(lastPassedRegion.topLeft(), lastPassedRegion.bottomLeft())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            lastPassedWaypointLineR = new Line.Builder(lastPassedRegion.topRight(), lastPassedRegion.bottomRight())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            lastPassedWaypointLineT = new Line.Builder(lastPassedRegion.topLeft(), lastPassedRegion.topRight())
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
            lastPassedWaypointLineB = new Line.Builder(lastPassedRegion.bottomLeft(), lastPassedRegion.bottomRight())
                    .color(0.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
            targetWaypointLineL= new Line.Builder(targetRegion.topLeft(), targetRegion.bottomLeft())
                    .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
            targetWaypointLineR = new Line.Builder(targetRegion.topRight(), targetRegion.bottomRight())
                    .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
            targetWaypointLineT = new Line.Builder(targetRegion.topLeft(), targetRegion.topRight())
                    .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
            targetWaypointLineB = new Line.Builder(targetRegion.bottomLeft(), targetRegion.bottomRight())
                    .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
        }
        else {
            waypointLine.set(myCar.getPosition(), targetRegion.center());
            lastPassedWaypointLineL.set(lastPassedRegion.topLeft(), lastPassedRegion.bottomLeft());
            lastPassedWaypointLineR.set(lastPassedRegion.topRight(), lastPassedRegion.bottomRight());
            lastPassedWaypointLineT.set(lastPassedRegion.topLeft(), lastPassedRegion.topRight());
            lastPassedWaypointLineB.set(lastPassedRegion.bottomLeft(), lastPassedRegion.bottomRight());
            targetWaypointLineL.set(targetRegion.topLeft(), targetRegion.bottomLeft());
            targetWaypointLineR.set(targetRegion.topRight(), targetRegion.bottomRight());
            targetWaypointLineT.set(targetRegion.topLeft(), targetRegion.topRight());
            targetWaypointLineB.set(targetRegion.bottomLeft(), targetRegion.bottomRight());
        }
        waypointLine.commit();
        lastPassedWaypointLineL.commit();
        lastPassedWaypointLineR.commit();
        lastPassedWaypointLineT.commit();
        lastPassedWaypointLineB.commit();
        targetWaypointLineL.commit();
        targetWaypointLineR.commit();
        targetWaypointLineT.commit();
        targetWaypointLineB.commit();

        if (state == State.CRUISING) {
            onCruising();
        }
        else if (state == State.DEFENSIVE_DRIVING) {
            onDefensiveDriving();
        }
        else if (state == State.EMERGENCY_ESCAPING) {
            stateBulldozerDriving();
        }
        else if (state == State.OVERTAKING) {
            stateOvertaking();
        }
    }

    private final int STARTING_WAYPOINT_INDEX = 30;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 1;
    private final int MAX_WAYPOINT_SEARCH_RANGE = 10;
    private final int DEFENSE_DRIVING_DURATION = 30;
    private final int DEFENSE_DRIVING_STAYING_LIMIT = 100;
    private final float BULLDOZER_STATE_TRIGGER_VELOCITY = 0.5f;
    private final int BULLDOZER_DRIVING_DURATION = 100;
    private final float OVERTAKING_POSSIBILITY = 0.1f;
    private final int OVERTAKING_DURATION = 100;

    private String name;
    private float reflection;

    private Car myCar;
    private Track track;
    private RectF targetRegion;

    private ArrayList<Car> cars;
    private ArrayList<CollidableObject> obstacles;

    // Waypoints
    private int lastWaypointPassedIndex = 0;
    private int currentTargetWaypointIndex = STARTING_WAYPOINT_INDEX;
    private int nextWaypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;
    private Track.PathSelection currentPathSelection = Track.PathSelection.OPTIMAL_PATH;

    // state-machine
    private State state;
    private int defenseDrivingTimeLeft;
    private int defenseDrivingStayingTime;
    private int bulldozerDrivingTimeLeft;
    private int overtakingTimeLeft;

    // debugging
    Line waypointLine;
    Line lastPassedWaypointLineL, lastPassedWaypointLineR, lastPassedWaypointLineT,
            lastPassedWaypointLineB;
    Line targetWaypointLineL, targetWaypointLineR, targetWaypointLineT,
            targetWaypointLineB;
}
