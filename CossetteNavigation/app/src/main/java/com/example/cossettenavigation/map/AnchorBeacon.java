package com.example.cossettenavigation.map;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A beacon placed in a key location and used to define zones.
 * @see Map
 */
public class AnchorBeacon extends Beacon {

    /**
     * References to zones this beacon is a part of.
     */
    private ArrayList<WeakReference<Zone>> zones = new ArrayList<>();




    /**
     * Constructor using an absolute position.
     */
    public AnchorBeacon(String name,
                        double xPosition,
                        double yPosition,
                        String uuid,
                        int major,
                        int minor) {

        super(name, xPosition, yPosition, uuid, major, minor);
    }

    /**
     * Constructor using a position relative to another beacon.
     */
    public AnchorBeacon(String name,
                        Beacon referenceBeacon,
                        double xPositionOffset,
                        double yPositionOffset,
                        String uuid,
                        int major,
                        int minor) {

        super(name, referenceBeacon, xPositionOffset, yPositionOffset, uuid, major, minor);
    }

    @Override
    public String toString() {
        return String.format(
                "%s { name = %s, position = %s, uuid = %s, major = %d, minor = %d, zones = %s }",
                getClass().getSimpleName(), name, position, uuid, major, minor, zones);
    }

    public void addZone(Zone zone) {
        zones.add(new WeakReference<>(zone));
    }

}
