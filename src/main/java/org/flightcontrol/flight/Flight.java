package org.flightcontrol.flight;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.LinkedList;
import java.util.concurrent.Phaser;

public class Flight implements Runnable {

    Phaser phaser;
    public Altitude altitude;
    LinkedList<Observer> observers;

    @Override
    public void run() {
        phaser = new Phaser(1);
        altitude = new Altitude(phaser);
    }

    public void nextPhase() {
        phaser.arriveAndAwaitAdvance();
        for (Observer observer : observers) {
            observer.update(phaser.getPhase());
        }
    }

}
