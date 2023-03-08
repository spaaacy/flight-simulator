package org.flightcontrol.sensor.altitude;

import static org.flightcontrol.sensor.altitude.Altitude.*;

public class AltitudeTakeoffState implements AltitudeState {

    Altitude altitude;

    public AltitudeTakeoffState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {

        if (altitude.isEngineReady) {
            int maxPossibleNextValue = altitude.currentAltitude + INCREMENT_TAKEOFF_LANDING + MAX_FLUCTUATION_TAKEOFF_LANDING;

            if (maxPossibleNextValue <= CRUISING_ALTITUDE) {

                Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_TAKEOFF_LANDING * 2) - MAX_FLUCTUATION_TAKEOFF_LANDING;
                Integer newAltitude = altitude.currentAltitude + INCREMENT_TAKEOFF_LANDING + fluctuation;
                altitude.setCurrentAltitude(newAltitude);

            } else {
                altitude.sendCruisingFlagToFlight();
            }
        }
    }
}
