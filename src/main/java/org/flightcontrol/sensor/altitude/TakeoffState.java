package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.*;

public class TakeoffState implements AltitudeState {

    Altitude altitude;

    public TakeoffState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {

        int maxPossibleNextValue = altitude.currentAltitude + INCREMENT_TAKEOFF_LANDING + MAX_FLUCTUATION_TAKEOFF_LANDING;

        if (maxPossibleNextValue <= CRUISING_ALTITUDE) {

            Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_TAKEOFF_LANDING * 2) - MAX_FLUCTUATION_TAKEOFF_LANDING;
            Integer newAltitude = altitude.currentAltitude + INCREMENT_TAKEOFF_LANDING + fluctuation;
            altitude.setCurrentAltitude(newAltitude);

        } else {

            altitude.sendNewState(CRUISING_FLAG);

        }
    }
}
