package com.example.cossettenavigation.map;

import com.example.cossettenavigation.Utilities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A key area within a floor or building; may be a destination that the user can navigate to.
 * Should only contain beacons such that the user can walk between any two beacons in a straight line.
 * @see Map
 */
public class Zone implements Serializable {

    public enum ZoneType {
        HALLWAY("Hallway"),
        ROOM("Room"),
        STAIRS("Stairs"),
        ELEVATOR("Elevator"),
        ENTRANCE("Entrance");

        public final String lowercaseDescription;

        ZoneType(String lowercaseDescription) {
            this.lowercaseDescription = lowercaseDescription;
        }
    }

    private String name;
    private ZoneType zoneType;

    /**
     * Whether the zone should be a destination that the user can navigate to.
     */
    private boolean isDestination;

    private ArrayList<Floor> floors = new ArrayList<>();
    private ArrayList<AnchorBeacon> anchorBeacons = new ArrayList<>();
    private ArrayList<SupportBeacon> supportBeacons = new ArrayList<>();




    public Zone(String name, ZoneType zoneType, boolean isDestination) {
        this.name = name;
        this.zoneType = zoneType;
        this.isDestination = isDestination;
    }

    @Override
    public String toString() {
        return String.format(
                "%s { name = \"%s\", zoneType = %s, isDestination = %b, anchorBeacons = %s, supportBeacons = %s, floors = %s }",
                getClass().getSimpleName(),
                name,
                zoneType.name(),
                isDestination,
                Utilities.getAnchorBeaconNamesString(anchorBeacons),
                Utilities.getSupportBeaconNamesString(supportBeacons),
                Utilities.getFloorNamesString(floors));
    }

    public String getName() {
        return name;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public boolean getIsDestination() {
        return isDestination;
    }

    public ArrayList<Floor> getFloors() {
        return floors;
    }

    public ArrayList<AnchorBeacon> getAnchorBeacons() {
        return anchorBeacons;
    }

    public ArrayList<SupportBeacon> getSupportBeacons() {
        return supportBeacons;
    }


    /**
     * Also updates the anchor beacons and associated floors to refer to this zone.
     */
    public void addAnchorBeacons(AnchorBeacon... anchorBeacons) {
        for (AnchorBeacon anchorBeacon : anchorBeacons) {
            if (!this.anchorBeacons.contains(anchorBeacon)) {
                this.anchorBeacons.add(anchorBeacon);
            }
            anchorBeacon.addZone(this);

            if (!this.floors.contains(anchorBeacon.getFloor())) {
                this.floors.add(anchorBeacon.getFloor());
            }
            anchorBeacon.getFloor().addZone(this);
        }
    }

    /**
     * Also updates the support beacons and associated floors to refer to this zone.
     */
    public void addSupportBeacons(SupportBeacon... supportBeacons) {
        for (SupportBeacon supportBeacon : supportBeacons) {
            if (!this.supportBeacons.contains(supportBeacon)) {
                this.supportBeacons.add(supportBeacon);
            }
            supportBeacon.setZone(this);

            if (!this.floors.contains(supportBeacon.getFloor())) {
                this.floors.add(supportBeacon.getFloor());
            }
            supportBeacon.getFloor().addZone(this);
        }
    }

}
