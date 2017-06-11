package com.example.cossettenavigation.pathfinding;

import com.example.cossettenavigation.map.Beacon;
import com.example.cossettenavigation.map.Map;
import com.example.cossettenavigation.map.Zone;

import java.io.Serializable;

/**
 * The navigation path data between two beacons in the same {@link Zone}.
 */
public class Step implements Serializable {

    private Beacon startBeacon;
    private Beacon endBeacon;

    private Zone zone;
    private double travelTime;

    // In degrees (clockwise from up)
    private Double travelAngle;
    private double turnAngle;




    public Step(Beacon startBeacon, Beacon endBeacon, Zone zone, Double travelAngle, double turnAngle) {
        this.startBeacon = startBeacon;
        this.endBeacon = endBeacon;
        this.zone = zone;
        this.travelTime = Map.estimateTravelTime(startBeacon, endBeacon, zone);
        this.travelAngle = travelAngle;
        this.turnAngle = turnAngle;

        if (this.travelAngle != null) {
            while (this.travelAngle < 0) {
                this.travelAngle += 360;
            }
        }
        while (this.turnAngle < 0) {
            this.turnAngle += 360;
        }
    }


    @Override
    public String toString() {
        return String.format(
                "%s { startBeacon = \"%s\", endBeacon = \"%s\", zone = \"%s\", travelTime = %.1f s, travelAngle = %s deg, turnAngle = %.0f deg, getTurnDescription() = \"%s\", getTravelDescription() = \"%s\"",
                getClass().getSimpleName(),
                startBeacon.getName(),
                endBeacon.getName(),
                zone.getName(),
                travelTime,
                (travelAngle != null) ? String.format("%.0f", travelAngle) : "null",
                turnAngle,
                getTurnDescription(),
                getTravelDescription());
    }


    public Beacon getStartBeacon() {
        return startBeacon;
    }

    public Beacon getEndBeacon() {
        return endBeacon;
    }

    public Zone getZone() {
        return zone;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public Double getTravelAngle() {
        return travelAngle;
    }

    public double getTurnAngle() {
        return turnAngle;
    }

    private String getTurnAngleDescription() {
        if (turnAngle == 0) {
            return "forward";
        } else if (0 < turnAngle && turnAngle <= 45) {
            return "slightly right";
        } else if (45 < turnAngle && turnAngle <= 135) {
            return "right";
        } else if (135 < turnAngle && turnAngle <= 225) {
            return "backward";
        } else if (225 < turnAngle && turnAngle <= 315) {
            return "left";
        } else if (315 < turnAngle && turnAngle < 360) {
            return "slightly left";
        } else {
            return "";
        }
    }

    public String getTurnDescription() {
        if (turnAngle == 0) {
            return "Walk forward";
        } else {
            return "Turn " + getTurnAngleDescription();
        }
    }

    public String getTravelDescription() {
        switch (zone.getZoneType()) {
            case HALLWAY:
            case ROOM:
                return String.format("Walk %.0f metres ahead", Map.distanceBetweenBeacons(startBeacon, endBeacon));
            case STAIRS:
            case ELEVATOR:
                return "Take the " + zone.getZoneType().lowercaseDescription /*+ " to " + endBeacon.getFloor().getName()*/;
            default:
                return "";
        }
    }

    public String getDestinationDescription(){
        return "To " + endBeacon.getDescription();
    }

}
