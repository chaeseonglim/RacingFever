package com.lifejourney.racingfever;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.lifejourney.engine2d.Engine2D;
import com.lifejourney.engine2d.Point;
import com.lifejourney.engine2d.PointF;
import com.lifejourney.engine2d.Rect;
import com.lifejourney.engine2d.ResourceManager;
import com.lifejourney.engine2d.Size;
import com.lifejourney.engine2d.Sprite;
import com.lifejourney.engine2d.View;
import com.lifejourney.engine2d.Waypoint;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

class MinimapView implements View {

    private String LOG_TAG = "TrackView";

    MinimapView(Track track) {
        trackData = track.getData();
        trackView = track.getView();
        dots = new ArrayList<>();

        int trackWidth = trackData.getSize().width;
        int trackHeight = trackData.getSize().height;
        dotXRatio = VIEW_WIDTH / (float)trackWidth;
        dotYRatio = VIEW_HEIGHT / (float)trackHeight;

        // Create minimap bitmap
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(trackWidth, trackHeight, conf);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(255, 255, 255, 255);

        Paint paint = new Paint();
        paint.setColor(Color.argb(255, 61, 61, 61));

        ArrayList<Waypoint> waypoints = track.getLane(Track.LaneSelection.MIDDLE_LANE);
        Waypoint prevWaypoint = null;
        for (Waypoint waypoint: waypoints) {
            if (prevWaypoint == null) {
                prevWaypoint = waypoints.get(waypoints.size() - 1);
            }
            canvas.drawLine(prevWaypoint.getPosition().x, prevWaypoint.getPosition().y,
                    waypoint.getPosition().x, waypoint.getPosition().y, paint);
            prevWaypoint = waypoint;
        }


        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;

        // Create minimap sprite
        minimap = new Sprite.Builder("minimap")
                        .data(byteArray)
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
        ResourceManager resourceManager = Engine2D.GetInstance().getResourceManager();
        resourceManager.releaseTexture("minimap");
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
