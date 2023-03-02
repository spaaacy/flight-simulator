package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;

public class CruisingState extends TimerTask implements AltitudeState {

    Altitude altitude;
    Timer timer = new Timer();;

    public CruisingState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void run() {
        System.out.println("Altitude: " + altitude.currentAltitude);
    }

    @Override
    public void generateAltitude() {
        altitude.phaser.arriveAndAwaitAdvance(); // First arrive to go into phase 2: Cruising
        altitude.phaser.arrive(); // Second arrive to give thumbs-up for phase 3: Landing
        timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
    }

    @Override
    public void stopExecuting() {
        timer.cancel();
    }

}
