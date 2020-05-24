package com.lifejourney.racingfever;

import java.lang.Object;

public class CoordKey {
    CoordKey(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return this.x + 65535*this.y;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CoordKey)) return false;
        if (((CoordKey) obj).x != x) return false;
        return ((CoordKey) obj).y == y;
    }

    private int x;
    private int y;

}