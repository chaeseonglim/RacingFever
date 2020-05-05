package com.lifejourney.racingfever;

import com.lifejourney.engine2d.Point;

import java.util.ArrayList;

public class Track {

    public Track(String mapAsset, float scale) {
        data = new TrackData(mapAsset);
        view = new TrackView(data, scale);
        view.show();

        TrackPathFinder pathFinder = new TrackPathFinder(data);
        optimalPath = pathFinder.findOptimalPath();
    }

    public TrackData getData() {
        return data;
    }

    public TrackView getView() {
        return view;
    }

    public ArrayList<Point> getOptimalPath() {
        return optimalPath;
    }

    public void show() {
        view.show();
    }

    public void hide() {
        view.hide();
    }

    private TrackData data;
    private TrackView view;
    private ArrayList<Point> optimalPath;
}
