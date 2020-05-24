package com.lifejourney.engine2d;

import java.util.ArrayList;

/**
 * Collision pool using Quad tree implementation
 */
public class QuadTree {

    QuadTree(int level, RectF region) {
        this.level = level;
        this.objects = new ArrayList<>();
        this.region = region;
        this.nodes = new QuadTree[4];
    }

    /**
     *
     */
    public void clear() {
        objects.clear();

        for (int i = 0; i < 4; ++i) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }

    /**
     *
     */
    public void split() {
        float subWidth = region.width / 2;
        float subHeight = region.height / 2;
        float x = region.x;
        float y = region.y;

        nodes[0] = new QuadTree(level+1, new RectF(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new QuadTree(level+1, new RectF(x, y, subWidth, subHeight));
        nodes[2] = new QuadTree(level+1, new RectF(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new QuadTree(level+1, new RectF(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    /**
     * Determine which node the object belongs to.
     *
     * @param targetRegion
     * @return -1 means object cannot completely fit within a child node and is part
     *          of the parent node
     */
    private int getIndex(RectF targetRegion) {
        int index = -1;
        float verticalMidpoint = region.x + (region.width / 2);
        float horizontalMidpoint = region.y + (region.height / 2);

        // Object can completely fit within the top quadrants
        boolean topQuadrant =
                (targetRegion.y < horizontalMidpoint &&
                        targetRegion.y + targetRegion.height < horizontalMidpoint);

        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = (targetRegion.y > horizontalMidpoint);

        // Object can completely fit within the left quadrants
        if (targetRegion.x < verticalMidpoint &&
                targetRegion.x + targetRegion.width < verticalMidpoint) {
            if (topQuadrant) {
                index = 1;
            }
            else if (bottomQuadrant) {
                index = 2;
            }
        }

        // Object can completely fit within the right quadrants
        else if (targetRegion.x > verticalMidpoint) {
            if (topQuadrant) {
                index = 0;
            }
            else if (bottomQuadrant) {
                index = 3;
            }
        }

        return index;
    }

    /**
     * Insert the object into the quadtree. If the node
     * exceeds the capacity, it will split and add all
     * objects to their corresponding nodes.
     *
     * @param object
     */
    public void insert(CollidableObject object) {
        {
            RectF objRegion = object.getShape().getMinimumCoveredRect();
            if (nodes[0] != null) {
                int index = getIndex(objRegion);

                if (index != -1) {
                    nodes[index].insert(object);

                    return;
                }
            }
        }

        objects.add(object);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                RectF objRegion = objects.get(i).getShape().getMinimumCoveredRect();
                int index = getIndex(objRegion);
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                }
                else {
                    i++;
                }
            }
        }
    }

    /**
     * Return all objects that could collide with the given object
     *
     * @param returnObjects
     * @param object
     * @return
     */
    ArrayList<CollidableObject> retrieve(ArrayList<CollidableObject> returnObjects,
                                         CollidableObject object) {
        RectF objRegion = object.getShape().getMinimumCoveredRect();
        int index = getIndex(objRegion);
        if (index != -1 && nodes[0] != null) {
            nodes[index].retrieve(returnObjects, object);
        }

        returnObjects.addAll(objects);

        return returnObjects;
    }

    private final int MAX_OBJECTS = 10;
    private final int MAX_LEVELS = 5;

    private int level;
    private ArrayList<CollidableObject> objects;
    private RectF region;
    private QuadTree[] nodes;

}
