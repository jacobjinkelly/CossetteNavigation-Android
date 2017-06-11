package com.example.cossettenavigation.pathfinding;

import com.example.cossettenavigation.Utilities;
import com.example.cossettenavigation.map.Zone;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A path going through a set of beacons.
 */
public class Path implements Serializable {

    private double travelTime;
    private ArrayList<Step> steps;
    //private ArrayList<Integer> timeRemaining;
    private Zone destination = null;


    public Path(double travelTime, ArrayList<Step> steps) {
        this.travelTime = travelTime;
        this.steps = steps;

        /*timeRemaining=new ArrayList<>(steps.size());
        //construct prefix sum array
        timeRemaining.set(0,(int)steps.get(0).getTravelTime());
        for (int i=1;i<steps.size();i++){
            timeRemaining.set(i,timeRemaining.get(i-1)+(int) steps.get(i).getTravelTime());
        }*/
    }

    @Override
    public String toString() {
        return String.format(
                "%s { travelTime = %.1f s, steps = %s, destination = \"%s\" }",
                getClass().getSimpleName(),
                travelTime,
                Utilities.getStepsString(steps),
                (destination != null) ? destination.getName() : null);
    }

    public double getTravelTime() {
        return travelTime;
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }

    /*public int getTimeRemaining(int i){
        return timeRemaining.get(i);
    }*/

    public Zone getDestination() {
        return destination;
    }

    public void setDestination(Zone destination) {
        this.destination = destination;
    }


    public ArrayList<NavigationStep> toNavigationSteps() {
        ArrayList<NavigationStep> navigationSteps = new ArrayList<>();

        // No steps
        if (steps.size() == 0) {
            navigationSteps.add(new NavigationStep(
                    "You are already there!",
                    "",
                    null,
                    null,
                    0
            ));
        }

        // There are steps
        else {
            for (int i = 0; i < steps.size(); i++) {
                Step step = steps.get(i);

                // Start at beacon
                if (i == 0) {
                    navigationSteps.add(new NavigationStep(
                            "Start at " + step.getStartBeacon().getDescription(),
                            "",
                            null,
                            step.getStartBeacon(),
                            5
                    ));
                }

                // Turn at beacon
                else {
                    navigationSteps.add(new NavigationStep(
                            step.getTurnDescription(),
                            "",
                            step.getTurnAngle(),
                            step.getStartBeacon(),
                            5   // Wait at least 5 seconds after turning
                    ));
                }

                // Travel
                navigationSteps.add(new NavigationStep(
                        step.getTravelDescription(),
                        step.getEndBeacon().getDescription(),
                        0.0,
                        step.getEndBeacon(),
                        step.getTravelTime()
                ));

                // Add this step's travel time to current and previous steps
                for (NavigationStep navigationStep : navigationSteps) {
                    navigationStep.addTimeRemaining(step.getTravelTime());
                }
            }

            // Arrival
            navigationSteps.add(new NavigationStep(
                    "You have arrived!",
                    (destination != null) ? destination.getName() : "",
                    null,
                    null,
                    0
            ));
        }

        return navigationSteps;
    }

}
