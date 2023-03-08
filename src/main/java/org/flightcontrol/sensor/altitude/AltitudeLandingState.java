package org.flightcontrol.sensor.altitude;

import static org.flightcontrol.sensor.altitude.Altitude.INCREMENT_TAKEOFF_LANDING;
import static org.flightcontrol.sensor.altitude.Altitude.MAX_FLUCTUATION_TAKEOFF_LANDING;

public class AltitudeLandingState implements AltitudeState  {

    Altitude altitude;

    public AltitudeLandingState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {
        if (altitude.currentAltitude > 0) {

            int smallestNextPossibleValue = altitude.currentAltitude - INCREMENT_TAKEOFF_LANDING - MAX_FLUCTUATION_TAKEOFF_LANDING;

            if (smallestNextPossibleValue >= 0) {
                int fluctuation = (int)(Math.random() * MAX_FLUCTUATION_TAKEOFF_LANDING * 2) - MAX_FLUCTUATION_TAKEOFF_LANDING;
                Integer newAltitude = altitude.currentAltitude - INCREMENT_TAKEOFF_LANDING + fluctuation;
                altitude.setCurrentAltitude(newAltitude);
            } else {
                altitude.setCurrentAltitude(0);
            }

        } else {
            altitude.sendLandedFlagToEngine();
            altitude.timer.cancel();
        }
    }



}

