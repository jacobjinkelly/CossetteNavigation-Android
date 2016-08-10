package com.example.cossettenavigation.map;

import android.util.Log;

import java.util.ArrayList;

/**
 * <p>
 * Organizing class for mapping data.
 * Uses a rectangular grid system to define the locations of beacons and zones.
 * </p>
 *
 * <p>
 * Anchor Beacons - Placed in key locations (e.g. ends of hallways, doors, entrances and exits, stairs, elevators).
 * </p>
 *
 * <p>
 * Support Beacons - Placed in supporting locations to improve location estimates (e.g. along hallways, middle of rooms).
 * </p>
 *
 * <p>
 * Zones - Key areas within a floor or building (e.g. hallways, rectangular spaces, stairs, elevators).
 * </p>
 */
public class Map {

    private static final String TAG = "Map";

    private static final String DEFAULT_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";


    public static ArrayList<AnchorBeacon> anchorBeacons = new ArrayList<>();
    public static ArrayList<Zone> zones = new ArrayList<>();

    /*
    Grid properties.
    The grid is defined with arbitrary units, which can be converted to real distances with the given ratio.
     */
    public static double gridWidth = 1;
    public static double gridHeight = 1;
    public static double metresPerGridUnit = 1;




    /**
     * Adds a beacon to the map.
     * @return The anchor beacon that was added.
     */
    private static AnchorBeacon addAnchorBeacon(AnchorBeacon anchorBeacon) {
        anchorBeacons.add(anchorBeacon);
        return anchorBeacon;
    }

    /**
     * Adds a beacon to the map using an absolute position.
     * @return The anchor beacon that was added.
     */
    private static AnchorBeacon addAnchorBeacon(String name,
                                                double xPosition,
                                                double yPosition,
                                                String uuid,
                                                int major,
                                                int minor) {

        AnchorBeacon anchorBeacon = new AnchorBeacon(name, xPosition, yPosition, uuid, major, minor);
        return addAnchorBeacon(anchorBeacon);
    }

    /**
     * Adds a beacon to the map using a position relative to another beacon.
     * @return The anchor beacon that was added.
     */
    private static AnchorBeacon addAnchorBeacon(String name,
                                                Beacon referenceBeacon,
                                                double xPositionOffset,
                                                double yPositionOffset,
                                                String uuid,
                                                int major,
                                                int minor) {

        AnchorBeacon anchorBeacon = new AnchorBeacon(
                name,
                referenceBeacon, xPositionOffset, yPositionOffset,
                uuid, major, minor);
        return addAnchorBeacon(anchorBeacon);
    }

    /**
     * Adds a zone to the map.
     * @return The zone that was added.
     */
    private static Zone addZone(String name) {
        Zone zone = new Zone(name);
        zones.add(zone);
        return zone;
    }




    private static void addOtherBeacons() {
        AnchorBeacon b1 = addAnchorBeacon(
                "white1 - F1",
                15, 40,
                DEFAULT_UUID, 6607, 59029);
        AnchorBeacon b2 = addAnchorBeacon(
                "white2 - F1",
                30, 20,
                DEFAULT_UUID, 62315, 20156);
    }

    private static void addFloor1() {
        gridWidth = 30;
        gridHeight = 100;

        AnchorBeacon b1 = addAnchorBeacon(
                "white17 - F1",
                20, 100,
                DEFAULT_UUID, 46447, 25300);
        AnchorBeacon b2 = addAnchorBeacon(
                "white1 - F1",
                15, 40,
                DEFAULT_UUID, 6607, 59029);
        AnchorBeacon b3 = addAnchorBeacon(
                "white2 - F1",
                30, 20,
                DEFAULT_UUID, 62315, 20156);

/*        // TODO - make a support beacon
        AnchorBeacon b4 = addAnchorBeacon(
                "white5 - Kitchen",
                b1, -5, 25,
                DEFAULT_UUID, 33753, 28870);*/

        Zone z1 = addZone("Main Lower Hallway");
        z1.addAnchorBeacons(b1, b2, b3);
//        z1.addSupportBeacons(b4);
    }

    /**
     * Roughly a 10m x 10m grid.
     */
    private static void addFloor2() {
        gridWidth = 100;
        gridHeight = 100;
        metresPerGridUnit = 0.1;

        AnchorBeacon ice1 = addAnchorBeacon(
                "ice1 - F2",
                20, 100,
                DEFAULT_UUID, 9051, 52752);
        AnchorBeacon ice2 = addAnchorBeacon(
                "ice2 - F2",
                0, 75,
                DEFAULT_UUID, 27598, 15040);
/*        AnchorBeacon ice3 = addAnchorBeacon(
                "ice3 - F2",
                10, 0,
                DEFAULT_UUID, 62693, 23343);*/
        AnchorBeacon ice4 = addAnchorBeacon(
                "ice4 - F2",
                50, 100,
                DEFAULT_UUID, 42484, 10171);

        Zone z1 = addZone("Open Area - Floor 2");
        Zone z2 = addZone("2");
        Zone z3 = addZone("3");
        Zone z4 = addZone("4");
        Zone z5 = addZone("5");
        z1.addAnchorBeacons(ice1, ice2, /*ice3,*/ ice4);
    }

    // Define beacons and zones
    static {
        Log.v(TAG, "static initializer");

        addFloor2();

/*        for (AnchorBeacon anchorBeacon : anchorBeacons) {
            Log.v(TAG, anchorBeacon.toString());
        }
        for (Zone zone : zones) {
            Log.v(TAG, zone.toString());
        }*/
    }

}
