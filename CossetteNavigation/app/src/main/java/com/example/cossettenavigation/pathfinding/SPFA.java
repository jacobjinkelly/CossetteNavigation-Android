package com.example.cossettenavigation.pathfinding;

import android.util.Log;
import android.util.Pair;

import com.example.cossettenavigation.map.AnchorBeacon;
import com.example.cossettenavigation.map.Beacon;
import com.example.cossettenavigation.map.Map;
import com.example.cossettenavigation.map.Zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * <h1>Shortest Path Faster Algorithm</h1>
 *
 * <p>
 *     Calculates the minimum distance from the start node to the end node in a weighted, undirected graph.
 * </p>
 *
 * <p>
 *     Each use of the algorithm requires its own object.
 *     Should not be used directly by client code; use {@link Pathfinder} instead.
 * </p>
 *
 * @see Pathfinder
 * @see <a href="https://en.wikipedia.org/wiki/Shortest_Path_Faster_Algorithm">SPFA Wiki</a>
 */
class SPFA {

    private static final String TAG = "SPFA";

    /**
     * <h1>Graph (adjacency list)</h1>
     * <p>AnchorBeacon -> { (connected beacon, travel time), ... }</p>
     * <p>
     *     The graph is the same for all algorithm uses, so it belongs to the class
     *     and should only be constructed once.
     * </p>
     */
    private static HashMap<AnchorBeacon, ArrayList<Pair<AnchorBeacon, Double>>> graph = null;

    private AnchorBeacon startBeacon;
    private AnchorBeacon endBeacon;

    /**
     * AnchorBeacon -> (shortest travel time from root, previous beacon in shortest path)
     */
    private HashMap<AnchorBeacon, Pair<Double, AnchorBeacon>> shortestTravelTimes;


    SPFA(AnchorBeacon startBeacon, AnchorBeacon endBeacon) {
        // If the graph doesn't exist yet, construct it
        if (graph == null) {
            graph = new HashMap<>();

            // Go through all anchor beacons
            for (AnchorBeacon beacon : Map.anchorBeacons) {

                // Connections for this anchor beacon
                ArrayList<Pair<AnchorBeacon, Double>> beaconConnections = new ArrayList<>();

                // Go through all zones this anchor beacon is part of
                for (Zone zone : beacon.getZones()) {

                    // Go through all anchor beacons in this zone
                    for (AnchorBeacon connectedBeacon : zone.getAnchorBeacons()) {

                        // Check that the anchor beacons are different
                        if (beacon != connectedBeacon) {

                            // Add connection
                            beaconConnections.add(new Pair<>(
                                    connectedBeacon,
                                    Map.estimateTravelTime(beacon, connectedBeacon, zone)));
                        }
                    }
                }

                graph.put(beacon, beaconConnections);
            }
        }
        
        this.startBeacon = startBeacon;
        this.endBeacon = endBeacon;

        // Set all travel times to infinity
        shortestTravelTimes = new HashMap<>();
        for (AnchorBeacon beacon : graph.keySet()) {
            shortestTravelTimes.put(beacon, new Pair<Double, AnchorBeacon>(Pathfinder.INFINITY, null));
        }
    }


    /**
     * Runs the algorithm and gets the shortest path result.
     * @return (shortest travel time in seconds, path of beacons), or null if no path was found.
     */
    Pair<Double, ArrayList<Beacon>> getShortestPath() {
        run();
        return getResult();
    }




    /**
     * Runs the algorithm, storing the results in `shortestTravelTimes`.
     */
    private void run() {
        // List of nodes to be visited next
        PriorityQueue<AnchorBeacon> queue = new PriorityQueue<>();

        // Setup root node
        shortestTravelTimes.put(startBeacon, new Pair<Double, AnchorBeacon>(0.0, null));
        queue.offer(startBeacon);

        while (queue.size() > 0) {
            // Get current beacon
            AnchorBeacon currentBeacon = queue.poll();

            // Go through current beacon's connections
            for (Pair<AnchorBeacon, Double> connection : graph.get(currentBeacon)) {
                AnchorBeacon connectedBeacon = connection.first;
                double connectionTime = connection.second;

                // If necessary, update the connected beacon's shortest travel time and add it to the queue
                double testTravelTime = shortestTravelTimes.get(currentBeacon).first + connectionTime;
                if (testTravelTime < shortestTravelTimes.get(connectedBeacon).first) {
                    shortestTravelTimes.put(connectedBeacon, new Pair<>(testTravelTime, currentBeacon));
                    if (!queue.contains(connectedBeacon)) {
                        queue.offer(connectedBeacon);
                    }
                }
            }
        }
    }


    /**
     * Gets the algorithm result from `shortestTravelTimes`.
     * @return (shortest travel time in seconds, path of beacons), or null if no path was found.
     */
    private Pair<Double, ArrayList<Beacon>> getResult() {
        // No result found
        if (shortestTravelTimes.get(endBeacon).first == Pathfinder.INFINITY) {
            Log.e(TAG, "getResult(): No path found");

            return null;

        // Result found
        } else {
            Log.v(TAG, "getResult(): Path found");

            return new Pair<>(shortestTravelTimes.get(endBeacon).first, constructPath(endBeacon));
        }
    }


    /**
     * Constructs the path stored in {@link #shortestTravelTimes} up to the given node.
     */
    private ArrayList<Beacon> constructPath(AnchorBeacon finalBeacon) {
        ArrayList<Beacon> path = new ArrayList<>();

        path.add(finalBeacon);

        AnchorBeacon currentBeacon = finalBeacon;
        while (shortestTravelTimes.get(currentBeacon).second != null) {
            currentBeacon = shortestTravelTimes.get(currentBeacon).second;
            path.add(0, currentBeacon);
        }

        return path;
    }

}
