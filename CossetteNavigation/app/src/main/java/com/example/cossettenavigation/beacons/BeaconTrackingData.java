package com.example.cossettenavigation.beacons;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.ArrayList;

/**
 * A collection of data for tracking a specific beacon, to be stored, updated, and queried over time.
 */
public class BeaconTrackingData {

    private static final String TAG = "BeaconTrackingData";

    private com.example.cossettenavigation.map.Beacon beacon;

    /**
     * Oldest measurements first, newest measurements last.
     */
    private ArrayList<Double> accuracyMeasurements = new ArrayList<>();

    /**
     * Oldest measurements first, newest measurements last.
     */
    private ArrayList<Utils.Proximity> proximityMeasurements = new ArrayList<>();




    public BeaconTrackingData(com.example.cossettenavigation.map.Beacon beacon) {
        this.beacon = beacon;
    }


    @Override
    public String toString() {
        String string = "BeaconTrackingData { accuracyMeasurements = { ";
        for (Double accuracy : accuracyMeasurements) {
            string += accuracy + ", ";
        }
        string += "}, proximityMeasurements = { ";
        for (Utils.Proximity proximity : proximityMeasurements) {
            string += proximity + ", ";
        }
        string += "}, estimatedAccuracy = " + getEstimatedAccuracy() + " }";

        return string;
    }


    public com.example.cossettenavigation.map.Beacon getBeacon() {
        return beacon;
    }

    public void addMeasurements(Beacon beacon) {
/*        Log.v(TAG, String.format(
                "addMeasurements(): \"%s\", \"%s\"",
                this.beacon.getName(), this.beacon.getDescription()));*/

        // Delete old measurements (keep a maximum of 5)
        if (accuracyMeasurements.size() >= 5) {
            accuracyMeasurements.remove(0);
        }
        if (proximityMeasurements.size() >= 5) {
            proximityMeasurements.remove(0);
        }

        // Add new measurements
        accuracyMeasurements.add(Utils.computeAccuracy(beacon));
        proximityMeasurements.add(Utils.computeProximity(beacon));
    }

    /**
     * @return The estimated distance of the beacon from the device (in metres).
     */
    public double getEstimatedAccuracy() {
        // Use a weighted average to estimate the distance
        // Newer measurements have greater weights

        double numerator = 0;
        double denominator = 0;

        double weight = 1;
        for (int i = 0; i < accuracyMeasurements.size(); i++) {
            numerator += (accuracyMeasurements.get(i) * weight);
            denominator += weight;

            weight *= 1.5;
        }

        if (denominator == 0) {
            return Double.POSITIVE_INFINITY;
        } else {
            return numerator / denominator;
        }
    }

}
