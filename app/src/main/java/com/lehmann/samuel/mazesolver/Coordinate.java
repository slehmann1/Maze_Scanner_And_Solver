package com.lehmann.samuel.mazesolver;

/**
 * Created by samuel on 2017-12-01.
 */

public class Coordinate {
    public int x, y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the distance to another coordinate
     * @param otherCoord
     * @return
     */
    public double distanceTo(Coordinate otherCoord) {
        return Math.sqrt((otherCoord.x - x) * (otherCoord.x - x) + (otherCoord.y - y) * (otherCoord.y - y));
    }

}
