package com.example.cossettenavigation.pathfinding;

import android.util.Log;
import android.util.Pair;

import com.example.cossettenavigation.map.AnchorBeacon;
import com.example.cossettenavigation.map.Beacon;
import com.example.cossettenavigation.map.Map;
import com.example.cossettenavigation.map.SupportBeacon;
import com.example.cossettenavigation.map.Zone;

import java.util.ArrayList;

/**
 * Interface for the shortest path algorithm.
 */
public class Pathfinder {

    private static final String TAG = "Pathfinder";

    static double INFINITY = Double.POSITIVE_INFINITY;

    /**
     * Determines the shortest path between two beacons.
     * @return The shortest path by travel time, or null if no path is found.
     */
    public static Path getShortestPath(Beacon startBeacon, Beacon endBeacon) {
        // If starting and ending at the same beacon, return an empty path
        if (startBeacon == endBeacon) {
            return new Path(0, new ArrayList<Step>());
        }


        Pair<Double, ArrayList<Beacon>> result = null;

        if (startBeacon instanceof SupportBeacon) {
            result = getShortestPath((SupportBeacon) startBeacon, endBeacon);
        }

        else if (startBeacon instanceof AnchorBeacon) {
            result = getShortestPath((AnchorBeacon) startBeacon, endBeacon);
        }

        else {
            Log.e(TAG, "getShortestPath(Beacon, Beacon): Invalid startBeacon type");
            return null;
        }


        if (result == null) {
            return null;

        } else {
            // Decompose results
            double travelTime = result.first;
            ArrayList<Beacon> beacons = result.second;

            ArrayList<Step> steps = new ArrayList<>();

            // Generate each step
            for (int i = 0; i < beacons.size() - 1; i++) {
                Beacon beaconOne = beacons.get(i);
                Beacon beaconTwo = beacons.get(i + 1);

                // Find the common zone
                Zone zone = null;
                for (Zone beaconOneZone : beaconOne.getZones()) {
                    if (beaconTwo.getZones().contains(beaconOneZone)) {
                        zone = beaconOneZone;
                    }
                }

                if (zone == null) {
                    Log.e(TAG, "getShortestPath(Beacon, Beacon): Zone for Step " + i + " not found");
                    return null;

                } else {
                    // Get angles

                    Double travelAngle = Map.estimateTravelAngle(beaconOne, beaconTwo);

                    double turnAngle;
                    if (    (i > 0) &&
                            (travelAngle != null) &&
                            (steps.get(i - 1).getTravelAngle() != null) ) {
                        turnAngle = travelAngle - steps.get(i - 1).getTravelAngle();
                    } else {
                        turnAngle = 0;
                    }

                    steps.add(new Step(beaconOne, beaconTwo, zone, travelAngle, turnAngle));
                }
            }

            return new Path(travelTime, steps);
        }
    }


    private static Pair<Double, ArrayList<Beacon>> getShortestPath(SupportBeacon startBeacon, Beacon endBeacon) {
        double minimumTime = INFINITY;
        ArrayList<Beacon> shortestPath = null;

        for (AnchorBeacon anchorBeacon : startBeacon.getZone().getAnchorBeacons()) {

            Pair<Double, ArrayList<Beacon>> testPath = getShortestPath(anchorBeacon, endBeacon);

            if (testPath != null) {
                double testTime = Map.estimateTravelTime(startBeacon, anchorBeacon, startBeacon.getZone()) + testPath.first;

                if (testTime < minimumTime) {
                    minimumTime = testTime;
                    shortestPath = testPath.second;
                    shortestPath.add(0, startBeacon);
                }
            }
        }

        if (shortestPath != null) {
            return new Pair<>(minimumTime, shortestPath);
        } else {
            Log.e(TAG, "getShortestPath(SupportBeacon, Beacon): No path found");
            return null;
        }
    }


    private static Pair<Double, ArrayList<Beacon>> getShortestPath(AnchorBeacon startBeacon, Beacon endBeacon) {
        if (endBeacon instanceof SupportBeacon) {
            return getShortestPath(startBeacon, (SupportBeacon) endBeacon);
        }

        else if (endBeacon instanceof AnchorBeacon) {
            return getShortestPath(startBeacon, (AnchorBeacon) endBeacon);
        }

        else {
            Log.e(TAG, "getShortestPath(AnchorBeacon, Beacon): Invalid endBeacon type");
            return null;
        }
    }


    private static Pair<Double, ArrayList<Beacon>> getShortestPath(AnchorBeacon startBeacon, SupportBeacon endBeacon) {
        double minimumTime = INFINITY;
        ArrayList<Beacon> shortestPath = null;

        for (AnchorBeacon anchorBeacon : endBeacon.getZone().getAnchorBeacons()) {
            Pair<Double, ArrayList<Beacon>> testPath = getShortestPath(startBeacon, anchorBeacon);

            if (testPath != null) {
                double testTime = testPath.first + Map.estimateTravelTime(anchorBeacon, endBeacon, endBeacon.getZone());

                if (testTime < minimumTime) {
                    minimumTime = testTime;
                    shortestPath = testPath.second;
                    shortestPath.add(endBeacon);
                }
            }
        }

        if (shortestPath != null) {
            return new Pair<>(minimumTime, shortestPath);
        } else {
            Log.e(TAG, "getShortestPath(AnchorBeacon, SupportBeacon): No path found");
            return null;
        }
    }


    private static Pair<Double, ArrayList<Beacon>> getShortestPath(AnchorBeacon startBeacon, AnchorBeacon endBeacon) {
        return new SPFA(startBeacon, endBeacon).getShortestPath();
    }

}
