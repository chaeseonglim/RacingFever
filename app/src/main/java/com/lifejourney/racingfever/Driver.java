package com.lifejourney.racingfever;

import android.util.Log;

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
        DEFENSIVE_DRIVING(60, CRUISING),
        AGGRESSIVE_DRIVING(30, CRUISING),
        EMERGENCY_ESCAPING(10, CRUISING),
        OVERTAKING(240, CRUISING);

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
            int otherLastWaypointIndexInOptimal = other.lastWaypointPassedIndex;
            int thisLastWaypointIndexInOptimal = this.lastWaypointPassedIndex;
            if (otherLastWaypointIndexInOptimal > thisLastWaypointIndexInOptimal) {
                return -1;
            }
            else if (otherLastWaypointIndexInOptimal < thisLastWaypointIndexInOptimal) {
                return 1;
            }
            else {
                int waypointCount = track.getPath(Track.LaneSelection.OPTIMAL_LANE).size();
                int nextPassingWaypointIndex = (thisLastWaypointIndexInOptimal + 1) % waypointCount;
                PointF nextWaypointPt = getWaypointRegion(Track.LaneSelection.OPTIMAL_LANE,
                        nextPassingWaypointIndex).center();

                float distanceSqToNextWaypointForOther =
                        other.myCar.getPosition().distanceSq(nextWaypointPt);
                float distanceSqToNextWaypointForThis =
                        this.myCar.getPosition().distanceSq(nextWaypointPt);

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
        int totaNumberOfWaypoints = track.getPath(currentLaneSelection).size();

        return Math.min(Math.abs(waypoint1-waypoint2),
                Math.abs(totaNumberOfWaypoints-Math.max(waypoint1, waypoint2)+
                        Math.min(waypoint1, waypoint2)));
    }

    private void updateLastPassedWaypoint() {
        int waypointCount = track.getPath(currentLaneSelection).size();
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
            RectF region = getWaypointRegion(currentLaneSelection, currentWaypointIndex);

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

    private int findSuitableWaypointForNewTarget(Track.LaneSelection selection, int currentIndex,
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
            PointF targetCenter = getWaypointRegion(selection, waypointIndex).center();
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
        int newWaypoint = findSuitableWaypointForNewTarget(currentLaneSelection,
                lastWaypointPassedIndex, currentTargetWaypointIndex);
        if (newWaypoint == -1) {
            int waypointCount = track.getPath(currentLaneSelection).size();
            newWaypoint = (lastWaypointPassedIndex + 1) % waypointCount;
        }

        setWaypointToTarget(newWaypoint);
    }

    private void setNewWaypointToTargetOnlyIfSuitable() {
        int newWaypoint = findSuitableWaypointForNewTarget(currentLaneSelection,
                lastWaypointPassedIndex, currentTargetWaypointIndex);
        if (newWaypoint != -1) {
            setWaypointToTarget(newWaypoint);
        }
    }

    private void setWaypointToTarget(int waypointIndex) {
        setTargetRegion(getWaypointRegion(currentLaneSelection, waypointIndex));
        currentTargetWaypointIndex = waypointIndex;

        /*
        Log.i(LOG_TAG, name + " " + currentLaneSelection.ordinal() +
                " currentTargetWaypointIndex: " + currentTargetWaypointIndex +
                " " + lastWaypointPassedIndex);
         */
    }

    private RectF getWaypointRegion(Track.LaneSelection selection, int waypointIndex) {
        Point targetMap = track.getPath(selection).get(waypointIndex).getPosition();
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
        weight += (Math.random()%0.2f) - 0.1f;
        myCar.seek(targetRegion.center(), weight);
    }

    private void transition(State state) {
        this.state = state;
        stateStayingTimeLeft = state.maxStayingTime();
        stateStayingTime = 0;
        defensiveDrivingReleaseCount = 0;
    }

    private ArrayList<CollidableObject> getNeighborObstacles(float frontAngle,
        float maxForwardDistance, float maxBackwardDistance) {
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
            Vector2D unitOffset = offset.normalize();
            if (myCar.getForwardVector().angle(unitOffset) < frontAngle) {
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
        Car.AvoidingState state = myCar.avoidObstacles(neighborObstacles, maxForwardDistance,
                maxBackwardDistance, track);

        return state;
    }

    private void tryOvertaking() {
        // Check if it can go to overtaking state
        float overDrivingScore = OVERTAKING_ENTER_POSSIBILITY;
        float maxDistance = myCar.getVelocity().length() * myCar.getUpdatePeriod() * 3;
        CollidableObject frontObstacle = getNearestFrontObstacle(maxDistance);
        if (frontObstacle == null) {
            overDrivingScore += OVERTAKING_ENTER_POSSIBILITY;
        } else if (frontObstacle.getVelocity().length() < myCar.getMaxVelocity()) {
            overDrivingScore += OVERTAKING_ENTER_POSSIBILITY;
        }

        if (Math.random() < overDrivingScore) {
            if (frontObstacle == null) {
                setPathSelection(Track.LaneSelection.MIDDLE_LANE);
                transition(State.OVERTAKING);
            } else {
                Track.LaneSelection laneSelection = chooseOvertakingPath();
                if (laneSelection != Track.LaneSelection.OPTIMAL_LANE) {
                    setPathSelection(laneSelection);
                    transition(State.OVERTAKING);
                    leftLaneCheckingLine.commit();
                    rightLaneCheckingLine.commit();
                }
            }
        }
    }

    private void onCruising() {
        myCar.circleShape.set(1.0f, 1.0f, 1.0f);

        // set to optimal path
        setPathSelection(Track.LaneSelection.MIDDLE_LANE);

        // Update waypoint target
        updateWaypoint();

        // Drive to the target waypoint
        driveAlongTheWay(0.6f);

        // Keep distance with front vehicle
        float maxDistance = myCar.getVelocity().length() * myCar.getUpdatePeriod() * 2;
        CollidableObject frontObstacle = getNearestFrontObstacle(maxDistance);
        if (frontObstacle != null && frontObstacle instanceof Car) {
            myCar.getVelocity().truncate(frontObstacle.getVelocity()
                    .dot(myCar.getForwardVector())*0.9f);
        }

        // Avoid collision
        Car.AvoidingState state = avoidObstacles();
        if (state == Car.AvoidingState.AVOIDING ||
            state == Car.AvoidingState.BRAKING) {
            transition(State.DEFENSIVE_DRIVING);
            if (state == Car.AvoidingState.AVOIDING) {
                setPathSelection(chooseDefensivePath());
            }
            return;
        }

        // Try overtaking
        tryOvertaking();
    }

    private void onDefensiveDriving() {
        myCar.circleShape.set(0.0f, 1.0f, 0.0f);

        // Update waypoint target
        updateWaypoint();

        // Drive to the target waypoint
        driveAlongTheWay(0.5f);

        // Avoid collision
        Car.AvoidingState state = avoidObstacles();
        if (state == Car.AvoidingState.NO_OBSTACLE) {
            defensiveDrivingReleaseCount++;
            if (defensiveDrivingReleaseCount > DEFENSIVE_DRIVING_RELEASE_COUNT) {
                transition(State.CRUISING);
                defensiveDrivingReleaseCount = 0;
            }
        }
        else {
            // if we stay here too long, let's go to emergency escaping mode
            if (stateStayingTime > DEFENSIVE_DRIVING_STAYING_LIMIT &&
                    myCar.getVelocity().length() < EMERGENCY_ESCAPING_STATE_VELOCITY_LIMIT) {
                transition(State.EMERGENCY_ESCAPING);
                setPathSelection(chooseDefensivePath());
            }
        }

        // Try overtaking
        tryOvertaking();
    }

    private void onEmergencyEscaping() {
        myCar.circleShape.set(1.0f, 0.0f, 1.0f);

        // Update waypoint target
        updateWaypoint();

        // Driver to the target waypoint
        driveAlongTheWay(1.0f);
    }

    private void onOvertaking() {
        myCar.circleShape.set(1.0f, 0.0f, 0.0f);

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
            tickTransitionTime(OVERTAKING_PENALTY_ON_BRAKING);
        }

        // Check if the lane is clear
        // TODO: If car is 1st, stop overtaking
        if (stateStayingTimeLeft == 0) {
           if (checkWaypointTargetIsBlocked() && Math.random() < OVERTAKING_EXTEND_POSSIBILITY) {
               extendTransitionTime(OVERTAKING_EXTEND_TICKCOUNT);
           }
        }
    }

    private boolean checkWaypointTargetIsBlocked() {
        PointF targetPt = targetRegion.center();

        // Get neighbors
        float distanceForOneUpdate = myCar.getVelocity().length() * myCar.getUpdatePeriod();
        float maxForwardDistance = distanceForOneUpdate * 6;
        float maxBackwardDistance = distanceForOneUpdate * 2;
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

        return (nearestDistance == Float.MAX_VALUE);
    }

    private float[] checkBoundaryPathToBeMovable(Track.LaneSelection laneSelection,
                                                 ArrayList<CollidableObject> obstacles) {
        // Check next waypoint target on this path
        int currentWaypointIndexOnPath = lastWaypointPassedIndex;
        int newTargetWaypointIndex = findSuitableWaypointForNewTarget(laneSelection,
                currentWaypointIndexOnPath, currentWaypointIndexOnPath);
        if (newTargetWaypointIndex == -1) {
            newTargetWaypointIndex = (currentWaypointIndexOnPath + 1) % track.getPath(laneSelection).size();
        }
        //int newTargetWaypointIndex = (currentWaypointIndexOnPath + 1) % track.getPath(selection).size();
        PointF targetPt = getWaypointRegion(laneSelection, newTargetWaypointIndex).center();

        if (laneSelection == Track.LaneSelection.LEFT_BOUNDARY_LANE) {
            if (leftLaneCheckingLine == null) {
                leftLaneCheckingLine = new Line.Builder(myCar.getPosition(), targetPt)
                        .color(1.0f, 0.0f, 0.0f, 1.0f).visible(true).build();
            } else {
                leftLaneCheckingLine.set(myCar.getPosition(), targetPt);
            }
        }
        else if (laneSelection == Track.LaneSelection.RIGHT_BOUNDARY_LANE) {
            if (rightLaneCheckingLine == null) {
                rightLaneCheckingLine = new Line.Builder(myCar.getPosition(), targetPt)
                        .color(1.0f, 0.0f, 0.0f, 1.0f).visible(true).build();
            } else {
                rightLaneCheckingLine.set(myCar.getPosition(), targetPt);
            }
        }

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

        float[] result = new float[2];

        // Check road block
        float distanceToRoadBlock = track.getNearestDistanceToRoadBlock(myCar.getPosition(),
                direction, targetDistance);
        if (distanceToRoadBlock > 0.0f && distanceToRoadBlock < Float.MAX_VALUE) {
            result[0] = targetDistance;
            result[1] = 0.0f;
            return result;
        }

        result[0] = targetDistance;
        result[1] = nearestDistance;
        return result;
    }

    private Track.LaneSelection chooseOvertakingPath() {
        // Get neighbors
        float distanceForOneUpdate = myCar.getVelocity().length() * myCar.getUpdatePeriod();
        float maxForwardDistance = distanceForOneUpdate * 6;
        float maxBackwardDistance = distanceForOneUpdate * 2;
        ArrayList<CollidableObject> neighborObstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        if (neighborObstacles.size() == 0) {
            return Track.LaneSelection.MIDDLE_LANE;
        }

        // Count vehicles on the each paths
        int vehiclesOnLeftLane = 0, vehiclesOnRightLane = 0, vehiclesOnMiddleLane = 0;
        for (CollidableObject obstacle : neighborObstacles) {
            if (obstacle instanceof Car) {
                Car neighborCar = (Car) obstacle;
                Driver neighborDriver = neighborCar.getDriver();

                Track.LaneSelection selection = neighborDriver.getPathSelection();
                if (selection == Track.LaneSelection.LEFT_BOUNDARY_LANE) {
                    vehiclesOnLeftLane++;
                } else if (selection == Track.LaneSelection.RIGHT_BOUNDARY_LANE) {
                    vehiclesOnRightLane++;
                } else if (selection == Track.LaneSelection.MIDDLE_LANE) {
                    vehiclesOnMiddleLane++;
                }
            }
        }

        if (vehiclesOnLeftLane >= 2 && vehiclesOnRightLane >= 2 &&
            vehiclesOnMiddleLane >= 2) {
            return Track.LaneSelection.OPTIMAL_LANE;
        }

        // Get distance to each boundary path
        float[] nearestDistanceToBlockOnLeftLane =
                checkBoundaryPathToBeMovable(Track.LaneSelection.LEFT_BOUNDARY_LANE,
                        neighborObstacles);
        float[] nearestDistanceToBlockOnRightLane =
                checkBoundaryPathToBeMovable(Track.LaneSelection.RIGHT_BOUNDARY_LANE,
                        neighborObstacles);
        float[] nearestDistanceToBlockOnMiddleLane =
                checkBoundaryPathToBeMovable(Track.LaneSelection.MIDDLE_LANE,
                        neighborObstacles);

        // Decide lane
        Track.LaneSelection laneSelection;
        float maxAllowedDistance = myCar.getVelocity().length()*myCar.getUpdatePeriod()*6;
        if (nearestDistanceToBlockOnLeftLane[1] < maxAllowedDistance &&
                nearestDistanceToBlockOnRightLane[1] < maxAllowedDistance) {
            laneSelection = Track.LaneSelection.OPTIMAL_LANE;
        }
        else if (nearestDistanceToBlockOnLeftLane[1] < maxAllowedDistance) {
            laneSelection = Track.LaneSelection.RIGHT_BOUNDARY_LANE;
        }
        else if (nearestDistanceToBlockOnRightLane[1] < maxAllowedDistance) {
            laneSelection = Track.LaneSelection.LEFT_BOUNDARY_LANE;
        }
        else {
            if (nearestDistanceToBlockOnLeftLane[0] > nearestDistanceToBlockOnRightLane[0]) {
                laneSelection = Track.LaneSelection.RIGHT_BOUNDARY_LANE;
            } else {
                laneSelection = Track.LaneSelection.LEFT_BOUNDARY_LANE;
            }
        }

        if (laneSelection == Track.LaneSelection.LEFT_BOUNDARY_LANE &&
                vehiclesOnLeftLane >= 2) {
            laneSelection = Track.LaneSelection.MIDDLE_LANE;
        }
        else if (laneSelection == Track.LaneSelection.RIGHT_BOUNDARY_LANE &&
                vehiclesOnRightLane >= 2) {
            laneSelection = Track.LaneSelection.MIDDLE_LANE;
        }

        return laneSelection;
    }

    private Track.LaneSelection chooseDefensivePath() {
        // Get neighbors
        float distanceForOneUpdate = myCar.getVelocity().length() * myCar.getUpdatePeriod();
        float maxForwardDistance = distanceForOneUpdate * 6;
        float maxBackwardDistance = distanceForOneUpdate * 2;
        ArrayList<CollidableObject> neighborObstacles = getNeighborObstacles(180.0f,
                maxForwardDistance, maxBackwardDistance);

        if (neighborObstacles.size() == 0) {
            return Track.LaneSelection.OPTIMAL_LANE;
        }

        // Get distance to each boundary path
        float[] leftBoundaryResult =
                checkBoundaryPathToBeMovable(Track.LaneSelection.LEFT_BOUNDARY_LANE,
                        neighborObstacles);
        float[] rightBoundaryResult =
                checkBoundaryPathToBeMovable(Track.LaneSelection.RIGHT_BOUNDARY_LANE,
                        neighborObstacles);

        // Decide path
        float maxAllowedDistance = myCar.getVelocity().length()*myCar.getUpdatePeriod()*4;
        if (leftBoundaryResult[1] < maxAllowedDistance &&
                rightBoundaryResult[1] < maxAllowedDistance) {
            return Track.LaneSelection.MIDDLE_LANE;
        }
        else if (leftBoundaryResult[1] < maxAllowedDistance) {
            return Track.LaneSelection.RIGHT_BOUNDARY_LANE;
        }
        else if (rightBoundaryResult[1] < maxAllowedDistance) {
            return Track.LaneSelection.LEFT_BOUNDARY_LANE;
        }
        else {
            if (leftBoundaryResult[1] == Float.MAX_VALUE &&
                rightBoundaryResult[1] == Float.MAX_VALUE) {
                // Count vehicles on the each paths
                int leftBoundaryVehicles = 0, rightBoundaryVehicles = 0;
                for (CollidableObject obstacle : neighborObstacles) {
                    if (obstacle instanceof Car) {
                        Car neighborCar = (Car) obstacle;
                        Driver neighborDriver = neighborCar.getDriver();

                        Track.LaneSelection selection = neighborDriver.getPathSelection();
                        if (selection == Track.LaneSelection.LEFT_BOUNDARY_LANE) {
                            leftBoundaryVehicles++;
                        } else if (selection == Track.LaneSelection.RIGHT_BOUNDARY_LANE) {
                            rightBoundaryVehicles++;
                        }
                    }
                }

                if (leftBoundaryVehicles < rightBoundaryVehicles) {
                    return Track.LaneSelection.LEFT_BOUNDARY_LANE;
                }
                else if (leftBoundaryVehicles > rightBoundaryVehicles) {
                    return Track.LaneSelection.RIGHT_BOUNDARY_LANE;
                }
                else {
                    if (leftBoundaryResult[0] > rightBoundaryResult[0]) {
                        return Track.LaneSelection.RIGHT_BOUNDARY_LANE;
                    }
                    else {
                        return Track.LaneSelection.LEFT_BOUNDARY_LANE;
                    }
                }

            }
            else if (leftBoundaryResult[1] == Float.MAX_VALUE) {
                return Track.LaneSelection.LEFT_BOUNDARY_LANE;
            }
            else if (rightBoundaryResult[1] == Float.MAX_VALUE) {
                return Track.LaneSelection.RIGHT_BOUNDARY_LANE;
            }
        }

        return Track.LaneSelection.MIDDLE_LANE;
    }

    private CollidableObject getNearestFrontObstacle(float maxDistance) {
        // Get front obstacles
        ArrayList<CollidableObject> frontObstacles = getNeighborObstacles(10.0f,
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

    private void drive() {
        // Debugging
        RectF lastPassedRegion = getWaypointRegion(currentLaneSelection, lastWaypointPassedIndex);
        RectF targetRegion = getWaypointRegion(currentLaneSelection, currentTargetWaypointIndex);
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
            case OVERTAKING:
                onOvertaking();
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

    private void extendTransitionTime(int tickCount) {
        stateStayingTimeLeft += tickCount;
    }

    private void setPathSelection(Track.LaneSelection selection) {
        if (currentLaneSelection != selection) {
            int newTargetWaypointIndex = findSuitableWaypointForNewTarget(selection,
                    lastWaypointPassedIndex, lastWaypointPassedIndex);
            if (newTargetWaypointIndex == -1) {
                newTargetWaypointIndex = (lastWaypointPassedIndex + 1) % track.getPath(selection).size();
            }
            currentLaneSelection = selection;
            currentTargetWaypointIndex = newTargetWaypointIndex;
            setNewWaypointToTarget();
        }
    }

    private Track.LaneSelection getPathSelection() {
        return currentLaneSelection;
    }

    private final int STARTING_WAYPOINT_INDEX = 30;
    private final int MIN_WAYPOINT_SEARCH_PERIOD = 1;
    private final int DEFENSIVE_DRIVING_STAYING_LIMIT = 30;
    private final int DEFENSIVE_DRIVING_RELEASE_COUNT = 3;
    private final float EMERGENCY_ESCAPING_STATE_VELOCITY_LIMIT = 2.0f;
    private final float OVERTAKING_ENTER_POSSIBILITY = 0.01f;
    private final float OVERTAKING_EXTEND_POSSIBILITY = 0.1f;
    private final int OVERTAKING_EXTEND_TICKCOUNT = 10;
    private final int OVERTAKING_PENALTY_ON_BRAKING = 10;

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
    private Track.LaneSelection currentLaneSelection = Track.LaneSelection.MIDDLE_LANE;

    // state-machine
    private State state;
    private int stateStayingTimeLeft = Integer.MAX_VALUE;
    private int stateStayingTime = 0;
    private int defensiveDrivingReleaseCount = 0;

    // debugging
    public Line waypointLine;
    public Line leftLaneCheckingLine;
    public Line rightLaneCheckingLine;
    public Line lastPassedWaypointLineL, lastPassedWaypointLineR, lastPassedWaypointLineT,
            lastPassedWaypointLineB;
    public Line targetWaypointLineL, targetWaypointLineR, targetWaypointLineT,
            targetWaypointLineB;
}
