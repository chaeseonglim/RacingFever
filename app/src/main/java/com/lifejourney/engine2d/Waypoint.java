package com.lifejourney.engine2d;

import androidx.annotation.Nullable;

public class Waypoint implements Comparable<Waypoint> {

    public Waypoint(Point position, Waypoint parent, float costFromStart) {
        this.position = position;
        this.parent = parent;
        this.costFromStart = costFromStart;
    }

    @Override
    public int compareTo(Waypoint o) {
        if (getCost() > o.getCost())
            return 1;
        else if (getCost() < o.getCost())
            return -1;
        else
            return 0;
    }

    public void setCostToTarget(float costToTarget) {
        this.costToTarget = costToTarget;
    }

    public float getCostFromStart() {
        return costFromStart;
    }

    public float getCost() {
        return costToTarget + costFromStart;
    }

    public Point getPosition() {
        return position;
    }

    public Waypoint getParent() {
        return parent;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean getValid() {
        return valid;
    }

    @Override
    public boolean equals(@Nullable java.lang.Object obj) {
        if (!super.equals(obj)) {
            if (this.position.equals(((Waypoint)obj).position)) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    private Point position;
    private Waypoint parent;
    private float costFromStart = 0.0f;
    private float costToTarget = 0.0f;
    private boolean valid = true;
}
