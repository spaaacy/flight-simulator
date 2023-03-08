package org.flightcontrol.sensor.altitude;

import static org.flightcontrol.sensor.altitude.Altitude.*;

public class AltitudeLandingState implements AltitudeState  {

    Altitude altitude;

    public AltitudeLandingState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {
        if (altitude.currentAltitude > 0) {

            int smallestPossibleNextValue = altitude.currentAltitude - INCREMENT_TAKEOFF_LANDING - MAX_FLUCTUATION_TAKEOFF_LANDING;

            if (smallestPossibleNextValue >= 0) {
                int fluctuation = (int)(Math.random() * MAX_FLUCTUATION_TAKEOFF_LANDING * 2) - MAX_FLUCTUATION_TAKEOFF_LANDING;
                Integer newAltitude = altitude.currentAltitude - INCREMENT_TAKEOFF_LANDING + fluctuation;
                altitude.setCurrentAltitude(newAltitude);
            } else {
                altitude.setCurrentAltitude(0);
            }

        } else {
            altitude.timer.cancel();
        }
    }



}

