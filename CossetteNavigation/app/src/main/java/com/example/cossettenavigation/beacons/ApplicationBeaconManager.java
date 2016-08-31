package com.example.cossettenavigation.beacons;

import android.app.Application;
import android.os.Build;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Pair;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.example.cossettenavigation.Utilities;
import com.example.cossettenavigation.map.Floor;
import com.example.cossettenavigation.map.Map;
import com.example.cossettenavigation.map.Point2D;
import com.example.cossettenavigation.map.Zone;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <h1>Global application state</h1>
 * <p>
 *     Primarily used to detect and manage beacons, but also manages text-to-speech and vibrations.
 * </p>
 *
 * <h2>Monitoring (background scanning)</h1>
 * <p>
 *     Monitoring is coarse, sending enter and exit events
 *     (takes a maximum of 30 seconds to detect beacons).
 * </p>
 *
 * <h2>Ranging (foreground scanning)</h2>
 * <p>
 *     Ranging is fine, providing power and approximate distance measurements
 *     (in 1 second intervals).
 * </p>
 *
 * @see <a href="http://developer.estimote.com/android/tutorial/part-1-setting-up/">Setup instructions</a>
 * @see <a href="http://developer.estimote.com/android/tutorial/part-2-background-monitoring/">Monitoring tutorial</a>
 * @see <a href="http://developer.estimote.com/android/tutorial/part-3-ranging-beacons/">Ranging tutorial</a>
 */
public class ApplicationBeaconManager extends Application {

    private final String TAG = "AppBeaconManager";

    private final Region ALL_BEACONS_REGION = new Region("All Beacons", null, null, null);

    /**
     * The range for beacons to be used in the location trilateration algorithm (in metres).
     */
    private static double MAX_BEACON_DISTANCE_FOR_TRILATERATION = 5;

    /**
     * The range for beacons to be classified as a nearby zone (in metres).
     */
    private static double BEACON_RANGE_FOR_NEARBY_ZONE = 10;

    /**
     * Tracks beacons, managing monitoring and ranging.
     */
    private BeaconManager beaconManager;

    /**
     * Set of beacons to be tracked over time (for location algorithms).
     */
    private HashMap<Region, BeaconTrackingData> trackedBeacons = new HashMap<>();

    /**
     * Tasks that will remove beacons from {@link #trackedBeacons} when not detected for a specific amount of time.
     */
    private HashMap<Region, TimerTask> removeTrackedBeaconTimerTasks = new HashMap<>();


    /**
     * True to enable, false to disable.
     */
    private boolean isTextToSpeechEnabled = true;

    private TextToSpeech textToSpeech = null;
    private boolean isTextToSpeechAvailable = false;




    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");

        super.onCreate();

        createTextToSpeech();

        // Initialize Map class
        Map map = new Map();

        // App ID & App Token can be taken from App section of Estimote Cloud.
        //EstimoteSDK.initialize(this, getString(R.string.app_name), getString(R.string.app_name));
        // Optional, debug logging.
        EstimoteSDK.enableDebugLogging(true);

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                logTrackedBeacons();
//                Log.v(TAG, getTrackedBeaconsDescription());
//
//                getEstimatedLocation();
//            }
//        }, 1, 1000);

        beaconManager = new BeaconManager(this);

        // Callback when the beacon manager has connected to the beacon service
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                Log.v(TAG, "BeaconManager.ServiceReadyCallback.onServiceReady()");

                setMonitoringListener();
                setRangingListener();

