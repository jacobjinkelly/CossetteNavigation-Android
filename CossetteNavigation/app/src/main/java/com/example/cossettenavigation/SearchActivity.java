package com.example.cossettenavigation;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Region;
import com.example.cossettenavigation.beacons.ApplicationBeaconManager;
import com.example.cossettenavigation.beacons.BeaconTrackingData;
import com.example.cossettenavigation.map.AnchorBeacon;
import com.example.cossettenavigation.map.Beacon;
import com.example.cossettenavigation.map.DatabaseHelper;
import com.example.cossettenavigation.map.Map;
import com.example.cossettenavigation.map.Zone;
import com.example.cossettenavigation.pathfinding.Path;
import com.example.cossettenavigation.pathfinding.Pathfinder;

import java.util.ArrayList;

/**
 * Allows the user to search for and select a destination to navigate to.
 */
public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    /**
     * The range to the nearest beacon that the device must be within before starting navigation.
     */
    private static final double START_BEACON_RANGE = Double.POSITIVE_INFINITY;

    private DatabaseHelper dbHelper;
    public static SQLiteDatabase db;

    private ApplicationBeaconManager beaconManager;

    private SearchView searchView;

    private ListView searchSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        String query;
        Intent searchIntent = getIntent();

        if(Intent.ACTION_SEARCH.equals(searchIntent.getAction()))
        {
            query = searchIntent.getStringExtra(SearchManager.QUERY);
            //create listview of returned results
            //act as if top choice clicked
            //pull down keyboard to view suggestions
            //do nothing if no results
            Toast.makeText(SearchActivity.this, query, Toast.LENGTH_SHORT).show();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Make the volume buttons control the text to speech volume (music stream)
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        /*String[] location = getResources().getStringArray(R.array.locations);
        ArrayList<String> searchResults = new ArrayList<>();
        for (int i=0; i<location.length; i++)
            if (location[i].toLowerCase().contains(query.toLowerCase()))
                searchResults.add(location[i]);*/


        beaconManager = (ApplicationBeaconManager) getApplication();

        searchSuggestions = (ListView) findViewById(R.id.search_suggestions);
        updateSearchSuggestions("");

        // When a search suggestion is selected
        searchSuggestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Zone zone = (Zone) parent.getItemAtPosition(position);

//                Floor floor=beaconManager.getFloor();
//                ArrayList<BeaconTrackingData> beacons=beaconManager.getNearestBeacons();
//
//                Double minDistance=Double.POSITIVE_INFINITY;
//                BeaconTrackingData nearestTrackedBeacon=null;
//
//                for (BeaconTrackingData beaconData:beacons){
//                    if (beaconData.getBeacon().getFloor()==floor&&minDistance>beaconData.getEstimatedAccuracy()){
//                        minDistance=beaconData.getEstimatedAccuracy();
//                        nearestTrackedBeacon=beaconData;
//                    }
//                }

                Pair<Region, BeaconTrackingData> nearestTrackedBeacon = beaconManager.getNearestTrackedBeacon();

                // Check for the nearest beacon
                if (nearestTrackedBeacon != null) {

                    // If the nearest beacon is in sufficient range
                    if (nearestTrackedBeacon.second.getEstimatedAccuracy() <= START_BEACON_RANGE) {
                        Beacon startBeacon = nearestTrackedBeacon.second.getBeacon();

                        // Get the shortest path to the destination
                        double minTravelTime = Double.POSITIVE_INFINITY;
                        Path minPath = null;

                        for (AnchorBeacon testEndBeacon : zone.getAnchorBeacons()) {
                            Path testPath = Pathfinder.getShortestPath(startBeacon, testEndBeacon);

                            if (testPath != null && testPath.getTravelTime() < minTravelTime) {
                                minTravelTime = testPath.getTravelTime();
                                minPath = testPath;
                            }
                        }

                        if (minPath != null) {
                            minPath.setDestination(zone);
                            startMainActivityNavigation(minPath);
                        } else {
                            Toast.makeText(SearchActivity.this, "Path not found", Toast.LENGTH_LONG).show();
                        }
                    }

                    else {
                        Toast.makeText(SearchActivity.this, "Please move closer to the nearest beacon", Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Toast.makeText(SearchActivity.this, "No beacons found", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateSearchSuggestions(String searchText) {
        ArrayList<Zone> filteredZones = new ArrayList<>();
        searchText = searchText.toLowerCase();

        for (Zone zone : Map.zones) {
            String zoneName = zone.getName().toLowerCase();
            String zoneType = Utilities.getZoneFloorNamesString(zone).toLowerCase();

            if ((zoneName.contains(searchText) || zoneType.contains(searchText)) && zone.getIsDestination()) {
                filteredZones.add(zone);
            }
        }

        searchSuggestions.setAdapter(new ZoneArrayAdapter(SearchActivity.this, filteredZones));
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateSearchSuggestions(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void startMainActivityNavigation(Path path) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.INTENT_KEY_PATH, path);
        startActivity(intent);
    }


    /**
     * Adapter for the search suggestion list.
     */
    private class ZoneArrayAdapter extends ArrayAdapter<Zone> {

        public ZoneArrayAdapter(Context context, ArrayList<Zone> zones) {
            super(context, 0, zones);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Zone zone = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.two_line_list_item, parent, false);
            }

            // Lookup view for data
            TextView text1 = (TextView) convertView.findViewById(R.id.text1);
            TextView text2 = (TextView) convertView.findViewById(R.id.text2);

            // Populate the data into the template view using the data object
            text1.setText(zone.getName());
            text2.setText(Utilities.getZoneFloorNamesString(zone));

            // Return the completed view to render on screen
            return convertView;
        }

    }

}
