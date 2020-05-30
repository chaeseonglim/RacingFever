package com.lifejourney.engine2d;

import androidx.annotation.Nullable;

public class Waypoint implements Comparable<Waypoint> {

    public Waypoint(Point position, Waypoint parent, float costFromStart) {
        this.position = position;
        this.parent = parent;
        this.costFromStart = costFromStart;
        this.costToTarget = 0.0f;
        this.next = null;
        this.prev = null;
        this.valid = true;
        this.costToSearch = 1;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Waypoint o) {
        return Float.compare(getCost(), o.getCost());
    }

    /**
     *
     * @param costToTarget
     */
    void setCostToTarget(float costToTarget) {
        this.costToTarget = costToTarget;
    }

    /**
     *
     * @return
     */
    float getCostFromStart() {
        return costFromStart;
    }

    /**
     *
     * @return
     */
    public float getCost() {
        return costToTarget + costFromStart;
    }

    /**
     *
     * @return
     */
    public Point getPosition() {
        return position;
    }

    /**
     *
     * @return
     */
    public Waypoint getParent() {
        return parent;
    }

    /**
     *
     * @return
     */
    public Waypoint getNext() {
        return next;
    }

    /**
     *
     * @param next
     */
    public void setNext(Waypoint next) {
        this.next = next;
    }

    /**
     *
     * @return
     */
    public Waypoint getPrev() {
        return prev;
    }

    /**
     *
     * @param prev
     */
    public void setPrev(Waypoint prev) {
        this.prev = prev;
    }

    /**
     *
     * @param valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     *
     * @return
     */
    public boolean isValid() {
        return valid;
    }

    /**
     *
     * @return
     */
    public int getCostToSearch() {
        return costToSearch;
    }

    /**
     *
     * @param costToSearch
     */
    public void setCostToSearch(int costToSearch) {
        this.costToSearch = costToSearch;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(@Nullable java.lang.Object obj) {
        if (this != obj) {
            if (obj instanceof Waypoint) {
                return this.position.equals(((Waypoint) obj).position);
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        return this.position.hashCode();
    }

    private Point position;
    private Waypoint parent;
    private Waypoint next;
    private Waypoint prev;
    private float costFromStart;
    private float costToTarget;
    private boolean valid;
    private int costToSearch;
}
