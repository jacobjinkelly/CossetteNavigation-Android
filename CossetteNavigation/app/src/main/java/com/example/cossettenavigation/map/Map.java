package com.example.cossettenavigation.map;

import android.util.Log;

import java.util.ArrayList;

/**
 * <p>
 *     Organizing class for mapping data.
 *     Uses a rectangular grid system to define the locations of beacons and zones.
 * </p>
 *
 * <p>
 *     Anchor Beacons - Placed in key locations (e.g. ends of hallways, doors, entrances and exits, stairs, elevators).
 * </p>
 *
 * <p>
 *     Support Beacons - Placed in supporting locations to improve location estimates (e.g. along hallways, middle of rooms).
 * </p>
 *
 * <p>
 *     Zones - Key areas within a floor or building (e.g. hallways, rectangular spaces, stairs, elevators).
 * </p>
 */
public class Map {

    private static final String TAG = "Map";

    // Average speeds, in metres/second
    private static final double WALKING_TRAVEL_SPEED = 1.0;
    private static final double STAIRS_TRAVEL_SPEED = 0.5;
    private static final double ELEVATOR_TRAVEL_SPEED = 0.5;

    private static final String DEFAULT_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    public static ArrayList<Floor> floors = new ArrayList<>();
    public static ArrayList<AnchorBeacon> anchorBeacons = new ArrayList<>();
    public static ArrayList<SupportBeacon> supportBeacons = new ArrayList<>();
    public static ArrayList<Zone> zones = new ArrayList<>();

    /*
        Grid properties.
        The grid is defined with arbitrary units, which can be converted to real distances with the given ratio.
     */
    public static double gridWidth = 1;
    public static double gridHeight = 1;
    public static double metresPerGridUnit = 1;




    public static ArrayList<Beacon> getAllBeacons() {
        ArrayList<Beacon> allBeacons = new ArrayList<>();
        allBeacons.addAll(anchorBeacons);
        allBeacons.addAll(supportBeacons);
        return allBeacons;
    }

    private static void setGridDimensions() {
        for (Beacon beacon : getAllBeacons()) {
            if (beacon.getXPosition() > gridWidth) {
                gridWidth = beacon.getXPosition();
            }
            if (beacon.getYPosition() > gridHeight) {
                gridHeight = beacon.getYPosition();
            }
        }
    }


    public static double distanceBetweenPoints(Point3D pointOne, Point3D pointTwo) {
        return Math.sqrt(
                Math.pow(pointTwo.x - pointOne.x, 2) +
                Math.pow(pointTwo.y - pointOne.y, 2) +
                Math.pow(pointTwo.z - pointOne.z, 2));
    }

    /**
     * Calculates the straight-line distance between two beacons.
     */
    public static double distanceBetweenBeacons(Beacon beaconOne, Beacon beaconTwo) {
        return distanceBetweenPoints(
                new Point3D(
                        beaconOne.getXPosition(),
                        beaconOne.getYPosition(),
                        beaconOne.getFloor().getZPosition()),
                new Point3D(
                        beaconTwo.getXPosition(),
                        beaconTwo.getYPosition(),
                        beaconTwo.getFloor().getZPosition()) );
    }

    /**
     * Assumes that both beacons are part of the same zone and have a straight-line connection.
     * @return The estimated travel time (in seconds) between the two beacons.
     */
    public static double estimateTravelTime(Beacon startBeacon, Beacon endBeacon, Zone zone) {
        double distance = distanceBetweenBeacons(startBeacon, endBeacon);
        double metres = distance * metresPerGridUnit;

        switch (zone.getZoneType()) {
            case HALLWAY:
                return metres / WALKING_TRAVEL_SPEED;
            case ROOM:
                return metres / WALKING_TRAVEL_SPEED;
            case STAIRS:
                return metres / STAIRS_TRAVEL_SPEED;
            case ELEVATOR:
                return metres / ELEVATOR_TRAVEL_SPEED;
            case ENTRANCE:
                return metres / WALKING_TRAVEL_SPEED;
            default:
                Log.e(TAG, "estimateTravelTime(): ZoneType not found");
                return metres / WALKING_TRAVEL_SPEED;
        }
    }

