package org.flightcontrol.sensor.altitude;

import org.flightcontrol.flight.Observer;

import java.util.concurrent.Phaser;

public class Altitude implements Runnable, Observer {

    static final Long UPDATE_RATE = 1000L;
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
        System.out.println("Phaser: " + phaser.getPhase());
        altitudeState.generateAltitude();
    }

    @Override
    public void update(int phaseValue) {
        if (phaseValue == 3) {
            changeState(new LandingState(this));
        }
    }

}
