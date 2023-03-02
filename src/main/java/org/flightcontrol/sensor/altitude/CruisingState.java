package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.CRUISING_ALTITUDE;

public class CruisingState implements AltitudeState {


    Altitude altitude;
    Timer timer = new Timer();;


    public CruisingState(Altitude altitude) {
        this.altitude = altitude;
    }


    @Override
    public void generateAltitude() {

        TimerTask cruisingTask = new TimerTask() {
            @Override
            public void run() {
//                Integer fluctuation = (int)(Math.random() * 2000) - 1000;
//                altitude.currentAltitude = CRUISING_ALTITUDE + fluctuation;
                System.out.println("Altitude: " + altitude.currentAltitude);
            }
        };


        altitude.phaser.arriveAndAwaitAdvance(); // First arrive to go into phase 2: Cruising
        timer.scheduleAtFixedRate(cruisingTask, 0L, TICK_RATE);
        altitude.phaser.arrive(); // Second arrive to give thumb-up for phase 3: Landing


    }

    @Override
    public void stopExecuting() {
        timer.cancel();
    }

}
