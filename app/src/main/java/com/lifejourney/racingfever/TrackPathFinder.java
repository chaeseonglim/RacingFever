package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.PathFinder;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.Rect;

class TrackPathFinder extends PathFinder {

    TrackPathFinder(TrackData trackData) {
        super(trackData.getStartPoint(trackData.getStartPointCount()/2),
            trackData.getMidEndPoint());
        this.trackData = trackData;
    }

    TrackPathFinder(TrackData trackData, Point startPosition, Point targetPosition) {
        super(startPosition, targetPosition);
        this.trackData = trackData;
    }

    /**
     *
     * @param curPt
     * @param newPt
     * @return
     */
    @Override
    protected boolean canMove(Point curPt, Point newPt) {
        // Special treat for diagonal move
        if (curPt.x != newPt.x && curPt.y != newPt.y) {
            return trackData.isSearchable(new Point(curPt.x, newPt.y)) &&
                    trackData.isSearchable(new Point(newPt.x, curPt.y)) &&
                    trackData.isSearchable(newPt);
        }
        else {
            return trackData.isSearchable(newPt);
        }
    }

    private TrackData trackData;
}
