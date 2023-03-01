package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.sensor.altitude.Altitude.*;

public class LandingState implements AltitudeState {

    Altitude altitude;
    Timer timer = new Timer();

    public LandingState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {

        System.out.println("Altitude: Landing");
        TimerTask landingTask = new TimerTask() {
            @Override
            public void run() {
                if (altitude.currentAltitude > 0) {
                    Integer fluctuation = (int)(Math.random() * 200) - 100;
                    altitude.currentAltitude += TAKEOFF_LANDING_INCREMENT - fluctuation;
                    System.out.println("Altitude: " + altitude.currentAltitude);
                } else {
                    timer.cancel();
                }
            }
        };


        timer.scheduleAtFixedRate(landingTask, 0L, UPDATE_RATE);

    }


}

