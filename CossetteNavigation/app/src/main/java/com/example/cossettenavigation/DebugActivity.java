package com.example.cossettenavigation;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cossettenavigation.beacons.ApplicationBeaconManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Displays information for debugging purposes (detected beacons, map, location, etc.).
 */
public class DebugActivity extends AppCompatActivity {

    private static final String TAG = "DebugActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Make the volume buttons control the text to speech volume (music stream)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);




        final ApplicationBeaconManager beaconManager = (ApplicationBeaconManager) getApplication();
        final TextView beaconList = (TextView) findViewById(R.id.beacon_list);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beaconList.setText(beaconManager.getTrackedBeaconsDescription());
                    }
                });
            }
        }, 100, 1000);




        LinearLayout mapLayout = (LinearLayout) findViewById(R.id.map_layout);

        LinearLayout.LayoutParams floorMapViewLayoutParams = new LinearLayout.LayoutParams(0, 0);
        floorMapViewLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        floorMapViewLayoutParams.weight = 1;
        final FloorMapView floorMapView = new FloorMapView(this, (ApplicationBeaconManager) getApplication());
        mapLayout.addView(floorMapView, floorMapViewLayoutParams);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        floorMapView.invalidate();
                    }
                });
            }
        }, 1 , 1000);

        // Multiple floor map views - test
/*        for (int i = 0; i < 2; i++) {
            // Floor map view
            LinearLayout.LayoutParams floorMapLayoutParams = new LinearLayout.LayoutParams(0, 0);
            floorMapLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            floorMapLayoutParams.weight = 1;
            mapLayout.addView(new FloorMapView(this), floorMapLayoutParams);

            // Separator (blank) view
            LinearLayout.LayoutParams separatorLayoutParams = new LinearLayout.LayoutParams(0, 0);
            separatorLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            separatorLayoutParams.height = 100;
            mapLayout.addView(new View(this), separatorLayoutParams);
        }*/
    }

}
