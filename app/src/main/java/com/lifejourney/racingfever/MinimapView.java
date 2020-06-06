package com.lifejourney.racingfever;

import android.util.Log;

import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.View;

import java.util.ArrayList;

class MinimapView implements View {

    private String LOG_TAG = "TrackView";

    MinimapView(Track track) {
        trackData = track.getData();
        trackView = track.getView();
        dots = new ArrayList<>();
        dotXRatio = VIEW_WIDTH / (float)trackData.getSize().width;
        dotYRatio = VIEW_HEIGHT / (float)trackData.getSize().height;

        // Create minimap sprite
        minimap = new Sprite.Builder(trackData.getMapAsset())
                        .size(new Size(VIEW_WIDTH, VIEW_HEIGHT))
                        .smooth(true).opaque(0.6f)
                        .layer(VIEW_LAYER).visible(false).build();
    }

    void setCars(ArrayList<Car> cars) {
        this.cars = cars;

        // Create dot sprites
        for (int i = 0; i < cars.size(); ++i) {
            Sprite dot = new Sprite.Builder("minimap_dots.png")
                    .size(new Size(DOT_WIDTH, DOT_HEIGHT))
                    .smooth(false)
                    .gridSize(new Size(8, 1))
                    .layer(VIEW_LAYER+1).visible(visible).build();
            dot.setGridIndex(new Point(i, 0));
            dots.add(dot);
        }
    }

    /**
     *
     */
    @Override
    public void close() {
        trackData = null;
        minimap.close();
    }

    /**
     *
     */
    @Override
    public void update() {
        if (!visible) {
            return;
        }
    }

    /**
     *
     */
    @Override
    public void commit() {
        if (!visible) {
            return;
        }

        // Adjust position of map and dots
        Rect viewport = Engine2D.GetInstance().getViewport();
        Point minimapPos = new Point(viewport.x + VIEW_X + VIEW_WIDTH/2,
            viewport.y + VIEW_Y + VIEW_HEIGHT/2);
        minimap.setPos(minimapPos);
        minimap.commit();
        for (int i = 0; i < dots.size(); ++i) {
            Sprite dot = dots.get(i);
            Car car = cars.get(i);
            Point dotPos =
                    trackView.getTrackCoordFromScreenCoord(car.getPosition())
                            .multiply(new PointF(dotXRatio, dotYRatio))
                            .offset(viewport.x + VIEW_X,viewport.y + VIEW_Y);

            dot.setPos(dotPos);
            dot.commit();
        }
    }

    /**
     *
     */
    @Override
    public void show() {
        visible = true;
        minimap.show();
        for(Sprite dot: dots) {
            dot.show();
        }
    }

    /**
     *
     */
    @Override
    public void hide() {
        visible = false;
        minimap.hide();
        for(Sprite dot: dots) {
            dot.hide();
        }
    }

    private final int VIEW_LAYER = 5;
    private final int VIEW_X = 30, VIEW_Y = 480;
    private final int VIEW_WIDTH = 300, VIEW_HEIGHT = 200;
    private final int DOT_WIDTH = 16 , DOT_HEIGHT = 16;

    private TrackData trackData;
    private TrackView trackView;
    private Sprite minimap;
    private ArrayList<Car> cars;
    private ArrayList<Sprite> dots;
    private boolean visible;
    private float dotXRatio, dotYRatio;
}
