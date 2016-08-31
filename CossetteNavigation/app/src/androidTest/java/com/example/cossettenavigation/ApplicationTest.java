package com.example.cossettenavigation;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.example.cossettenavigation.map.Beacon;
import com.example.cossettenavigation.map.Map;
import com.example.cossettenavigation.pathfinding.Path;
import com.example.cossettenavigation.pathfinding.Pathfinder;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    private static final String TAG = "ApplicationTest";

    public ApplicationTest() {
        super(Application.class);
    }

    /**
     * Tests that any two unique beacons have different x-y coordinates.
     */
    public void test_estimateTravelAngle() {
        Map map = new Map();

        for (Beacon beacon1 : Map.getAllBeacons()) {
            for (Beacon beacon2 : Map.getAllBeacons()) {
                if (beacon1 != beacon2) {
                    Double travelAngle = Map.estimateTravelAngle(beacon1, beacon2);

                    Log.v(TAG, String.format(
                            "test_estimateTravelAngle():\n%s\n%s\n%s deg",
                            beacon1,
                            beacon2,
                            (travelAngle != null) ? String.format("%.0f", travelAngle) : "null"));

                    assertNotNull(String.format(
                            "A travel angle between any two unique beacons should exist (beacons should have unique x-y coordinates).\n%s\n%s",
                            beacon1, beacon2),
                            travelAngle);
                }
            }
        }
    }

    /**
     * Tests that a path exists between any two beacons.
     */
    public void test_getShortestPath() {
        for (Beacon beacon1 : Map.getAllBeacons()) {
            for (Beacon beacon2 : Map.getAllBeacons()) {
                Log.v(TAG, String.format(
                        "test_getShortestPath():\n%s\n%s",
                        beacon1, beacon2));

                Path path = Pathfinder.getShortestPath(beacon1, beacon2);

                if (path == null) {
                    Log.v(TAG, "No path");
                } else {
                    Log.v(TAG, path.toString());
                }

                assertNotNull("path = null: A path should exist", path);
            }
        }
    }

}
