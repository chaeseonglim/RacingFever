package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.PathFinder;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.Rect;

public class TrackPathFinder extends PathFinder {

    public TrackPathFinder(TrackData trackData) {
        super(trackData.getStartPoint(trackData.getStartPointCount()/2),
            trackData.getMidEndPoint());
        this.trackData = trackData;
    }

    public TrackPathFinder(TrackData trackData, Point startPosition, Point targetPosition) {
        super(startPosition, targetPosition);
        this.trackData = trackData;
    }

    @Override
    protected boolean canMove(Point curPt, Point newPt) {
        // Special treat for diagonal move
        if (curPt.x != newPt.x && curPt.y != newPt.y) {
            return trackData.isMovable(new Point(curPt.x, newPt.y)) &&
                    trackData.isMovable(new Point(newPt.x, curPt.y)) &&
                    trackData.isMovable(newPt);
        }
        else {
            return trackData.isMovable(newPt);
        }
    }

    private TrackData trackData;
}
