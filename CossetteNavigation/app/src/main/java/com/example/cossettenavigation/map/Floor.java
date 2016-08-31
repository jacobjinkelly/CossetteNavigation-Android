package com.example.cossettenavigation.map;

import com.example.cossettenavigation.Utilities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A floor of the building.
 * @see Map
 */
public class Floor implements Serializable {

    private String name;

    private double zPosition;

    private ArrayList<AnchorBeacon> anchorBeacons = new ArrayList<>();
    private ArrayList<SupportBeacon> supportBeacons = new ArrayList<>();

    private ArrayList<Zone> zones = new ArrayList<>();


    /**
     * Constructor using an absolute position.
     */
    public Floor(String name, double zPosition) {
        this.name = name;
        this.zPosition = zPosition;
    }

    /**
     * Constructor using a position relative to another floor.
     */
    public Floor(String name, Floor referenceFloor, double zPositionOffset) {
        this(name, referenceFloor.getZPosition() + zPositionOffset);
    }


    @Override
    public String toString() {
        return String.format(
                "%s { name = %s, zPosition = %.1f, anchorBeacons = %s, supportBeacons = %s, zones = %s }",
                getClass().getSimpleName(),
                name,
                zPosition,
                Utilities.getAnchorBeaconNamesString(anchorBeacons),
                Utilities.getSupportBeaconNamesString(supportBeacons),
                Utilities.getZoneNamesString(zones));
    }


    public String getName() {
        return name;
    }

    public double getZPosition() {
        return zPosition;
    }

    public ArrayList<AnchorBeacon> getAnchorBeacons() {
        return anchorBeacons;
    }

    public ArrayList<SupportBeacon> getSupportBeacons() {
        return supportBeacons;
    }

    public ArrayList<Beacon> getAllBeacons() {
        ArrayList<Beacon> beacons = new ArrayList<>();
        beacons.addAll(anchorBeacons);
        beacons.addAll(supportBeacons);
        return beacons;
    }

    public ArrayList<Zone> getZones() {
        return zones;
    }


    public void addAnchorBeacon(AnchorBeacon anchorBeacon) {
        if (!this.anchorBeacons.contains(anchorBeacon)) {
            this.anchorBeacons.add(anchorBeacon);
        }
    }

    public void addSupportBeacon(SupportBeacon supportBeacon) {
        if (!this.supportBeacons.contains(supportBeacon)) {
            this.supportBeacons.add(supportBeacon);
        }
    }

    public void addZone(Zone zone) {
        if (!this.zones.contains(zone)) {
            this.zones.add(zone);
        }
    }

}
