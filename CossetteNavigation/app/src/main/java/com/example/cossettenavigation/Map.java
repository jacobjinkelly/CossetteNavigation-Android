package com.example.cossettenavigation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Organizing class for mapping data.
 *
 * Uses a rectangular grid system to define "anchor beacons" and their location.
 * Other beacons ("support beacons") are defined relative to anchor beacons in zones.
 *
 * Created by Bruno on 2016-07-20.
 */
public class Map {

    private static List<Beacon> anchorBeacons = new ArrayList<>();

    private static final String DEFAULT_WHITE_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    /**
     * Adds a beacon to the set of anchor beacons using an absolute position.
     * @return The anchor beacon that was added.
     */
    private static Beacon addAbsolutePositionAnchorBeacon(String name,
                                                          double xPosition,
                                                          double yPosition,
                                                          String uuid,
                                                          int major,
                                                          int minor) {

        Beacon beacon = new Beacon(name, xPosition, yPosition, uuid, major, minor);
        anchorBeacons.add(beacon);
        return beacon;
    }

    private static Beacon addRelativePositionAnchorBeacon(String name,
                                                          Beacon referenceBeacon,
                                                          double xPositionOffset,
                                                          double yPositionOffset,
                                                          String uuid,
                                                          int major,
                                                          int minor) {

        return addAbsolutePositionAnchorBeacon(
                name,
                referenceBeacon.getXPosition() + xPositionOffset,
                referenceBeacon.getYPosition() + yPositionOffset,
                uuid,
                major,
                minor);
    }

    static {
        Beacon b1 = addAbsolutePositionAnchorBeacon(
                "white17 - Entrance",
                20, 0,
                "B9407F30-F5F8-466E-AFF9-25556B57FE6D", 46447, 25300);
        Beacon b2 = addRelativePositionAnchorBeacon(
                "white5 - Kitchen",
                b1, -5, 25,
                "B9407F30-F5F8-466E-AFF9-25556B57FE6D", 33753, 28870);
    }

    private static class Point {

        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

    }


    private static class Beacon {

        private String name;
        private Point position;

        private UUID uuid;
        private int major;
        private int minor;

        public Beacon(String name, Point position, UUID uuid, int major, int minor) {
            this.name = name;
            this.position = position;
            this.uuid = uuid;
            this.major = major;
            this.minor = minor;
        }

        public Beacon(String name, double xPosition, double yPosition, String uuid, int major, int minor) {
            this(name, new Point(xPosition, yPosition), UUID.fromString(uuid), major, minor);
        }

        public Point getPosition() {
            return position;
        }

        public double getXPosition() {
            return getPosition().getX();
        }

        public double getYPosition() {
            return getPosition().getY();
        }

    }


    private static class Zone {

        private String name;

        // A Zone does not own its anchor beacons, since they can define multiple zones
        private ArrayList<WeakReference<Beacon>> anchorBeacons = new ArrayList<>();

        // A zone owns its support beacons, since they are only part of that zone
        private ArrayList<Beacon> supportBeacons = new ArrayList<>();


        public Zone(String name) {
            this.name = name;
        }

        public void addAnchorBeacon(Beacon anchorBeacon) {
            anchorBeacons.add(new WeakReference(anchorBeacon));
        }

        public void addSupportBeacon(Beacon supportBeacon) {
            supportBeacons.add(supportBeacon);
        }

    }

}