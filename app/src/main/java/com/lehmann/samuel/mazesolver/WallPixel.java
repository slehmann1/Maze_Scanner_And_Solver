package com.lehmann.samuel.mazesolver;

/**
 * Created by samue on 2017-12-06.
 */

public class WallPixel extends Coordinate {

    public int groupId;

    public WallPixel(int x, int y, int groupId) {
        super(x, y);
        this.groupId = groupId;
    }

}