    /**
     * @return Angle of travel (clockwise from up), or null if there is no angle in the x-y plane.
     */
    public static Double estimateTravelAngle(Beacon startBeacon, Beacon endBeacon) {
        if (startBeacon.getXPosition() == endBeacon.getXPosition() &&
                startBeacon.getYPosition() == endBeacon.getYPosition()) {

            Log.e(TAG, String.format(
                    "estimateTravelAngle(): Beacons have the same x-y position\n%s\n%s",
                    startBeacon, endBeacon));
            return null;

        } else {

            return 90 - Math.toDegrees(Math.atan2(
                    endBeacon.getYPosition() - startBeacon.getYPosition(),
                    endBeacon.getXPosition() - startBeacon.getXPosition()));
        }
    }




    private static Floor addFloor(Floor floor) {
        floors.add(floor);
        return floor;
    }

    private static AnchorBeacon addAnchorBeacon(AnchorBeacon anchorBeacon) {
        anchorBeacons.add(anchorBeacon);
        return anchorBeacon;
    }

    private static SupportBeacon addSupportBeacon(SupportBeacon supportBeacon) {
        supportBeacons.add(supportBeacon);
        return supportBeacon;
    }

    private static Zone addZone(Zone zone) {
        zones.add(zone);
        return zone;
    }




