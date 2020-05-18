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
        public Builder(String name) {
            this.name = name;
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

    private enum State {
        STOP(Integer.MAX_VALUE),
        CRUISING(Integer.MAX_VALUE),
        DEFENSIVE_DRIVING(30, CRUISING),
        AGGRESSIVE_DRIVING(30, CRUISING),
        EMERGENCY_ESCAPING(10, CRUISING),
        OVERDRIVING(150, CRUISING);

        State(int maxStayingTime) {
            this.maxStayingTime = maxStayingTime;
            this.returnState = this;
        }

        State(int maxStayingTime, State returnState) {
            this.maxStayingTime = maxStayingTime;
            this.returnState = returnState;
        }

        public int maxStayingTime() {
            return maxStayingTime;
        }

        public State returnState() {
            return returnState;
        }

        private final int maxStayingTime;
        private final State returnState;
    }

    private Driver(Builder builder) {
        name = builder.name;
        obstacles = builder.obstacles;
        cars = builder.cars;
    }

    public void ride(Car car) {
        this.myCar = car;
        this.myCar.setDriver(this);
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
                PointF nextWaypointPt = getWaypointRegion(currentPathSelection,
                        nextPassingWaypointIndex).center();

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
        int waypointCount = track.getPath(currentPathSelection).size();
        int numberOfWaypointsToTest;
        if (currentTargetWaypointIndex < lastWaypointPassedIndex) {
            numberOfWaypointsToTest = waypointCount -
                    lastWaypointPassedIndex + currentTargetWaypointIndex + 1;
        }
        else {
            numberOfWaypointsToTest = currentTargetWaypointIndex - lastWaypointPassedIndex + 1;
        }

        PointF position = myCar.getPosition();
        float nearestDistance = Float.MAX_VALUE;
        int nearestWaypointIndex = -1;
        for (int i = numberOfWaypointsToTest - 1; i >= 0; --i) {
            int currentWaypointIndex = (lastWaypointPassedIndex + i) % waypointCount;
            RectF region = getWaypointRegion(currentPathSelection, currentWaypointIndex);

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

    private int findSuitableWaypointForNewTarget(Track.PathSelection selection, int currentIndex,
                                                 int targetIndex) {
        ArrayList<Integer> candidatesWaypoints = new ArrayList<>();

        if (getIndexDistanceBetweenWaypoints(currentIndex, targetIndex) >
            selection.maxSearchRange()) {
            return -1;
        }

        // Search through waypoints
        int waypointCount = track.getPath(selection).size();
        for (int i = 1; i <= selection.maxSearchRange(); ++i) {
            candidatesWaypoints.add((currentIndex + i) % waypointCount);
        }

        // Raytracing waypoints to find possible one
        Collections.reverse(candidatesWaypoints);
        for (int waypointIndex : candidatesWaypoints) {
            PointF targetCenter = getWaypointRegion(currentPathSelection, waypointIndex).center();
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
        int newWaypoint = findSuitableWaypointForNewTarget(currentPathSelection,
                lastWaypointPassedIndex, currentTargetWaypointIndex);
        if (newWaypoint == -1) {
            int waypointCount = track.getPath(currentPathSelection).size();
            newWaypoint = (lastWaypointPassedIndex + 1) % waypointCount;
        }

        setWaypointToTarget(newWaypoint);
    }

    private void setNewWaypointToTargetOnlyIfSuitable() {
        int newWaypoint = findSuitableWaypointForNewTarget(currentPathSelection,
                lastWaypointPassedIndex, currentTargetWaypointIndex);
        if (newWaypoint != -1) {
            setWaypointToTarget(newWaypoint);
        }
    }

    private void setWaypointToTarget(int waypointIndex) {
        setTargetRegion(getWaypointRegion(currentPathSelection, waypointIndex));
        currentTargetWaypointIndex = waypointIndex;

        //Log.i(LOG_TAG, name + " currentTargetWaypointIndex: " + currentTargetWaypointIndex +
        //        " " + lastWaypointPassedIndex);
    }

    private RectF getWaypointRegion(Track.PathSelection selection, int waypointIndex) {
        Point targetMap = track.getPath(selection).get(waypointIndex);
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

    private void driveAlongTheWay(float weight) {
        myCar.seek(targetRegion.center(), weight);
    }

    private void transition(State state) {
        this.state = state;
        stateStayingTimeLeft = state.maxStayingTime();
        stateStayingTime = 0;
    }

    private ArrayList<CollidableObject> getNeighborObstacles(float frontAngle,
        float maxForwardDistance, float maxBackwardDistance) {
        ArrayList<CollidableObject> neighborObstacles = new ArrayList<>();
        if (obstacles == null) {
            return neighborObstacles;
        }

        Vector2D myPositionVector = myCar.getPositionVector();
        float cosForwardAngle = (float) Math.cos(frontAngle);

        for (CollidableObject obstacle: obstacles) {
            if (obstacle == myCar) {
                continue;
            }

            Vector2D offset = obstacle.getPositionVector().subtract(myPositionVector);
            Vector2D unitOffset = offset.normalize();
            float forwardness = myCar.getForwardVector().dot(unitOffset);
            if (forwardness > cosForwardAngle) {
                if (offset.length() <= maxForwardDistance) {
                    neighborObstacles.add(obstacle);
                }
            }
            else {
                if (offset.length() <= maxBackwardDistance) {
                    neighborObstacles.add(obstacle);
                }
            }
        }

        return neighborObstacles;
    }

    private Car.AvoidingState avoidObstacles() {
        float distanceForOneUpdate = myCar.getVelocity().length() * myCar.getUpdatePeriod();
        float maxForwardDistance = distanceForOneUpdate * 6;
        float maxBackwardDistance = distanceForOneUpdate * 2;
        ArrayList<CollidableObject> neighborObstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        if (neighborObstacles.size() == 0) {
            return Car.AvoidingState.NO_OBSTACLE;
        }

        PointF targetPoint = targetRegion.center();
        Car.AvoidingState state = myCar.avoidObstacles(neighborObstacles, maxForwardDistance, track,
                new Vector2D(targetPoint.x, targetPoint.y).subtract(myCar.getPositionVector()));

        return state;
    }

    private void onCruising() {
        myCar.shapeBoundary.set(1.0f, 1.0f, 1.0f);

        // set to optimal path
        setPathSelection(Track.PathSelection.MIDDLE_PATH);

        // Update waypoint target
        updateWaypoint();

        // Drive to the target waypoint
        driveAlongTheWay(0.7f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles();
        if (state == Car.AvoidingState.AVOIDING ||
            state == Car.AvoidingState.BRAKING) {
            transition(State.DEFENSIVE_DRIVING);
            return;
        }

        // Check if it can go to overdriving state
        if (Math.random() < OVERDRIVING_POSSIBILITY) {
            CollidableObject obstacle = getNearestFrontObstacle();
            if (obstacle == null || obstacle.getVelocity().length() < myCar.getMaxVelocity()) {
                transition(State.OVERDRIVING);
                if (obstacle == null) {
                    setPathSelection(Track.PathSelection.OPTIMAL_PATH);
                } else {
                    setPathSelection(chooseOverDrivingPath());
                }
            }
        }
    }

    private void onDefensiveDriving() {
        myCar.shapeBoundary.set(0.0f, 1.0f, 0.0f);

        // Update waypoint target
        updateWaypoint();

        // Drive to the target waypoint
        driveAlongTheWay(0.3f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles();
        if (state == Car.AvoidingState.NO_OBSTACLE) {
            transition(State.CRUISING);
        }
        else {
            // if we stay here too long, let's go to emergency escaping mode
            if (stateStayingTime > DEFENSIVE_DRIVING_STAYING_LIMIT &&
                    myCar.getVelocity().length() < EMERGENCY_ESCAPING_STATE_VELOCITY_LIMIT) {
                transition(State.EMERGENCY_ESCAPING);
            }
        }
    }

    private void onEmergencyEscaping() {
        myCar.shapeBoundary.set(1.0f, 0.0f, 1.0f);

        // Update waypoint target
        updateWaypoint();

        // Driver to the target waypoint
        driveAlongTheWay(1.0f);
    }

    private void onOverDriving() {
        myCar.shapeBoundary.set(1.0f, 0.0f, 0.0f);

        // If car collided, transition to defensive driving
        if (myCar.isCollided()) {
            transition(State.DEFENSIVE_DRIVING);
            return;
        }

        // Update waypoint target
        updateWaypoint();

        // Go faster
        driveAlongTheWay(1.0f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles();
        if (state != Car.AvoidingState.BRAKING) {
            // Reduce tick counts faster if it takes brake
            tickTransitionTime(OVERDRIVING_PENALTY_ON_BRAKING);
        }
    }

    private float checkBoundaryPathToBeMovable(Track.PathSelection selection,
                                                 ArrayList<CollidableObject> obstacles) {

        // Check next waypoint target on this path
        int currentWaypointIndexOnPath = track.getNearestWaypointIndex(selection,
                myCar.getPosition());
        int newTargetWaypointIndex = findSuitableWaypointForNewTarget(selection,
                currentWaypointIndexOnPath, currentWaypointIndexOnPath);
        if (newTargetWaypointIndex == -1) {
            newTargetWaypointIndex = (currentWaypointIndexOnPath + 1) % track.getPath(selection).size();
        }
        //int newTargetWaypointIndex = (currentWaypointIndexOnPath + 1) % track.getPath(selection).size();
        PointF targetPt = getWaypointRegion(selection, newTargetWaypointIndex).center();

        // Check if obstacle blocks the way
        float targetDistance = targetPt.distance(myCar.getPosition());
        float direction = targetPt.vectorize().subtract(myCar.getPositionVector()).direction();
        float maxDistanceAllowed = Math.min(targetDistance,
                myCar.getVelocity().length() * myCar.getUpdatePeriod() * 2);
        for (CollidableObject obstacle : obstacles) {
            float distance = myCar.checkObstacleCollidability(obstacle, targetDistance, direction);
            if (distance < maxDistanceAllowed) {
                return Float.MAX_VALUE;
            }
        }

        // Check road block
        float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(myCar.getPosition(),
                direction, targetDistance);
        if (distanceToRoadBlock > 0.0f && distanceToRoadBlock < Float.MAX_VALUE) {
            return Float.MAX_VALUE;
        }

        return targetDistance;
    }

    private Track.PathSelection chooseOverDrivingPath() {
        // Get neighbors
        float distanceForOneUpdate = myCar.getVelocity().length() * myCar.getUpdatePeriod();
        float maxForwardDistance = distanceForOneUpdate * 6;
        float maxBackwardDistance = distanceForOneUpdate * 2;
        ArrayList<CollidableObject> neighborObstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        if (neighborObstacles.size() == 0) {
            return Track.PathSelection.OPTIMAL_PATH;
        }

        // Get distance to each boundary path
        float leftBoundaryDistance =
                checkBoundaryPathToBeMovable(Track.PathSelection.LEFT_BOUNDARY_PATH,
                        neighborObstacles);
        float rightBoundaryDistance =
                checkBoundaryPathToBeMovable(Track.PathSelection.RIGHT_BOUNDARY_PATH,
                        neighborObstacles);

        // Decide path
        if (leftBoundaryDistance == Float.MAX_VALUE && rightBoundaryDistance == Float.MAX_VALUE) {
            return Track.PathSelection.OPTIMAL_PATH;
        }
        else if (leftBoundaryDistance == Float.MAX_VALUE) {
            return Track.PathSelection.RIGHT_BOUNDARY_PATH;
        }
        else if (rightBoundaryDistance == Float.MAX_VALUE) {
            return Track.PathSelection.LEFT_BOUNDARY_PATH;
        }
        else {
            int leftBoundaryVehicles = 0, rightBoundaryVehicles = 0;
            for (CollidableObject obstacle: neighborObstacles) {
                if (obstacle instanceof Car) {
                    Car neighborCar = (Car) obstacle;
                    Driver neighborDriver = neighborCar.getDriver();

                    Track.PathSelection selection = neighborDriver.getPathSelection();
                    if (selection == Track.PathSelection.LEFT_BOUNDARY_PATH) {
                        leftBoundaryVehicles++;
                    }
                    else if (selection == Track.PathSelection.RIGHT_BOUNDARY_PATH) {
                        rightBoundaryVehicles++;
                    }
                }
            }
            if (leftBoundaryVehicles < rightBoundaryVehicles) {
                return Track.PathSelection.LEFT_BOUNDARY_PATH;
            }
            else if (leftBoundaryVehicles > rightBoundaryVehicles) {
                return Track.PathSelection.RIGHT_BOUNDARY_PATH;
            }
            else {
                if (leftBoundaryDistance > rightBoundaryDistance) {
                    return Track.PathSelection.RIGHT_BOUNDARY_PATH;
                } else {
                    return Track.PathSelection.LEFT_BOUNDARY_PATH;
                }
            }
        }
    }

    private CollidableObject getNearestFrontObstacle() {
        // Get front obstacles
        float distanceForOneUpdate = myCar.getVelocity().length() * myCar.getUpdatePeriod();
        float maxForwardDistance = distanceForOneUpdate * 4;
        ArrayList<CollidableObject> frontObstacles = getNeighborObstacles(20.0f,
                maxForwardDistance, 0);

        if (frontObstacles.size() == 0) {
            return null;
        }

        // Find nearest one
        float nearestDistance = Float.MAX_VALUE;
        CollidableObject nearestObstacle = null;
        for (CollidableObject obstacle: frontObstacles) {
            float distance = obstacle.getPosition().distance(myCar.getPosition());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestObstacle = obstacle;
            }
        }

        return nearestObstacle;
    }

    private void drive() {
        // Debugging
        RectF lastPassedRegion = getWaypointRegion(currentPathSelection, lastWaypointPassedIndex);
        RectF targetRegion = getWaypointRegion(currentPathSelection, currentTargetWaypointIndex);
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
                    .color(0.0f, 1.0f, 1.0f, 1.0f).visible(true).build();
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

        // Run state machine
        State prevState = state;
        switch (state) {
            case STOP:
                break;
            case CRUISING:
                onCruising();
                break;
            case DEFENSIVE_DRIVING:
                onDefensiveDriving();
                break;
            case AGGRESSIVE_DRIVING:
                break;
            case EMERGENCY_ESCAPING:
                onEmergencyEscaping();
                break;
            case OVERDRIVING:
                onOverDriving();
                break;
        }

        if (state == prevState) {
            tickTransitionTime(1);
            if (stateStayingTimeLeft <= 0) {
                transition(state.returnState());
            }
        }
    }

    private void tickTransitionTime(int tickCount) {
        stateStayingTimeLeft -= tickCount;
        stateStayingTime += tickCount;
    }

    private void setPathSelection(Track.PathSelection selection) {
        if (currentPathSelection != selection) {
            lastWaypointPassedIndex = track.getNearestWaypointIndex(currentPathSelection,
                    myCar.getPosition());
            int newTargetWaypointIndex = findSuitableWaypointForNewTarget(selection,
                    lastWaypointPassedIndex, lastWaypointPassedIndex);
            if (newTargetWaypointIndex == -1) {
                newTargetWaypointIndex = (lastWaypointPassedIndex + 1) % track.getPath(selection).size();
            }
            currentPathSelection = selection;
            currentTargetWaypointIndex = newTargetWaypointIndex;
            setNewWaypointToTarget();
        }
    }

    private Track.PathSelection getPathSelection() {
        return currentPathSelection;
    }

    private final int STARTING_WAYPOINT_INDEX = 30;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 1;
    private final int DEFENSIVE_DRIVING_STAYING_LIMIT = 100;
    private final float EMERGENCY_ESCAPING_STATE_VELOCITY_LIMIT = 2.0f;
    private final float OVERDRIVING_POSSIBILITY = 0.01f;
    private final int OVERDRIVING_PENALTY_ON_BRAKING = 5;

    private String name;
    private Car myCar;
    private Track track;
    private RectF targetRegion;

    private ArrayList<Car> cars;
    private ArrayList<CollidableObject> obstacles;

    // Waypoints
    private int lastWaypointPassedIndex = 0;
    private int currentTargetWaypointIndex = STARTING_WAYPOINT_INDEX;
    private int nextWaypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;
    private Track.PathSelection currentPathSelection = Track.PathSelection.MIDDLE_PATH;

    // state-machine
    private State state;
    private int stateStayingTimeLeft = Integer.MAX_VALUE;
    private int stateStayingTime = 0;

    // debugging
    Line waypointLine;
    Line lastPassedWaypointLineL, lastPassedWaypointLineR, lastPassedWaypointLineT,
            lastPassedWaypointLineB;
    Line targetWaypointLineL, targetWaypointLineR, targetWaypointLineT,
            targetWaypointLineB;
}
