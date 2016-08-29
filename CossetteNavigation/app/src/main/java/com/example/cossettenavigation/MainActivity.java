package com.example.cossettenavigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.example.cossettenavigation.beacons.ApplicationBeaconManager;
import com.example.cossettenavigation.beacons.BeaconTrackingData;
import com.example.cossettenavigation.map.Beacon;
import com.example.cossettenavigation.pathfinding.NavigationStep;
import com.example.cossettenavigation.pathfinding.Path;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "MainActivity";

    private static int PERMISSION_REQUEST_CODE_CAMERA = 1;

    public static final String INTENT_KEY_PATH = "path";

    /**
     * Range (in metres) for switching steps during navigation.
     */
    private static double BEACON_RANGE_FOR_SWITCHING_STEPS = 3;

    private boolean mVisible; //UI elements (status bar, toolbar, bottom bar visible)
    private boolean cVisible; //camera visible
    private boolean cGranted; //camera permission granted

    private MenuItem cameraToggle;
    private MenuItem audioToggle;

    private FrameLayout m_camera_view = null;
    private CameraView mCameraView = null;
    private LinearLayout bottomBar;

    //UI elements (all for navigation mode, some for discovery mode)
    private RelativeLayout toggleArrows;
    private ImageView toggleUp;
    private TextView stepNumber;
    private ImageView toggleDown;
    private ImageView direction;
    private TextView instruction;
    private TextView description;
    private TextView time;
    private FloatingActionButton FAB;

    private ApplicationBeaconManager beaconManager;

    private Timer discoveryTimer = new Timer();

    private Timer navigationTimer = new Timer();

    private Path path = null;
    private ArrayList<NavigationStep> navigationSteps = new ArrayList<>();

    private Timer navigationStepTimer = new Timer();
    private int navigationStepIndex = 0;
    /**
     * Determined by minimum time.
     */
    private boolean canChangeNavigationStep = true;
    /**
     * Determined by proximity to the next beacon.
     */
    private boolean shouldChangeNavigationStep = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        // Make the volume buttons control the text to speech volume (music stream)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_camera_view = (FrameLayout) findViewById(R.id.camera_view);

        mVisible = true;

        // Set up the user interaction to manually show or hide the UI.
        m_camera_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Request the write permission on Android 6.0+
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA },
                    PERMISSION_REQUEST_CODE_CAMERA);
        } else {
            cameraPermissionGranted();
        }

        bottomBar=(LinearLayout) findViewById(R.id.bottomBar);

        direction=(ImageView) findViewById(R.id.arrow);
        direction.bringToFront();
        toggleArrows=(RelativeLayout) findViewById(R.id.toggleArrows);
        instruction=(TextView) findViewById(R.id.instruction);
        time=(TextView) findViewById(R.id.time);
        description=(TextView) findViewById(R.id.description);

        //get FAB
        FAB=(FloatingActionButton) findViewById(R.id.FAB);

        beaconManager = (ApplicationBeaconManager) getApplication();

        final TextView debugView = (TextView) findViewById(R.id.debug_view);

        // Periodic general UI update
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Show debug info
                        debugView.setText(beaconManager.getTrackedBeaconsDescription());
                    }
                });
            }
        }, 1, 100);


        // Check intent for a Path object - discovery or navigation mode
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            path = (Path) extras.getSerializable(INTENT_KEY_PATH);

            if (path != null) {
                navigationSteps = path.toNavigationSteps();
                enterNavigationMode();
            } else {
                enterDiscoveryMode();
            }
        }
        else {
            enterDiscoveryMode();
        }
    }




    /* Discovery */

    private void enterDiscoveryMode() {
        Log.v(TAG, "enterDiscoveryMode()");

        exitNavigationMode();

        FAB.setImageResource(R.drawable.search);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });

        // Discovery UI updating
        discoveryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Floor floor=beaconManager.getFloor();
