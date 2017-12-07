package com.lehmann.samuel.mazesolver;

/**
 * Created by samuel on 2017-12-05.
 */

public class Vector {

    private Coordinate startPoint;
    private int length;
    private double direction;

    /**
     * Creates a Vector object
     *
     * @param startPoint start point in pixels
     * @param length     length in pixels
     * @param direction  The direction, measured from the x-axis (+ = horizontal right), in degrees
     */
    public Vector(Coordinate startPoint, int length, double direction) {
        this.startPoint = startPoint;
        this.length = length;
        this.direction = direction;
    }

    public double getDirection() {
        return direction;
    }

    /**
     * Computes and returns a coordinate specifying the endpoint, based on values given in the constructor
     *
     * @return
     */
    public Coordinate getEndpoint() {

        int x = (int) (startPoint.x + length * Math.cos(direction / 180 * Math.PI));
        int y = (int) (startPoint.y + length * Math.sin(direction / 180 * Math.PI));

        return new Coordinate(x, y);
    }


}
