package com.lifejourney.racingfever;

import java.util.ArrayList;
import java.util.Arrays;

public class Shape {

    public Shape() {
    }

    // circle
    public Shape(float radius) {
        this.centroid = new PointF(0, 0);
        this.radius = radius;
    }

    // polygon
    public Shape(ArrayList<PointF> vertices) {
        this.vertices = vertices;
        this.centroid = calcCentroid();
        this.radius = calcMinimumRadius();
    }

    // polygon
    public Shape(PointF[] vertices) {
        this.vertices = new ArrayList<PointF>(Arrays.asList(vertices));
        this.centroid = calcCentroid();
        this.radius = calcMinimumRadius();
    }

    public Shape multiply(float m) {
        if (vertices.size() > 0) {
            for (PointF vertex : vertices) {
                vertex.multiply(m);
            }
            centroid = calcCentroid();
            radius = calcMinimumRadius();
        }
        else {
            radius *= m;
        }

        return this;
    }

    public boolean isValid() {
        if (vertices.size() == 0 && radius == 0.0f)
            return false;
        else
            return true;
    }

    public boolean isCircle() {
        if (vertices.size() == 0 && radius != 0.0f) {
            return true;
        }
        else {
            return false;
        }
    }

    private PointF calcCentroid() {
        if (vertices.size() == 0)
            return new PointF();

        float xSum = 0.0f, ySum = 0.0f;
        for (PointF vertex: vertices) {
            xSum += vertex.x;
            ySum += vertex.y;
        }
        return new PointF(xSum/vertices.size(), ySum/vertices.size());
    }

    private float calcMinimumRadius() {
        float radius = 0.0f;
        for (PointF vertex: vertices) {
            radius = Math.max(vertex.vectorize().subtract(centroid.vectorize()).length(), radius);
        }

        return radius;
    }

    public PointF getCentroid() {
        return centroid;
    }

    public float getRadius() {
        return radius;
    }

    public ArrayList<PointF> getVertices() {
        return vertices;
    }

    // TODO: Shape should consider rotation

    private ArrayList<PointF> vertices;
    private PointF centroid;
    private float radius = 0.0f;

}