                startScanning();
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        destroyTextToSpeech();
    }




    private void setMonitoringListener() {
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.v(TAG, "BeaconManager.MonitoringListener.onEnteredRegion()");
                Log.v(TAG, region.toString());
                for (Beacon beacon : list) {
                    Log.v(TAG, beacon.toString());
                }

                if (list.size() > 0) {
                    updateTrackedBeacon(region, list.get(0));
                } else {
                    Log.v(TAG, "No beacons in region");
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.v(TAG, "BeaconManager.MonitoringListener.onExitedRegion()");
                Log.v(TAG, region.toString());

                removeTrackedBeacon(region);
            }
        });
    }


    private void setRangingListener() {
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                Log.v(TAG, "BeaconManager.RangingListener.onBeaconsDiscovered()");
                Log.v(TAG, region.toString());
                for (Beacon beacon : list) {
                    Log.v(TAG, beacon.toString());
                }

                if (list.size() > 0) {
                    updateTrackedBeacon(region, list.get(0));
                } else {
                    Log.v(TAG, "No beacons in region");
                }
            }
        });
    }




    private void startScanning() {
        // Monitor and range all beacons
        for (com.example.cossettenavigation.map.Beacon beacon : Map.getAllBeacons()) {
            Region region = new Region(
                    beacon.getName(),
                    beacon.getUUID(),
                    beacon.getMajor(),
                    beacon.getMinor());

            beaconManager.startMonitoring(region);
            beaconManager.startRanging(region);
        }
    }




    private void updateTrackedBeacon(final Region region, Beacon beacon) {
        Log.v(TAG, "updateTrackedBeacon()");

        Log.v(TAG, String.format(
                "Beacon: accuracy = %f, proximity = %s, %s",
                Utils.computeAccuracy(beacon), Utils.computeProximity(beacon), beacon));

        // Add the beacon if it isn't already tracked
        if (!trackedBeacons.containsKey(region)) {

            // Try to find the beacon in the map
            com.example.cossettenavigation.map.Beacon mapBeacon = null;

            for (com.example.cossettenavigation.map.Beacon testBeacon : Map.getAllBeacons()) {
                if (Utilities.areEqual(region, testBeacon)) {
                    mapBeacon = testBeacon;
                }
            }

            if (mapBeacon == null) {
                Log.e(TAG, String.format(
                        "updateTrackedBeacon(): Tracked beacon not found in map\nregion = %s\nbeacon = %s",
                        region, beacon));
                return;
            } else {
                trackedBeacons.put(region, new BeaconTrackingData(mapBeacon));
            }
        }

        Log.v(TAG, "updateTrackedBeacon(): " + trackedBeacons.get(region).getBeacon().toString());

        // The beacon must now be in the tracked set, so update it with measurements
        trackedBeacons.get(region).addMeasurements(beacon);

        // If a tracked beacon removal timer exists for this region, cancel it
        if (removeTrackedBeaconTimerTasks.containsKey(region)) {
            Log.v(TAG, String.format(
                    "updateTrackedBeacon(): Canceling timer task for \"%s\"",
                    trackedBeacons.get(region).getBeacon().getName()));

            removeTrackedBeaconTimerTasks.get(region).cancel();
            removeTrackedBeaconTimerTasks.remove(region);
        }

        // Set a timer to remove this tracked beacon in 5 seconds
        Log.v(TAG, String.format(
                "updateTrackedBeacon(): Scheduling timer task for \"%s\"",
                trackedBeacons.get(region).getBeacon().getName()));
        TimerTask removeTask = new TimerTask() {
            @Override
            public void run() {
                Log.v(TAG, String.format(
                        "updateTrackedBeacon(): Removing tracking data for \"%s\"",
                        trackedBeacons.get(region).getBeacon().getName()));
                removeTrackedBeacon(region);
            }
        };
        new Timer().schedule(removeTask, 5000);
        removeTrackedBeaconTimerTasks.put(region, removeTask);

        //Log.v(TAG, trackedBeacons.get(region).toString());
    }

    private void removeTrackedBeacon(Region region) {
        trackedBeacons.remove(region);
    }

    public BeaconTrackingData getBeaconTrackingData(com.example.cossettenavigation.map.Beacon beacon) {
        return trackedBeacons.get(new Region(
                beacon.getName(),
                beacon.getUUID(),
                beacon.getMajor(),
                beacon.getMinor()));
    }

    public Pair<Region, BeaconTrackingData> getNearestTrackedBeacon() {
        double minAccuracy = Double.POSITIVE_INFINITY;
        Pair<Region, BeaconTrackingData> nearest = null;

        for (HashMap.Entry<Region, BeaconTrackingData> trackedBeacon : trackedBeacons.entrySet()) {
            if (trackedBeacon.getValue().getEstimatedAccuracy() < minAccuracy) {
                minAccuracy = trackedBeacon.getValue().getEstimatedAccuracy();
                nearest = new Pair<>(trackedBeacon.getKey(), trackedBeacon.getValue());
            }
        }

        return nearest;
    }




    public ArrayList<Zone> getNearbyZones() {
        ArrayList<Zone> nearbyZones = new ArrayList<>();

        for (HashMap.Entry<Region, BeaconTrackingData> trackedBeacon : trackedBeacons.entrySet()) {
            if (trackedBeacon.getValue().getEstimatedAccuracy() <= BEACON_RANGE_FOR_NEARBY_ZONE) {
                ArrayList<Zone> zones = trackedBeacon.getValue().getBeacon().getZones();

                for (Zone zone : zones) {
                    if (!nearbyZones.contains(zone) && zone.getIsDestination()) {
                        nearbyZones.add(zone);
                    }
                }
            }
        }

        return nearbyZones;
    }

    public boolean getIsTextToSpeechEnabled() {
        return isTextToSpeechEnabled;
    }

    public void setIsTextToSpeechEnabled(boolean isTextToSpeechEnabled) {
        this.isTextToSpeechEnabled = isTextToSpeechEnabled;
    }

    /**
     * @see <a href="https://github.com/lemmingapex/Trilateration">Trilateration example</a>
     * @return Estimated location (on map grid), or null if not found.
     */
    public Point2D getEstimatedLocation() {
        // Get beacon positions and distances
        // Convert positions to metres
        // { { x, y }, { x, y }, ... }
        ArrayList<double[]> positions = new ArrayList<>();
        ArrayList<Double> distances = new ArrayList<>();

        // Loop through tracked beacons
        for (HashMap.Entry<Region, BeaconTrackingData> trackedBeacon : trackedBeacons.entrySet()) {
            if (trackedBeacon.getValue().getEstimatedAccuracy() <= MAX_BEACON_DISTANCE_FOR_TRILATERATION) {

                // Add position and distance (in metres)
                positions.add(new double[] {
                        trackedBeacon.getValue().getBeacon().getXPosition() * Map.metresPerGridUnit,
                        trackedBeacon.getValue().getBeacon().getYPosition() * Map.metresPerGridUnit });
                distances.add(trackedBeacon.getValue().getEstimatedAccuracy());
            }
        }


        // Trilaterate position

        // If there are 3 or more beacons (required for 2D triangulation)
        if (positions.size() >= 3) {
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
                    new TrilaterationFunction(
                            Utilities.getDoubleDoubleArray(positions), Utilities.getDoubleArray(distances)),
                    new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            // the answer
            double[] centroid = optimum.getPoint().toArray();

            // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
            /*RealVector standardDeviation = optimum.getSigma(0);
            RealMatrix covarianceMatrix = optimum.getCovariances(0);*/

            Point2D estimatedLocation = new Point2D(centroid[0] / Map.metresPerGridUnit,
                                                    centroid[1] / Map.metresPerGridUnit);

            //Log.v(TAG, "getEstimatedLocation(): " + estimatedLocation);

            return estimatedLocation;

        } else {
/*            Log.v(TAG, String.format(
                    "getEstimatedLocation(): Not enough beacons within %.1fm to trilaterate location",
                    MAX_BEACON_DISTANCE_FOR_TRILATERATION));*/

            return null;
        }
    }


    public ArrayList<BeaconTrackingData> getNearestBeacons() {
        ArrayList<BeaconTrackingData> beacons = new ArrayList<>();
        for (HashMap.Entry<Region, BeaconTrackingData> trackedBeacon : trackedBeacons.entrySet()){
            beacons.add(trackedBeacon.getValue());
        }
        return beacons;
    }

    public Floor getFloor() {
        HashMap<Floor, ArrayList<Integer>> floorMatrix = new HashMap();
        for (HashMap.Entry<Region, BeaconTrackingData> trackedBeacon : trackedBeacons.entrySet()) {
            Floor floor = trackedBeacon.getValue().getBeacon().getFloor();
            if (floorMatrix.containsKey(floor)) {
                ArrayList<Integer> tuple = floorMatrix.get(floor);
                tuple.set(0, tuple.get(0) + 1);
                tuple.set(1, tuple.get(1) + (int) Math.pow(trackedBeacon.getValue().getEstimatedAccuracy(), 2));
                floorMatrix.put(floor, tuple);
            } else {
                ArrayList<Integer> tuple = new ArrayList<>(2);
                tuple.add(0, 1);
                tuple.add(1, (int) Math.pow(trackedBeacon.getValue().getEstimatedAccuracy(), 2));
                floorMatrix.put(floor, tuple);
            }
        }
        Integer minDiff = Integer.MAX_VALUE;
        Floor closeFloor = null;
        for (Floor floor : Map.floors) {
            if (floorMatrix.containsKey(floor)) {
                Integer count = floorMatrix.get(floor).get(0);
                Integer sum = floorMatrix.get(floor).get(1);
                if (minDiff > sum - count) {
                    minDiff = sum - count;
                    closeFloor = floor;
                }
            }
        }
        return closeFloor;
    }

    public void logTrackedBeacons() {
        String string = "logTrackedBeacons():\n";

        for (java.util.Map.Entry<Region, BeaconTrackingData> beacon : trackedBeacons.entrySet()) {
            string += String.format(
                    "%s : %s\n",
                    beacon.getValue(), beacon.getKey());
        }

        Log.v(TAG, string);
    }

    public String getTrackedBeaconsDescription() {
        //Log.v(TAG, "getTrackedBeaconsDescription()");

        String string = "";

        for (java.util.Map.Entry<Region, BeaconTrackingData> entry : trackedBeacons.entrySet()) {
            string += String.format(
                    "%s : %.2f m\n",
                    entry.getKey().getIdentifier(), entry.getValue().getEstimatedAccuracy());
        }

        Point2D estimatedLocation = getEstimatedLocation();
        if (estimatedLocation == null) {
            string += "Location Unavailable";
        } else {
            string += String.format(
                    "(%.1f, %.1f)",
                    estimatedLocation.x, estimatedLocation.y);
        }

        return string;
    }




    private void createTextToSpeech() {
        if (isTextToSpeechEnabled) {
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        isTextToSpeechAvailable = true;

                        textToSpeech.setLanguage(Locale.CANADA);

                        Log.v(TAG, "createTextToSpeech(): success");
                    }

                    else {
                        isTextToSpeechAvailable = false;

                        Log.v(TAG, "createTextToSpeech(): error");
                    }
                }
            });
        }
    }

    private void destroyTextToSpeech() {
        Log.v(TAG, "destroyTextToSpeech()");

        if (isTextToSpeechAvailable) {
            textToSpeech.shutdown();
            isTextToSpeechAvailable = false;
        }
    }

    /**
     * Speaks the given text and vibrates the device.
     */
    public void speakText(String text) {
        if (isTextToSpeechEnabled && isTextToSpeechAvailable && Build.VERSION.SDK_INT >= 21) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");

            Vibrator v = (Vibrator) getSystemService(ApplicationBeaconManager.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

}
