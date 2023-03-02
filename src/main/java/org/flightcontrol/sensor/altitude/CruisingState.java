package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

import static org.flightcontrol.sensor.altitude.Altitude.CRUISING_ALTITUDE;
import static org.flightcontrol.sensor.altitude.Altitude.UPDATE_RATE;

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
                Integer fluctuation = (int)(Math.random() * 2000) - 1000;
                altitude.currentAltitude = CRUISING_ALTITUDE + fluctuation;
                System.out.println("Altitude: " + altitude.currentAltitude);
            }
        };


        altitude.phaser.arriveAndAwaitAdvance(); // First arrive to go into phase 2: Cruising
        System.out.println("Altitude: Phase " + altitude.phaser.getPhase());
        timer.scheduleAtFixedRate(cruisingTask, 0L, UPDATE_RATE);
        altitude.phaser.arrive(); // Second arrive to give thumb-up for phase 3: Landing


    }

    @Override
    public void stopExecuting() {
        timer.cancel();
    }

}
