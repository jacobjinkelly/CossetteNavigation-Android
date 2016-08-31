package com.example.cossettenavigation.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * An Estimote beacon on the map.
 * @see Map
 */
public abstract class Beacon implements Serializable {

    private static final String TAG = "Beacon";

    protected String name;
    protected String description;

    protected Floor floor;
    protected Point2D position;

    protected UUID uuid;
    protected int major;
    protected int minor;




    private Beacon(String name, String description, Floor floor, Point2D position, UUID uuid, int major, int minor) {
        this.name = name;
        this.description = description;
        this.floor = floor;
        this.position = position;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    /**
     * Constructor using an absolute position.
     */
    public Beacon(String name,
                  String description,
                  Floor floor,
                  double xPosition,
                  double yPosition,
                  String uuid,
                  int major,
                  int minor) {

        this(name, description, floor, new Point2D(xPosition, yPosition), UUID.fromString(uuid), major, minor);
    }

    /**
     * Constructor using a position relative to another beacon.
     */
    public Beacon(String name,
                  String description,
                  Floor floor,
                  Beacon referenceBeacon,
                  double xPositionOffset,
                  double yPositionOffset,
                  String uuid,
                  int major,
                  int minor) {

        this(
                name,
                description,
                floor,
                referenceBeacon.getXPosition() + xPositionOffset,
                referenceBeacon.getYPosition() + yPositionOffset,
                uuid,
                major,
                minor);
    }


    @Override
    public String toString() {
        return String.format(
                "%s { name = \"%s\", floor = \"%s\", position = %s, uuid = %s, major = %d, minor = %d }",
                getClass().getSimpleName(), name, floor.getName(), position, uuid, major, minor);
    }

    public int compareTo(Object another) {
        if (another instanceof Beacon) {
            Beacon anotherBeacon = (Beacon) another;
            return this.name.compareTo(anotherBeacon.name);
        }
        else {
            return 0;
        }
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Floor getFloor() {
        return floor;
    }

    public Point2D getPosition() {
        return position;
    }

    public double getXPosition() {
        return getPosition().x;
    }

    public double getYPosition() {
        return getPosition().y;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public abstract ArrayList<Zone> getZones();

}
