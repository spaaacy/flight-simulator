package org.flightcontrol.sensor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

public class Altitude implements Runnable {

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

        changeState(new TakeoffState(currentAltitude, phaser));

        System.out.println("\tCRUISING ALTITUDE");


    }

    public void changeState(AltitudeState newState) {
        this.altitudeState = newState;
        altitudeState.generateAltitude();
    }


}