    /*
        Define floors, beacons, and zones

        1. define floor
        2. define beacons -> associate with floor
        3. define zones -> add beacons
     */
    static {
        //Log.v(TAG, "static {}");


        // Start floor 1

        Floor floor1 = addFloor(new Floor("Floor 1", 0));

        AnchorBeacon white17 = addAnchorBeacon(new AnchorBeacon(
                "white17",
                "Front Entrance",
                floor1,
                5, 0,
                DEFAULT_UUID, 46447, 25300));

        SupportBeacon white5 = addSupportBeacon(new SupportBeacon(
                "white5",
                "Trophy Case",
                floor1,
                white17, -5, 8,
                DEFAULT_UUID, 33753, 28870));

        AnchorBeacon white10 = addAnchorBeacon(new AnchorBeacon(
                "white10",
                "Dynamite Stairs",
                floor1,
                white5, 2, 15,
                DEFAULT_UUID, 65261, 60647));

        AnchorBeacon white3 = addAnchorBeacon(new AnchorBeacon(
                "white3",
                "Grenade",
                floor1,
                white10, -2,20,
                DEFAULT_UUID, 9953, 12088));

        AnchorBeacon white11 = addAnchorBeacon(new AnchorBeacon(
                "white11",
                "Floor 1 Elevator",
                floor1,
                white10, -5, 0,
                DEFAULT_UUID, 18120, 25600));

        // End floor 1


        // Start floor 2

        Floor floor2 = addFloor(new Floor("Floor 2", floor1, 3));

        AnchorBeacon white15 = addAnchorBeacon(new AnchorBeacon(
                "white15",
                "Health Lab Staircase",
                floor2,
                white10, 2, 0,
                DEFAULT_UUID, 2949, 35856));

        AnchorBeacon white1 = addAnchorBeacon(new AnchorBeacon(
                "white1",
                "Health Lab Corridor",
                floor2,
                white15, 0, -8,
                DEFAULT_UUID, 6607, 59029));

        AnchorBeacon white25 = addAnchorBeacon(new AnchorBeacon(
                "white25",
                "East Staircase",
                floor2,
                white1, 2, -3,
                DEFAULT_UUID, 27415, 8243));

//        AnchorBeacon white19 = addAnchorBeacon(new AnchorBeacon(
//                "white19",
//                "Entrance Stair",
//                floor2,
//                white1, -3, -5,
//                DEFAULT_UUID, 21519, 1525));

        AnchorBeacon white18 = addAnchorBeacon(new AnchorBeacon(
                "white18",
                "Game Room & Elevator",
                floor2,
                white15,-12,0,
                DEFAULT_UUID, 3531, 48649));

        // End floor 2

        // Start floor 3

        Floor floor3 = addFloor(new Floor("Floor 3", floor2, 2));

        AnchorBeacon white12 = addAnchorBeacon(new AnchorBeacon(
                "white12",
                "West Patio Entrance",
                floor3,
                white15, 5, 0,
                DEFAULT_UUID, 64248, 32245));

        AnchorBeacon white24 = addAnchorBeacon(new AnchorBeacon(
                "white24",
                "East Patio Entrance",
                floor3,
                white25, 5,0,
                DEFAULT_UUID, 6433, 58059));

        // End floor 3


        // Zones

        Zone z5 = addZone(new Zone("Front Entrance",Zone.ZoneType.ENTRANCE, true));
        z5.addAnchorBeacons(white17);

        Zone z1 = addZone(new Zone("Main Hallway", Zone.ZoneType.HALLWAY, true));
        z1.addAnchorBeacons(white17, white10, white3);
        z1.addSupportBeacons(white5);

        Zone z6 = addZone(new Zone("Main Intersection", Zone.ZoneType.HALLWAY, true));
        z6.addAnchorBeacons(white10, white11);

        Zone z2 = addZone(new Zone("Middle Stairs", Zone.ZoneType.STAIRS, false));
        z2.addAnchorBeacons(white10, white15);

        Zone z4 = addZone(new Zone("Open Area", Zone.ZoneType.ROOM, true));
        z4.addAnchorBeacons(white25, /*white19,*/ white1);

        Zone z3 = addZone(new Zone("Health Lab", Zone.ZoneType.HALLWAY, true));
        z3.addAnchorBeacons(white15, white1);

        Zone z7 = addZone(new Zone("West Patio Stairs", Zone.ZoneType.STAIRS, false));
        z7.addAnchorBeacons(white15, white12);

        Zone z8 = addZone(new Zone("Elevator", Zone.ZoneType.ELEVATOR, false));
        z8.addAnchorBeacons(white18, white11);

        Zone z9 = addZone(new Zone("Games Room", Zone.ZoneType.HALLWAY, true));
        z9.addAnchorBeacons(white15,white18);

        Zone z10 = addZone(new Zone("East Patio Stairs", Zone.ZoneType.ELEVATOR,false));
        z10.addAnchorBeacons(white25, white24);

        Zone z11 = addZone(new Zone("Patio",Zone.ZoneType.ROOM,true));
        z11.addAnchorBeacons(white24,white12);

        // End Zones


        // Grid dimensions
        setGridDimensions();




        // Log all mapping data
/*        Log.v(TAG, String.format("Grid: %.0f x %.0f", gridWidth, gridHeight));
        for (Floor floor : floors) {
            Log.v(TAG, floor.toString());
        }
        for (AnchorBeacon anchorBeacon : anchorBeacons) {
            Log.v(TAG, anchorBeacon.toString());
        }
        for (SupportBeacon supportBeacon : supportBeacons) {
            Log.v(TAG, supportBeacon.toString());
        }
        for (Zone zone : zones) {
            Log.v(TAG, zone.toString());
        }*/




/*        ICE BEACONS that were used previously for debugging.
        AnchorBeacon ice1 = addAnchorBeacon(new AnchorBeacon(
                "ice1 - F2",
                floor2,
                20, 100,
                DEFAULT_UUID, 9051, 52752));
        AnchorBeacon ice2 = addAnchorBeacon(new AnchorBeacon(
                "ice2 - F2",
                floor2,
                0, 75,
                DEFAULT_UUID, 27598, 15040));
       AnchorBeacon ice3 = addAnchorBeacon(new AnchorBeacon(
                "ice3 - F2",
                floor2,
                10, 0,
                DEFAULT_UUID, 62693, 23343));
        AnchorBeacon ice4 = addAnchorBeacon(new AnchorBeacon(
                "ice4 - F2",
                floor2,
                50, 100,
                DEFAULT_UUID, 42484, 10171));


        Zone z1 = addZone(new Zone("Open Area - Floor 2", Zone.ZoneType.ROOM));
        z1.addAnchorBeacons(ice1, ice2, ice4);

        Zone z2 = addZone("2");
        Zone z3 = addZone("3");
        Zone z4 = addZone("4");
        Zone z5 = addZone("5");*/
    }

}
