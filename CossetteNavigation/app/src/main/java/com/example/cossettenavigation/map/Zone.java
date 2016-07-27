package com.example.cossettenavigation.map;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Bruno on 2016-07-22.
 */
public class Zone {

    private String name;

    // A Zone does not own its anchor beacons, since they can define multiple zones
    private ArrayList<WeakReference<AnchorBeacon>> anchorBeacons = new ArrayList<>();

    // A zone owns its support beacons, since they are only part of that zone
    private ArrayList<SupportBeacon> supportBeacons = new ArrayList<>();


    public Zone(String name) {
        this.name = name;
    }

    public void addAnchorBeacon(AnchorBeacon anchorBeacon) {
        anchorBeacons.add(new WeakReference<AnchorBeacon>(anchorBeacon));
        anchorBeacon.addZone(this);
    }

    public void addAnchorBeacons(AnchorBeacon... anchorBeacons) {
        for (AnchorBeacon anchorBeacon : anchorBeacons) {
            addAnchorBeacon(anchorBeacon);
        }
    }

    public void addSupportBeacon(SupportBeacon supportBeacon) {
        supportBeacons.add(supportBeacon);
        supportBeacon.setZone(this);
    }

    public void addSupportBeacons(SupportBeacon... supportBeacons) {
        for (SupportBeacon supportBeacon : supportBeacons) {
            addSupportBeacon(supportBeacon);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s { name = %s, anchorBeacons = %s, supportBeacons = %s }",
                getClass().getSimpleName(), name, anchorBeacons, supportBeacons);
    }

}