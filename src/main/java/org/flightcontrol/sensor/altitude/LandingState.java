package org.flightcontrol.sensor.altitude;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.*;

public class LandingState implements AltitudeState  {

    Altitude altitude;

    public LandingState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {
        if (altitude.currentAltitude > 0) {

            int minPossibleNextValue = altitude.currentAltitude - INCREMENT_TAKEOFF_LANDING - MAX_FLUCTUATION_TAKEOFF_LANDING;
            int largestPossibleDecrement = INCREMENT_TAKEOFF_LANDING + MAX_FLUCTUATION_TAKEOFF_LANDING;

            if (minPossibleNextValue > largestPossibleDecrement) {
                int fluctuation = (int)(Math.random() * MAX_FLUCTUATION_TAKEOFF_LANDING * 2) - MAX_FLUCTUATION_TAKEOFF_LANDING;
                Integer newAltitude = altitude.currentAltitude - INCREMENT_TAKEOFF_LANDING + fluctuation;
                altitude.setCurrentAltitude(newAltitude);
            } else {
                altitude.setCurrentAltitude(0);
            }

            System.out.println("Altitude: " + altitude.currentAltitude);
        } else {
            altitude.timer.cancel();
        }
    }



}

