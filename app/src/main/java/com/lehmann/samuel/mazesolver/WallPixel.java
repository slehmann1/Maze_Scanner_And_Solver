package com.lehmann.samuel.mazesolver;

/**
 * Created by samue on 2017-12-06.
 */

public class WallPixel extends Coordinate {

    public int groupId = -1; // Defaults to -1 if not set

    public WallPixel(Coordinate c) {
        super(c.x, c.y);
    }

    public WallPixel(int x, int y) {
        super(x, y);
    }

    public WallPixel(int x, int y, int groupId) {
        super(x, y);
        this.groupId = groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

}