//                        ArrayList<BeaconTrackingData> beacons=beaconManager.getNearestBeacons();
//
//                        Double minDistance=Double.POSITIVE_INFINITY;
//                        Beacon nearestBeacon=null;
//
//                        for (BeaconTrackingData beaconData:beacons){
//                            if (beaconData.getBeacon().getFloor()==floor&&minDistance>beaconData.getEstimatedAccuracy()){
//                                minDistance=beaconData.getEstimatedAccuracy();
//                                nearestBeacon=beaconData.getBeacon();
//                            }
//                        }

                        Pair<Region, BeaconTrackingData> nearestTrackedBeacon = beaconManager.getNearestTrackedBeacon();

                        if (nearestTrackedBeacon != null) {
                            instruction.setText(nearestTrackedBeacon.second.getBeacon().getDescription());
                            description.setText(nearestTrackedBeacon.second.getBeacon().getFloor().getName());
                        } else {
                            instruction.setText("Unknown Location");
                            description.setText("No Beacons Found");
                        }
                    }
                });
            }
        }, 1, 100);
    }

    private void exitDiscoveryMode() {
        Log.i(TAG, "exitDiscoveryMode()");

        resetDiscoveryTimer();
    }




    /* Navigation */

    private void enterNavigationMode() {
        Log.i(TAG, "enterNavigationMode():");
        Log.i(TAG, path.toString());
        for (NavigationStep navigationStep : navigationSteps) {
            Log.i(TAG, navigationStep.toString());
        }

        exitDiscoveryMode();

        FAB.setImageResource(R.drawable.close);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterDiscoveryMode();
            }
        });

        bottomBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                /*//bottomBar.setBackgroundColor(android.R.color.holo_red_light);
                LinearLayout helpResize=(LinearLayout) findViewById(R.id.helpResize);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) helpResize.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 156, getResources().getDisplayMetrics());;
                bottomBar.setLayoutParams(params);*/
                return true;
            }
        });

        direction.setVisibility(View.VISIBLE);
        toggleArrows.setVisibility(View.VISIBLE);
        time.setVisibility(View.VISIBLE);

        toggleUp=(ImageView) findViewById(R.id.toggleUp);
        stepNumber = (TextView) findViewById(R.id.stepNumber);
        toggleDown=(ImageView) findViewById(R.id.toggleDown);

        toggleUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "enterNavigationMode(): toggleUp.setOnClickListener()");

                canChangeNavigationStep = true;
                shouldChangeNavigationStep = true;
                decreaseNavigationStepIndex();
            }
        });

        toggleDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "enterNavigationMode(): toggleDown.setOnClickListener()");

                canChangeNavigationStep = true;
                shouldChangeNavigationStep = true;
                increaseNavigationStepIndex();
            }
        });

        //limit on number of characters
