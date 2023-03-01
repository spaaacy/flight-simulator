package org.flightcontrol.sensor.altitude;

import java.util.concurrent.Phaser;

public class Altitude implements Runnable {

    static final Long UPDATE_RATE = 5000L;
    static final Integer CRUISING_ALTITUDE = 11000;
    static final Integer TAKEOFF_LANDING_INCREMENT = 500;


    Phaser phaser;
    Integer currentAltitude = 0;
    AltitudeState altitudeState;

    public Altitude(Phaser phaser) {
        this.phaser = phaser;
        phaser.register();
    }

    @Override
    public void run() {

        phaser.arriveAndAwaitAdvance();
        System.out.println("Phaser: " + phaser.getPhase());
        System.out.println("Altitude: Taking off");
        changeState(new TakeoffState(this));

    }

    public void changeState(AltitudeState newState) {
        this.altitudeState = newState;
        phaser.arrive();
        altitudeState.generateAltitude();
    }


}
