package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.*;

public class LandingState implements AltitudeState {

    Altitude altitude;
    Timer timer = new Timer();

    public LandingState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {
        TimerTask landingTask = new TimerTask() {
            @Override
            public void run() {
                if (altitude.currentAltitude > 0) {

                    if (!(altitude.currentAltitude <= 600)) {
                        Integer fluctuation = (int)(Math.random() * 200) - 100;
                        altitude.currentAltitude +=  fluctuation - TAKEOFF_LANDING_INCREMENT;
                        System.out.println("Altitude: " + altitude.currentAltitude);

                    } else {
                        altitude.currentAltitude = 0;
                        System.out.println("Altitude: " + altitude.currentAltitude);
                    }

                } else {
                    altitude.phaser.arriveAndDeregister();
                    timer.cancel();
                }
            }
        };


        timer.scheduleAtFixedRate(landingTask, 0L, TICK_RATE);

    }

    @Override
    public void stopExecuting() {
        timer.cancel();
    }



}

