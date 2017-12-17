package com.lehmann.samuel.mazesolver;

import android.graphics.Point;

/**
 * Created by samuel on 2017-12-01.
 */

public class Coordinate extends Point {

    final static private int PRIME = 3;

    public Coordinate(int x, int y) {
        super.x = x;
        super.y = y;
    }

    /**
     * Returns the distance to another coordinate
     *
     * @param otherCoord
     * @return
     */
    public double distanceTo(Coordinate otherCoord) {
        return Math.sqrt((otherCoord.x - x) * (otherCoord.x - x) + (otherCoord.y - y) * (otherCoord.y - y));
    }

    // There should never be a duplicate of any x/y combination, so this is ok
    @Override
    public int hashCode() {
        return super.x + (super.y + PRIME);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Coordinate c = (Coordinate) o;
        return super.x == c.x && super.y == c.y;
    }

}
