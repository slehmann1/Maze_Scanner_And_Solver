package com.lehmann.samuel.mazesolver;

import android.graphics.Point;

/**
 * Created by samuel on 2017-12-01.
 */

public class Coordinate extends Point {

    public Coordinate(int x, int y) {
        super.x = x;
        super.y = y;
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
