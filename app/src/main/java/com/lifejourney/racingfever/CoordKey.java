package com.lifejourney.racingfever;

import java.lang.Object;

public class CoordKey {
    private int x;
    private int y;

    public CoordKey(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        return this.x + 65535*this.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CoordKey)) return false;
        if (((CoordKey) obj).x != x) return false;
        if (((CoordKey) obj).y != y) return false;
        return true;
    }
}