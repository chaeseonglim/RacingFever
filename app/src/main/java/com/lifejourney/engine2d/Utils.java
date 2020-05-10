package com.lifejourney.engine2d;

import android.util.Log;

public class Utils {

    private static final String LOG_TAG = "Utils";

    /**
     *
     * @param p1 First point of segment
     * @param p2 Second point of segment
     * @param p3 Point which we need to know the distance between segment
     * @return
     */
    public static float distanceToSegment(PointF p1, PointF p2, PointF p3) {
        Vector2D v = p2.vectorize().subtract(p1.vectorize());
        Vector2D w = p3.vectorize().subtract(p1.vectorize());

        float c1 = w.dot(v);
        if (c1 <= 0) {
            return p3.vectorize().distance(p1.vectorize());
        }
        float c2 = v.dot(v);
        if (c2 <= c1) {
            return p3.vectorize().distance(p2.vectorize());
        }
        float b = c1 / c2;
        Vector2D Pb = p3.vectorize().add(v.multiply(b));
        return Pb.distance(p3.vectorize());
    }

}
