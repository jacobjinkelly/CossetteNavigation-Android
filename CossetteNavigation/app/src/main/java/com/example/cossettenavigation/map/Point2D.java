package com.example.cossettenavigation.map;

import java.io.Serializable;

/**
 * A 2-dimensional point with double precision.
 */
public class Point2D implements Serializable {

    public double x;
    public double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format(
                "%s { x = %.1f units, y = %.1f units }",
                getClass().getSimpleName(), x, y);
    }

}
