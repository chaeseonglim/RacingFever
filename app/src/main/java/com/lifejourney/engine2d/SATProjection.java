package com.lifejourney.engine2d;

public class SATProjection {

    SATProjection(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public boolean isOverlap(SATProjection p2) {
        if (this.max > p2.min && this.min < p2.max)
            return true;
        return false;
    }

    public float getOverlap(SATProjection p2) {
        return (this.max - this.min + p2.max - p2.min) -
                (Math.max(this.max, p2.max) - Math.min(this.min, p2.min));
    }

    private float min;
    private float max;
}
