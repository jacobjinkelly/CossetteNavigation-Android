package com.example.cossettenavigation.map;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A beacon placed in a supporting location; can be used to improve location estimates.
 * @see Map
 */
public class SupportBeacon extends Beacon implements Serializable {

    private static final String TAG = "SupportBeacon";

    /**
     * A SupportBeacon can only be part of one zone.
     */
    private Zone zone = null;

    /**
     * Constructor using an absolute position.
     */
    public SupportBeacon(String name,
                         String description,
                         Floor floor,
                         double xPosition,
                         double yPosition,
                         String uuid,
                         int major,
                         int minor) {

        super(name, description, floor, xPosition, yPosition, uuid, major, minor);
        floor.addSupportBeacon(this);
    }

    /**
     * Constructor using a position relative to another beacon.
     */
    public SupportBeacon(String name,
                         String description,
                         Floor floor,
                         Beacon referenceBeacon,
                         double xPositionOffset,
                         double yPositionOffset,
                         String uuid,
                         int major,
                         int minor) {

        super(name, description, floor, referenceBeacon, xPositionOffset, yPositionOffset, uuid, major, minor);
        floor.addSupportBeacon(this);
    }


    @Override
    public String toString() {
        return String.format(
                "%s { name = \"%s\", floor = \"%s\", position = %s, uuid = %s, major = %d, minor = %d, zone = \"%s\" }",
                getClass().getSimpleName(),
                name, floor.getName(), position, uuid, major, minor,
                (zone != null) ? zone.getName() : "null");
    }


    public Zone getZone() {
        return zone;
    }

    @Override
    public ArrayList<Zone> getZones() {
        ArrayList<Zone> zones = new ArrayList<>();
        zones.add(zone);
        return zones;
    }

    public void setZone(Zone zone) {
        if (this.zone != null) {
            Log.e(TAG, "Overriding zone for support beacon: " + this);
        }
        this.zone = zone;
    }

}
