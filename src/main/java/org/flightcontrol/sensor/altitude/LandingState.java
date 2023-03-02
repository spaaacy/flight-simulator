package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.*;

public class LandingState extends TimerTask implements AltitudeState  {

    Altitude altitude;
    Timer timer = new Timer();

    public LandingState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void run() {
        if (altitude.currentAltitude > 0) {

            int minPossibleNextValue = altitude.currentAltitude + INCREMENT_TAKEOFF_LANDING + MAX_FLUCTUATION_TAKEOFF_LANDING;
            int largestPossibleDecrement = INCREMENT_TAKEOFF_LANDING + MAX_FLUCTUATION_TAKEOFF_LANDING;
            if (minPossibleNextValue > largestPossibleDecrement) {
                Integer fluctuation = (int)(Math.random() * MAX_FLUCTUATION_TAKEOFF_LANDING * 2) - MAX_FLUCTUATION_TAKEOFF_LANDING;
                altitude.currentAltitude -= INCREMENT_TAKEOFF_LANDING + fluctuation;
            } else {
                altitude.currentAltitude = 0;
            }

            System.out.println("Altitude: " + altitude.currentAltitude);
        } else {
            altitude.phaser.arriveAndDeregister();
            timer.cancel();
        }
    }

    @Override
    public void generateAltitude() {
        timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
    }

    @Override
    public void stopExecuting() {
        timer.cancel();
    }



}

