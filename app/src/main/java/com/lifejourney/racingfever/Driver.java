package com.lifejourney.racingfever;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.lifejourney.engine2d.CollidableObject;
import com.lifejourney.engine2d.Line;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.RectF;
import com.lifejourney.engine2d.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class Driver implements Comparable<Driver> {

    private static final String LOG_TAG = "Driver";

    public static class Builder {
        String name;
        ArrayList<CollidableObject> obstacles;
        ArrayList<Car> cars;

        // Optional parameter
        Builder(String name) {
            this.name = name;
        }
        Builder obstacles(ArrayList<CollidableObject> obstacles) {
            this.obstacles = obstacles;
            return this;
        }
        Builder cars(ArrayList<Car> cars) {
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
        DEFENSIVE_DRIVING(50, CRUISING),
        AGGRESSIVE_DRIVING(20, CRUISING),
        EMERGENCY_ESCAPING(10, CRUISING),
        OVERTAKING(20, CRUISING);

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
        effects = new ArrayList<>();

        lastWaypointPassedIndex = 0;
        targetWaypointIndex = STARTING_WAYPOINT_INDEX;
        nextWaypointSearchTimeLeft = MIN_WAYPOINT_SEARCH_PERIOD;
        stayingTimeLeftOnState = Integer.MAX_VALUE;
        stayingTimeOnState = 0;
        lap = 0;
        rank = 0;
        finishLineCheckerDone = false;
        modifierDriverGeneral = 1.0f;
    }

    /**
     *
     */
    public void close() {
        if (waypointLine != null) {
            waypointLine.close();
        }
        if (lastPassedWaypointLineL != null) {
            lastPassedWaypointLineL.close();
            lastPassedWaypointLineR.close();
            lastPassedWaypointLineT.close();
            lastPassedWaypointLineB.close();
        }
        if (targetWaypointLineL != null) {
            targetWaypointLineL.close();
            targetWaypointLineR.close();
            targetWaypointLineT.close();
            targetWaypointLineB.close();
        }
    }

    /**
     *
     */
    public void update() {
        if (myCar == null) {
            return;
        }

        // Apply effects
        applyEffects();

        // Checking if the car passes the finish line
        if (!finishLineCheckerDone && checkFinishLinePassing()) {
            lap++;
            lastWaypointPassedIndex = 0;
            if (targetWaypointIndex > 20) {
                targetWaypointIndex = 1;
            }
            /*
            Log.i(LOG_TAG, name + " lap: " + lap + " rank: " + rank +
                    " lastWaypointPassedIndex " + lastWaypointPassedIndex +
                    " " + targetWaypointIndex);
             */
            finishLineCheckerDone = true;
        }
        else if (finishLineCheckerDone && lastWaypointPassedIndex > 10 &&
                lastWaypointPassedIndex < 20) {
            finishLineCheckerDone = false;
        }

        if (!myCar.isUpdatePossible()) {
            return;
        }

        // Driver the car
        drive();
    }

    /**
     *
     * @param car
     */
    void ride(Car car) {
        myCar = car;
        if (myCar != null) {
            myCar.setDriver(this);
            if (track != null) {
                laneSelection =
                        track.getNearestLaneFromCurrentPosition(lastWaypointPassedIndex,
                                myCar.getPosition());
            }
        }
    }

    /**
     *
     * @param track
     */
    void learn(Track track) {
        this.track = track;
        if (myCar != null) {
            laneSelection =
                    track.getNearestLaneFromCurrentPosition(lastWaypointPassedIndex,
                            myCar.getPosition());
        }
    }

    /**
     *
     */
    void start() {
        state = State.CRUISING;
        setWaypointToTarget(targetWaypointIndex);
    }

    /**
     *
     */
    void stop() {
        myCar.stop();
    }

    /**
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(@NonNull Driver other) {
        if (other == this) {
            return 0;
        }
        else {
            return comparePositionAhead(other);
        }
    }

    /**
     *
     * @param other
     * @return
     */
    int compareRanking(Driver other) {
        if (other.getLap() < this.getLap()) {
            return -1;
        }
        else if (other.getLap() > this.getLap()) {
            return 1;
        }
        else {
            return comparePositionAhead(other);
        }
    }

    /**
     *
     * @param other
     * @return
     */
    private int comparePositionAhead(Driver other) {
        int otherLastWaypointIndex = other.lastWaypointPassedIndex;
        int thisLastWaypointIndex = this.lastWaypointPassedIndex;

        if (myCar == null) {
            return -1;
        }
        else if (other.myCar == null) {
            return 1;
        }
        else if (otherLastWaypointIndex < thisLastWaypointIndex) {
            return -1;
        }
        else if (otherLastWaypointIndex > thisLastWaypointIndex) {
            return 1;
        }
        else {
            int waypointCount = track.getLane(Track.LaneSelection.INVALID_LANE).size();
            int nextPassingWaypointIndex = (thisLastWaypointIndex + 1) % waypointCount;
            PointF nextWaypointPt = track.getWaypointRegion(Track.LaneSelection.INVALID_LANE,
                    nextPassingWaypointIndex).center();

            float distanceSqToNextWaypointForOther =
                    other.myCar.getPosition().distanceSq(nextWaypointPt);
            float distanceSqToNextWaypointForThis =
                    this.myCar.getPosition().distanceSq(nextWaypointPt);

            return Float.compare(distanceSqToNextWaypointForThis,
                    distanceSqToNextWaypointForOther);
        }
    }

    /**
     *
     * @return
     */
    private boolean checkFinishLinePassing() {
        for (int i = 0; i < track.getData().getFinishPointCount(); ++i) {
            RectF finishLineRegion =
                    track.getView().getScreenRegionfromTrackCoord(track.getData().getFinishPoint(i));
            if (finishLineRegion.includes(myCar.getPosition())) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param targetRegion
     */
    private void setTargetRegion(RectF targetRegion) {
        this.targetRegion = targetRegion;
    }

    /**
     *
     * @return
     */
    private boolean checkIfCarArrivesTargetRegion() {
        if (targetRegion == null)
            return false;

        return targetRegion.includes(myCar.getPosition());
    }

    /**
     *
     */
    private void updateLastPassedWaypoint() {
        int waypointCount = track.getLane(laneSelection).size();
        int numberOfWaypointsToTest;
        if (targetWaypointIndex < lastWaypointPassedIndex) {
            numberOfWaypointsToTest = waypointCount -
                    lastWaypointPassedIndex + targetWaypointIndex + 1;
        }
        else {
            numberOfWaypointsToTest = targetWaypointIndex - lastWaypointPassedIndex + 1;
        }

        PointF position = myCar.getPosition();
        float nearestDistance = Float.MAX_VALUE;
        int nearestWaypointIndex = -1;
        for (int i = numberOfWaypointsToTest - 1; i >= 0; --i) {
            int currentWaypointIndex = (lastWaypointPassedIndex + i) % waypointCount;
            RectF region = track.getWaypointRegion(laneSelection, currentWaypointIndex);

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

    /**
     *
     * @param laneSelection
     * @param currentIndex
     * @param targetIndex
     * @param maxSearchableScore
     * @return
     */
    private int findSuitableWaypointForNewTarget(Track.LaneSelection laneSelection,
                                                 int currentIndex,
                                                 int targetIndex,
                                                 int maxSearchableScore) {
        ArrayList<Integer> candidatesWaypoints = new ArrayList<>();

        int maxSearchableCount =
                track.getWaypointCountWhichCanBeSearched(laneSelection, currentIndex, maxSearchableScore);

        if (track.getDistanceBetweenWaypointIndex(laneSelection, currentIndex, targetIndex) >
                maxSearchableCount) {
            return -1;
        }

        // Search through waypoints
        int waypointCount = track.getLane(laneSelection).size();
        for (int i = 1; i < maxSearchableCount; ++i) {
            candidatesWaypoints.add((currentIndex + i) % waypointCount);
        }

        // Raytracing waypoints to find possible one
        Collections.reverse(candidatesWaypoints);
        for (int waypointIndex : candidatesWaypoints) {
            PointF targetCenter = track.getWaypointRegion(laneSelection, waypointIndex).center();
            float distanceToWaypoint = targetCenter.distance(myCar.getPosition());

            if (track.getNearestDistanceToRoadBlock(myCar.getPosition(), targetCenter) >=
                    distanceToWaypoint) {
                return waypointIndex;
            }
        }

        // Failed to find suitable waypoint index
        return -1;
    }

    /**
     *
     */
    private void setNewWaypointToTarget() {
        int newWaypoint = findSuitableWaypointForNewTarget(laneSelection,
                lastWaypointPassedIndex, targetWaypointIndex, 0);
        if (newWaypoint == -1) {
            newWaypoint = track.findNextValidWaypoint(laneSelection, lastWaypointPassedIndex);
        }

        setWaypointToTarget(newWaypoint);
    }

    /**
     *
     */
    private void setNewWaypointToTargetOnlyIfSuitable() {
        int newWaypoint = findSuitableWaypointForNewTarget(laneSelection,
                lastWaypointPassedIndex, targetWaypointIndex, 0);
        if (newWaypoint != -1) {
            setWaypointToTarget(newWaypoint);
        }
    }

    /**
     *
     * @param waypointIndex
     */
    private void setWaypointToTarget(int waypointIndex) {
        setTargetRegion(track.getWaypointRegion(laneSelection, waypointIndex));
        targetWaypointIndex = waypointIndex;

        /*
        Log.i(LOG_TAG, name + " " + currentLaneSelection.ordinal() +
                " currentTargetWaypointIndex: " + currentTargetWaypointIndex +
                " " + lastWaypointPassedIndex);
         */
    }

    /**
     *
     */
    private void updateWaypoint() {
        // Check if target region is achieved
        if (track == null) {
            if (checkIfCarArrivesTargetRegion())
                targetRegion = null;
        }
        else {
            updateLastPassedWaypoint();
            if (lastWaypointPassedIndex == targetWaypointIndex) {
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

    /**
     *
     * @param weight
     */
    private void driveAlongTheWay(float weight) {
        weight += (Math.random()%0.2f) - 0.1f;
        myCar.seek(targetRegion.center(), weight);
    }

    /**
     *
     * @param frontAngle
     * @param maxForwardDistance
     * @param maxBackwardDistance
     * @return
     */
    private ArrayList<CollidableObject> getNeighborObstacles(float frontAngle,
                                                             float maxForwardDistance,
                                                             float maxBackwardDistance) {

        ArrayList<CollidableObject> neighborObstacles = new ArrayList<>();

        if (obstacles == null) {
            return neighborObstacles;
        }

        Vector2D myPositionVector = myCar.getPositionVector();

        for (CollidableObject obstacle: obstacles) {
            if (obstacle == myCar) {
                continue;
            }

            Vector2D offset = obstacle.getPositionVector().subtract(myPositionVector);
            Vector2D unitOffset = offset.clone().normalize();
            if (myCar.getForwardVector().angle(unitOffset) < frontAngle) {
                if (offset.length() <= maxForwardDistance) {
                    neighborObstacles.add(obstacle);
                    if (frontAngle == 10.0f) {
                        Log.e(LOG_TAG, frontAngle + " " + offset.length() + " " + maxForwardDistance);
                    }
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

    /**
     *
     * @return
     */
    private Car.AvoidingState avoidObstacles(float avoidingPossibility, float brakingPossibility) {
        float distanceUnit = myCar.getShape().getRadius() * 2; // myCar.getMovingDistanceForOneUpdate();
        float maxForwardDistance = distanceUnit * 3; // myCar.getMovingDistanceForOneUpdate() * 6;
        float maxBackwardDistance = 0;
        ArrayList<CollidableObject> neighborObstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        if (neighborObstacles.size() == 0) {
            return Car.AvoidingState.NO_OBSTACLE;
        }

        return myCar.avoidObstacles(neighborObstacles, maxForwardDistance,
                maxBackwardDistance, track, avoidingPossibility, brakingPossibility);
    }

    /**
     *
     */
    private boolean tryOvertaking() {
        if (rank == 0) {
            return false;
        }

        // Check if it can go to overtaking state
        float overDrivingPossibility = OVERTAKING_ENTER_POSSIBILITY;
        float maxDistance = myCar.getShape().getRadius() * 2 * 4; //myCar.getMovingDistanceForOneUpdate() * 5;
        CollidableObject frontObstacle = getNearestFrontObstacle(maxDistance);
        if (frontObstacle == null) {
            overDrivingPossibility += OVERTAKING_ENTER_POSSIBILITY;
        } else {
            if (frontObstacle instanceof Car) {
                Car frontCar = (Car) frontObstacle;
                if (frontCar.getVelocity().length() < myCar.getMaxVelocity()) {
                    overDrivingPossibility += OVERTAKING_ENTER_POSSIBILITY;
                }
                if (frontCar.getEnginePower() / frontCar.getMass() <
                        myCar.getEnginePower() / myCar.getMass()){
                    overDrivingPossibility += OVERTAKING_ENTER_POSSIBILITY;
                }
            }
        }

        if (Math.random() < overDrivingPossibility) {
            if (frontObstacle == null) {
                transition(State.OVERTAKING);
            } else {
                Track.LaneSelection laneSelection = chooseAlternativeLane();
                if (laneSelection != Track.LaneSelection.INVALID_LANE) {
                    transition(State.OVERTAKING);
                    setLaneSelection(laneSelection);
                }
            }
        }

        return (state == State.OVERTAKING);
    }

    /**
     *
     * @param keepWeight
     */
    private boolean keepDistanceFromFrontVehicle(float keepWeight) {
        float maxDistance = myCar.getShape().getRadius() * 2 * 2; // myCar.getMovingDistanceForOneUpdate() * 6;
        CollidableObject frontObstacle = getNearestFrontObstacle(maxDistance);
        if (frontObstacle instanceof Car) {
            float currentVelocity = myCar.getVelocity().length();
            float obstacleVelocity = frontObstacle.getVelocity().dot(myCar.getForwardVector());
            float velocityLimit = obstacleVelocity * keepWeight;
            myCar.getVelocity().truncate(velocityLimit);
            if (currentVelocity < obstacleVelocity) {
                float forceLimit = frontObstacle.getForce().dot(myCar.getForwardVector()) * keepWeight;
                myCar.getForce().truncate(forceLimit);
            }
            return true;
        }

        return false;
    }

    /**
     *
     * @param state
     */
    private void transition(State state) {
        this.state = state;
        stayingTimeLeftOnState = state.maxStayingTime();
        stayingTimeOnState = 0;
    }

    /**
     *
     */
    private void onCruising() {
        if (myCar.isDebugMode()) {
            myCar.circleShape.setColor(1.0f, 1.0f, 1.0f);
        }

        // Update waypoint target
        updateWaypoint();

        // Drive to the target waypoint
        driveAlongTheWay(0.8f);

        // Keep distance with front vehicle
        keepDistanceFromFrontVehicle(0.8f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles(1.0f, 0.0f);
        if (state == Car.AvoidingState.AVOIDING ||
            state == Car.AvoidingState.BRAKING) {
            boolean goLeft = targetRegion.center().vectorize().ccw(myCar.getForce()) <= 0.0f;
            setLaneSelection(chooseAlternativeLane(goLeft));
        }

        tryOvertaking();
    }

    /**
     *
     */
    private void onDefensiveDriving() {
        if (myCar.isDebugMode()) {
            myCar.circleShape.setColor(0.0f, 1.0f, 0.0f);
        }

        // Update waypoint target
        updateWaypoint();

        // Drive to the target waypoint
        driveAlongTheWay(0.6f);

        // Keep distance with front vehicle
        keepDistanceFromFrontVehicle(0.7f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles(1.0f, 1.0f);

        if (state == Car.AvoidingState.NO_OBSTACLE) {
            tickTransitionTime(DEFENSIVE_DRIVING_RELEASE_TICKCOUNT);
        }
        else {
            // Take alternative lane
            setLaneSelection(chooseAlternativeLane());

            // If it's too slow downed, let's go to emergency escaping mode
            if (myCar.getVelocity().length() < EMERGENCY_ESCAPING_STATE_VELOCITY_LIMIT) {
                transition(State.EMERGENCY_ESCAPING);
            }
        }
    }

    /**
     *
     */
    private void onAggressiveDriving() {
        if (myCar.isDebugMode()) {
            myCar.circleShape.setColor(0.0f, 1.0f, 1.0f);
        }

        // Update waypoint target
        updateWaypoint();

        // Drive to the target waypoint
        driveAlongTheWay(0.8f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles(1.0f, 0.0f);

        if (state == Car.AvoidingState.AVOIDING) {
            setLaneSelection(chooseAlternativeLane());
        }
    }

    /**
     *
     */
    private void onEmergencyEscaping() {
        if (myCar.isDebugMode()) {
            myCar.circleShape.setColor(1.0f, 0.0f, 1.0f);
        }

        // Update waypoint target
        updateWaypoint();

        // Driver to the target waypoint
        driveAlongTheWay(1.0f);
    }

    /**
     *
     */
    @SuppressLint("Assert")
    private void onOvertaking() {
        if (myCar.isDebugMode()) {
            myCar.circleShape.setColor(1.0f, 0.0f, 0.0f);
        }

        // If car collided, reduce tick count
        if (myCar.isCollided()) {
            tickTransitionTime(OVERTAKING_PENALTY_ON_BRAKING);
        }

        // Update waypoint target
        updateWaypoint();

        // Go faster
        driveAlongTheWay(1.0f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles(0.6f, 0.0f);
        if (state != Car.AvoidingState.NO_OBSTACLE) {
            // Take alternative lane
            setLaneSelection(chooseAlternativeLane());

            if (state == Car.AvoidingState.BRAKING) {
                if (stayingTimeOnState > 10) {
                    // Reduce tick counts faster if it takes brake
                    tickTransitionTime(OVERTAKING_PENALTY_ON_BRAKING);
                }
            }

            //  Go aggresive
            if (Math.random() < AGGRESSIVE_ENTER_POSSIBILITY) {
                transition(State.AGGRESSIVE_DRIVING);
            }
        }
    }

    /**
     *
     * @return
     */
    private boolean checkWaypointTargetIsBlocked() {
        PointF targetPt = targetRegion.center();

        // Get neighbors
        float distanceUnit = myCar.getShape().getRadius() * 2; //myCar.getMovingDistanceForOneUpdate();
        float maxForwardDistance = distanceUnit * 3;
        float maxBackwardDistance = distanceUnit * 2;
        ArrayList<CollidableObject> obstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        // Check if obstacle blocks the way
        float nearestDistance = Float.MAX_VALUE;
        float targetDistance = targetPt.distance(myCar.getPosition());
        float direction = targetPt.vectorize().subtract(myCar.getPositionVector()).direction();
        for (CollidableObject obstacle : obstacles) {
            float distance = myCar.checkObstacleCanBeCollided(obstacle, direction, targetDistance,
                    targetDistance);
            if (distance < nearestDistance) {
                nearestDistance = distance;
            }
        }

        return (nearestDistance != Float.MAX_VALUE);
    }

    enum LaneDirection {
        Left,
        Right;
    }

    /**
     *
     * @param direction
     * @param obstacles
     * @return
     */
    private Track.LaneSelection checkLaneIsMovable(LaneDirection direction,
                                                   ArrayList<CollidableObject> obstacles) {

        Track.LaneSelection adjacentLane = laneSelection;
        int newTargetWaypointIndex;
        while(true) {
            if (direction == LaneDirection.Left) {
                if (adjacentLane.ordinal() > 1) {
                    adjacentLane = Track.LaneSelection.values()[adjacentLane.ordinal() - 1];
                } else {
                    return Track.LaneSelection.INVALID_LANE;
                }
            } else {
                if (adjacentLane.ordinal() < Track.LaneSelection.values().length - 1) {
                    adjacentLane = Track.LaneSelection.values()[adjacentLane.ordinal() + 1];
                } else {
                    return Track.LaneSelection.INVALID_LANE;
                }
            }

            // Check next waypoint target on this lane
            int currentWaypointIndexOnPath = lastWaypointPassedIndex;
            newTargetWaypointIndex = findSuitableWaypointForNewTarget(adjacentLane,
                    currentWaypointIndexOnPath, currentWaypointIndexOnPath, 2);
            if (newTargetWaypointIndex == -1) {
                return Track.LaneSelection.INVALID_LANE;
            }

            if (!track.getWaypointPositionInMapCoord(laneSelection, newTargetWaypointIndex).equals(
                    track.getWaypointPositionInMapCoord(adjacentLane, newTargetWaypointIndex))) {
                break;
            }

            Log.v(LOG_TAG, name + " Lane is duplicated in this path between " +
                    laneSelection.toString() + " " + adjacentLane.toString());
        }

        PointF targetPt = track.getWaypointRegion(adjacentLane, newTargetWaypointIndex).center();

        float targetDistance = targetPt.distance(myCar.getPosition());
        float targetDirection = targetPt.vectorize().subtract(myCar.getPositionVector()).direction();

        // Check road block
        float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(myCar.getPosition(),
                targetDirection, targetDistance);
        if (distanceToRoadBlock > 0.0f && distanceToRoadBlock < Float.MAX_VALUE) {
            return Track.LaneSelection.INVALID_LANE;
        }

        // Check if obstacle blocks the way
        float nearestDistance = Float.MAX_VALUE;
        for (CollidableObject obstacle : obstacles) {
            float distance = myCar.checkObstacleCanBeCollided(obstacle, targetDirection, targetDistance,
                    targetDistance);
            if (distance < nearestDistance) {
                nearestDistance = distance;
            }
        }

        return adjacentLane;
    }

    /**
     *
     * @param laneSelection
     * @param obstacles
     * @return
     */
    private boolean checkLaneIsMovable(Track.LaneSelection laneSelection,
                                       ArrayList<CollidableObject> obstacles) {
        // Check next waypoint target on this path
        int currentWaypointIndexOnPath = lastWaypointPassedIndex;
        int newTargetWaypointIndex = findSuitableWaypointForNewTarget(laneSelection,
                currentWaypointIndexOnPath, currentWaypointIndexOnPath, 2);
        if (newTargetWaypointIndex == -1) {
            newTargetWaypointIndex =
                    (currentWaypointIndexOnPath + 1) % track.getLane(laneSelection).size();
        }
        //int newTargetWaypointIndex = (currentWaypointIndexOnPath + 1) % track.getPath(selection).size();
        PointF targetPt = track.getWaypointRegion(laneSelection, newTargetWaypointIndex).center();

        float targetDistance = targetPt.distance(myCar.getPosition());
        float direction = targetPt.vectorize().subtract(myCar.getPositionVector()).direction();

        // Check road block
        float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(myCar.getPosition(),
                direction, targetDistance);
        if (distanceToRoadBlock > 0.0f && distanceToRoadBlock < Float.MAX_VALUE) {
            return false;
        }

        // Check if obstacle blocks the way
        float nearestDistance = Float.MAX_VALUE;
        for (CollidableObject obstacle : obstacles) {
            float distance = myCar.checkObstacleCanBeCollided(obstacle, direction, targetDistance,
                    targetDistance);
            if (distance < nearestDistance) {
                nearestDistance = distance;
            }
        }

        return true;
    }

    /**
     *
     * @return
     */
    private Track.LaneSelection chooseAlternativeLane() {
        if (laneChangingGuardTimeLeft > 0) {
            return Track.LaneSelection.INVALID_LANE;
        }

        float distanceUnit = myCar.getShape().getRadius() * 2; // myCar.getMovingDistanceForOneUpdate();
        float maxForwardDistance = distanceUnit * 4;
        float maxBackwardDistance = distanceUnit * 2;
        ArrayList<CollidableObject> neighborObstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        // If there's no neighbors, take middle lane
        if (neighborObstacles.size() == 0) {
            Log.e(LOG_TAG, name + " alternative lane empty");
            return Track.LaneSelection.MIDDLE_LANE;
        }

        // Count vehicles on the each lanes
        int[] vehicleCountOnLane = new int[Track.LaneSelection.values().length];
        Arrays.fill(vehicleCountOnLane, 0);
        for (CollidableObject obstacle : neighborObstacles) {
            if (obstacle instanceof Car) {
                Car neighborCar = (Car) obstacle;
                Driver neighborDriver = neighborCar.getDriver();

                if (neighborDriver != null) {
                    Track.LaneSelection selection = neighborDriver.getPathSelection();
                    vehicleCountOnLane[selection.ordinal()]++;
                }
            }
        }

        // Check adjacent lanes are movable first
        Track.LaneSelection leftAdjacentLane = checkLaneIsMovable(LaneDirection.Left, neighborObstacles);
        Track.LaneSelection rightAdjacentLane = checkLaneIsMovable(LaneDirection.Right, neighborObstacles);
        boolean leftAdjacentLaneMovable = (leftAdjacentLane != Track.LaneSelection.INVALID_LANE),
                rightAdjacentLaneMovable = (rightAdjacentLane != Track.LaneSelection.INVALID_LANE);

        boolean leftAdjacentFeasibility = false, rightAdjacentFeasibility = false;
        if (leftAdjacentLaneMovable && vehicleCountOnLane[leftAdjacentLane.ordinal()] <= 2) {
            leftAdjacentFeasibility = true;
        }
        if (rightAdjacentLaneMovable && vehicleCountOnLane[rightAdjacentLane.ordinal()] <= 2) {
            rightAdjacentFeasibility = true;
        }

        if (!leftAdjacentFeasibility && rightAdjacentFeasibility) {
            Log.v(LOG_TAG, name + " alternative lane right " + rightAdjacentLane.toString());
            return rightAdjacentLane;
        }
        else if (leftAdjacentFeasibility && !rightAdjacentFeasibility) {
            Log.v(LOG_TAG, name + " alternative lane left " + leftAdjacentLane.toString());
            return leftAdjacentLane;
        }
        else if (!leftAdjacentFeasibility && !rightAdjacentFeasibility) {
            return laneSelection;
        }
        else {
            if (vehicleCountOnLane[leftAdjacentLane.ordinal()] < vehicleCountOnLane[rightAdjacentLane.ordinal()]) {
                Log.v(LOG_TAG, name + " alternative lane count left " + leftAdjacentLane.toString());
                return leftAdjacentLane;
            }
            else if (vehicleCountOnLane[leftAdjacentLane.ordinal()] > vehicleCountOnLane[rightAdjacentLane.ordinal()]) {
                Log.v(LOG_TAG, name + " alternative lane count right " + rightAdjacentLane.toString());
                return rightAdjacentLane;
            }
            else {
                if (Math.abs(rightAdjacentLane.ordinal() - Track.LaneSelection.MIDDLE_LANE.ordinal()) <
                        Math.abs(leftAdjacentLane.ordinal() - Track.LaneSelection.MIDDLE_LANE.ordinal())) {
                    Log.v(LOG_TAG, name + " alternative lane close right " + rightAdjacentLane.toString());
                    return rightAdjacentLane;
                }
                else {
                    Log.v(LOG_TAG, name + " alternative lane close left " + leftAdjacentLane.toString());
                    return leftAdjacentLane;
                }
            }
        }
    }

    /**
     *
     * @return
     */
    private Track.LaneSelection chooseAlternativeLane(boolean goLeft) {
        if (laneChangingGuardTimeLeft > 0) {
            return Track.LaneSelection.INVALID_LANE;
        }

        float distanceUnit = myCar.getShape().getRadius() * 4; // myCar.getMovingDistanceForOneUpdate();
        float maxForwardDistance = distanceUnit * 3;
        float maxBackwardDistance = distanceUnit * 2;
        ArrayList<CollidableObject> neighborObstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        // Check adjacent lanes are movable first
        if (goLeft) {
            if (laneSelection.ordinal() > 1) {
                Track.LaneSelection leftAdjacentLane =
                        Track.LaneSelection.values()[laneSelection.ordinal()-1];
                if (checkLaneIsMovable(leftAdjacentLane, neighborObstacles)) {
                    return leftAdjacentLane;
                }
            }
        }
        else {
            if (laneSelection.ordinal() > 1) {
                Track.LaneSelection leftAdjacentLane =
                        Track.LaneSelection.values()[laneSelection.ordinal()-1];
                if (checkLaneIsMovable(leftAdjacentLane, neighborObstacles)) {
                    return leftAdjacentLane;
                }
            }
        }

        return laneSelection;
    }

    /**
     *
     * @param maxDistance
     * @return
     */
    private CollidableObject getNearestFrontObstacle(float maxDistance) {
        // Get front obstacles
        ArrayList<CollidableObject> frontObstacles = getNeighborObstacles(20.0f,
                maxDistance, 0);

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

    /**
     *
     */
    private void drive() {
        // Debugging
        RectF lastPassedRegion = track.getWaypointRegion(laneSelection, lastWaypointPassedIndex);
        RectF targetRegion = track.getWaypointRegion(laneSelection, targetWaypointIndex);
        if (debugMode) {
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
                targetWaypointLineL = new Line.Builder(targetRegion.topLeft(), targetRegion.bottomLeft())
                        .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
                targetWaypointLineR = new Line.Builder(targetRegion.topRight(), targetRegion.bottomRight())
                        .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
                targetWaypointLineT = new Line.Builder(targetRegion.topLeft(), targetRegion.topRight())
                        .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
                targetWaypointLineB = new Line.Builder(targetRegion.bottomLeft(), targetRegion.bottomRight())
                        .color(1.0f, 1.0f, 0.0f, 1.0f).visible(true).build();
            } else {
                waypointLine.setPoints(myCar.getPosition(), targetRegion.center());
                lastPassedWaypointLineL.setPoints(lastPassedRegion.topLeft(), lastPassedRegion.bottomLeft());
                lastPassedWaypointLineR.setPoints(lastPassedRegion.topRight(), lastPassedRegion.bottomRight());
                lastPassedWaypointLineT.setPoints(lastPassedRegion.topLeft(), lastPassedRegion.topRight());
                lastPassedWaypointLineB.setPoints(lastPassedRegion.bottomLeft(), lastPassedRegion.bottomRight());
                targetWaypointLineL.setPoints(targetRegion.topLeft(), targetRegion.bottomLeft());
                targetWaypointLineR.setPoints(targetRegion.topRight(), targetRegion.bottomRight());
                targetWaypointLineT.setPoints(targetRegion.topLeft(), targetRegion.topRight());
                targetWaypointLineB.setPoints(targetRegion.bottomLeft(), targetRegion.bottomRight());
            }
            //waypointLine.commit();
            lastPassedWaypointLineL.commit();
            lastPassedWaypointLineR.commit();
            lastPassedWaypointLineT.commit();
            lastPassedWaypointLineB.commit();
            targetWaypointLineL.commit();
            targetWaypointLineR.commit();
            targetWaypointLineT.commit();
            targetWaypointLineB.commit();
        }

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
                onAggressiveDriving();
                break;
            case EMERGENCY_ESCAPING:
                onEmergencyEscaping();
                break;
            case OVERTAKING:
                onOvertaking();
                break;
        }

        if (state == prevState) {
            tickTransitionTime(1);
            if (stayingTimeLeftOnState <= 0) {
                transition(state.returnState());
            }
        }

        if (laneChangingGuardTimeLeft > 0) {
            laneChangingGuardTimeLeft--;
        }
    }

    /**
     *
     * @param tickCount
     */
    private void tickTransitionTime(int tickCount) {
        stayingTimeLeftOnState -= tickCount;
        stayingTimeOnState += tickCount;
    }

    /**
     *
     * @param tickCount
     */
    private void extendTransitionTime(int tickCount) {
        stayingTimeLeftOnState += tickCount;
    }

    /**
     *
     * @param laneSelection
     */
    private void setLaneSelection(Track.LaneSelection laneSelection) {
        if (laneSelection == Track.LaneSelection.INVALID_LANE) {
            return;
        }

        if (laneChangingGuardTimeLeft != 0) {
            Log.e(LOG_TAG, "laneChangingGuardTimeLeft != 0 : " + laneChangingGuardTimeLeft);
            return;
        }

        if (this.laneSelection != laneSelection) {
            laneChangingGuardTimeLeft = LANE_CHANGING_GUARD_TIME;

            int newTargetWaypointIndex = findSuitableWaypointForNewTarget(laneSelection,
                    lastWaypointPassedIndex, lastWaypointPassedIndex,2);
            if (newTargetWaypointIndex == -1) {
                newTargetWaypointIndex =
                        track.findNextValidWaypoint(laneSelection, lastWaypointPassedIndex);
            }
            this.laneSelection = laneSelection;
            this.targetWaypointIndex = newTargetWaypointIndex;
            setNewWaypointToTarget();
        }
    }

    /**
     *
     * @return
     */
    private Track.LaneSelection getPathSelection() {
        return laneSelection;
    }

    /**
     *
     * @return
     */
    String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public int getLastWaypointPassedIndex() {
        return lastWaypointPassedIndex;
    }

    /**
     *
     * @return
     */
    public int getLap() {
        return lap;
    }

    /**
     *
     * @return
     */
    public int getRank() {
        return rank;
    }

    /**
     *
     * @param rank
     */
    void setRank(int rank) {
        this.rank = rank;
    }

    /**
     *
     * @param effect
     */
    void addEffect(Effect effect) {
        effects.add(effect);
    }

    /**
     *
     * @return
     */
    private void applyEffects() {
        float modifierCarGeneral = 1.0f;
        float modifierDriverGeneral = 1.0f;

        for(Iterator<Effect> it = effects.iterator(); it.hasNext() ; ) {
            Effect effect = it.next();

            modifierCarGeneral *= effect.getModifierCarGeneral();
            modifierDriverGeneral *= effect.getModifierDriverGeneral();

            effect.tick();
            if (effect.isExpired()) {
                it.remove();
            }
        }

        setModifierDriverGeneral(modifierDriverGeneral);
        myCar.setModifierGeneral(modifierCarGeneral);
    }

    public float getModifierDriverGeneral() {
        return modifierDriverGeneral;
    }

    public void setModifierDriverGeneral(float modifierDriverGeneral) {
        this.modifierDriverGeneral = modifierDriverGeneral;
    }

    private final int STARTING_WAYPOINT_INDEX = 10;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 1;
    private final int DEFENSIVE_DRIVING_RELEASE_TICKCOUNT = 10;
    private final float EMERGENCY_ESCAPING_STATE_VELOCITY_LIMIT = 2.0f;
    private final float OVERTAKING_ENTER_POSSIBILITY = 0.03f;
    private final float AGGRESSIVE_ENTER_POSSIBILITY = 0.05f;
    private final int OVERTAKING_PENALTY_ON_BRAKING = 10;
    private final int LANE_CHANGING_GUARD_TIME = 6;

    private String name;
    private Car myCar;
    private Track track;
    private RectF targetRegion;

    private ArrayList<Car> cars;
    private ArrayList<CollidableObject> obstacles;

    // waypoints
    private int lastWaypointPassedIndex;
    private int targetWaypointIndex;
    private int nextWaypointSearchTimeLeft;
    private Track.LaneSelection laneSelection;

    // state-machine
    private State state;
    private int stayingTimeLeftOnState;
    private int stayingTimeOnState;
    private int laneChangingGuardTimeLeft = 0;

    // state
    private int lap;
    private int rank;
    private boolean finishLineCheckerDone;
    private ArrayList<Effect> effects;
    private float modifierDriverGeneral;

    // debugging
    private boolean debugMode = false;
    private Line waypointLine;
    private Line lastPassedWaypointLineL, lastPassedWaypointLineR, lastPassedWaypointLineT,
            lastPassedWaypointLineB;
    private Line targetWaypointLineL, targetWaypointLineR, targetWaypointLineT,
            targetWaypointLineB;
}