/*        instruction.setText("Walk 4 m ahead");
        time.setText("20 min");
        description.setText("Top of North Stairwell");
        nextStep.setText("Walk down staircase");*/

        // Navigation loop
        navigationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v(TAG, "enterNavigationMode(): navigationTimer.schedule()");

                updateStep();
            }
        }, 1, 1);
    }

    private void exitNavigationMode() {
        Log.v(TAG, "exitNavigationMode()");

        resetNavigationTimer();
        resetNavigationStepTimer();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                direction.setVisibility(View.GONE);
                toggleArrows.setVisibility(View.GONE);
                time.setVisibility(View.GONE);
            }
        });
    }





    private void updateStep() {
        Log.v(TAG, "updateStep()");

        if (canChangeNavigationStep && shouldChangeNavigationStep) {
            resetNavigationStepTimer();
            canChangeNavigationStep = false;
            shouldChangeNavigationStep = false;


            final NavigationStep navigationStep = navigationSteps.get(navigationStepIndex);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Direction arrow
                    if (navigationStep.getArrowAngle() != null) {
                        direction.setVisibility(View.VISIBLE);
                        direction.setRotation((float) navigationStep.getArrowAngle().doubleValue());
                    } else {
                        direction.setVisibility(View.INVISIBLE);
                    }

                    // Up toggle
                    if (navigationStepIndex > 0) {
                        toggleUp.setAlpha(255);
                    } else {
                        toggleUp.setAlpha(50);
                    }

                    // Down toggle
                    if (navigationStepIndex < navigationSteps.size() - 1) {
                        toggleDown.setAlpha(255);
                    } else {
                        toggleDown.setAlpha(50);
                    }

                    stepNumber.setText(String.format(
                            "%d/%d",
                            navigationStepIndex + 1, navigationSteps.size()));
                    instruction.setText(navigationStep.getDescriptionOne());
                    description.setText(navigationStep.getDescriptionTwo());
                    time.setText(String.format("%.0fs", navigationStep.getTimeRemaining()));
                    beaconManager.speakText(navigationStep.getDescriptionOne());
                }
            });

            // Timer to allow minimum time for step
            if (navigationStep.getMinimumTime() > 0) {
                navigationStepTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.i(TAG, "updateStep(): navigationStepTimer.schedule() - minimum time");
                        canChangeNavigationStep = true;
                    }
                }, (long) (navigationStep.getMinimumTime() * 1000));
            } else {
                canChangeNavigationStep = true;
            }

            // Timer to determine when in beacon range to switch to next step
            if (navigationStep.getEndBeacon() != null) {
                navigationStepTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.v(TAG, "updateStep(): navigationStepTimer.schedule() - beacon range");
                        if (canChangeNavigationStep && isInRangeOfBeacon(navigationStep.getEndBeacon())) {
                            increaseNavigationStepIndex();
                        }
                    }
                }, 1, 200);
            }
        }
    }

    private boolean isInRangeOfBeacon(Beacon beacon) {
        Log.v(TAG, "isInRangeOfBeacon()");

        BeaconTrackingData beaconTrackingData = beaconManager.getBeaconTrackingData(beacon);
        if (beaconTrackingData != null) {
            if (beaconTrackingData.getEstimatedAccuracy() <= BEACON_RANGE_FOR_SWITCHING_STEPS) {
                return true;
            }
        }

        return false;
    }




    private void resetDiscoveryTimer() {
        Log.i(TAG, "resetDiscoveryTimer()");

        discoveryTimer.cancel();
        discoveryTimer.purge();
        discoveryTimer = new Timer();
    }

    private void resetNavigationTimer() {
        Log.v(TAG, "resetNavigationTimer()");

        navigationTimer.cancel();
        navigationTimer.purge();
        navigationTimer = new Timer();
    }

    private void resetNavigationStepTimer() {
        Log.v(TAG, "resetNavigationStepTimer()");

        navigationStepTimer.cancel();
        navigationStepTimer.purge();
        navigationStepTimer = new Timer();
    }

    private void decreaseNavigationStepIndex() {
        Log.i(TAG, "decreaseNavigationStepIndex()");

        if (navigationStepIndex > 0) {
            navigationStepIndex--;
            shouldChangeNavigationStep = true;
        }
    }

    private void increaseNavigationStepIndex() {
        Log.i(TAG, "increaseNavigationStepIndex");

        if (navigationStepIndex < navigationSteps.size() - 1) {
            navigationStepIndex++;
            shouldChangeNavigationStep = true;
        }
    }




    private void toggleAudio() {
        if (beaconManager.getIsTextToSpeechEnabled()) {
            audioToggle.setIcon(R.drawable.ic_volume_off_white_48px);
            beaconManager.setIsTextToSpeechEnabled(false);
        } else {
            audioToggle.setIcon(R.drawable.ic_volume_up_white_48px);
            beaconManager.setIsTextToSpeechEnabled(true);
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult()");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                cameraPermissionGranted();
            } else {
                cGranted=false;
            }
        }
    }

    private void cameraPermissionGranted() {
        Log.v(TAG, "Camera permission granted");
        cGranted=true;
        cVisible=true;
        mCameraView = new CameraView(this); // create a SurfaceView to show camera data
        FrameLayout camera_view = (FrameLayout) findViewById(R.id.camera_view);
        camera_view.addView(mCameraView);   // add the SurfaceView to the layout
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        cameraToggle = menu.findItem(R.id.camera_button);
        audioToggle = menu.findItem(R.id.audio_button);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*            case R.id.action_debug:
                Intent intent = new Intent(this, DebugActivity.class);
                startActivity(intent);
                return true;*/
            case R.id.camera_button:
                cameraOnOff();
                return true;
            case R.id.audio_button:
                toggleAudio();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        if (mCameraView != null) {
            mCameraView.activityOnResume();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void cameraOnOff() {
        if (cGranted) {
            if (cVisible) {
                cameraToggle.setIcon(R.drawable.ic_videocam_off_white_48px);
                hideCamera();
            } else {
                cameraToggle.setIcon(R.drawable.ic_videocam_white_48px);
                showCamera();
            }
        }
    }
    
    private void hideCamera(){
        mCameraView.setVisibility(View.INVISIBLE);
        cVisible=false;
    }

    private void showCamera(){
        mCameraView=new CameraView(this);
        m_camera_view.addView(mCameraView);
        direction.bringToFront();
        cVisible=true;
    }

    private void toggle() {
        /*if (mVisible) hide();
        else show();*/
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        View decorView = getWindow().getDecorView();
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.coordinatorLayout);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        //rootView.setFitsSystemWindows(false);
        //decorView.setSystemUiVisibility(uiOptions);

        m_camera_view.setSystemUiVisibility(/*View.SYSTEM_UI_FLAG_LOW_PROFILE*/
                View.SYSTEM_UI_FLAG_FULLSCREEN
                /*| View.SYSTEM_UI_FLAG_LAYOUT_STABLE*/
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                /*| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION*/
                /*| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION*/);

        mVisible=false;
    }

    @SuppressLint("InlinedApi")
    private void show() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }

        View decorView = getWindow().getDecorView();
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.coordinatorLayout);

        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //rootView.setFitsSystemWindows(true);
        //decorView.setSystemUiVisibility(0);

        //m_camera_view.setSystemUiVisibility(0);

        mVisible=true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.v(TAG, "onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
        mCameraView.activityOnConfigurationChanged();
        }

}
