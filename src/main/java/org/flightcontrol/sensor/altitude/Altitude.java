package org.flightcontrol.sensor.altitude;

import org.flightcontrol.Observer;

import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.Phaser;

public class Altitude implements Runnable, Observer {

    static final Integer INCREMENT_TAKEOFF_LANDING = 500;
    static final Integer MAX_FLUCTUATION_TAKEOFF_LANDING = 100;
    public static final String ALTITUDE_EXCHANGE_NAME = "AltitudeExchange";
    public static final String ALTITUDE_EXCHANGE_KEY = "AltitudeKey";

    Phaser phaser;
    Integer currentAltitude = 0;
    AltitudeState altitudeState;
    LinkedList<Observer> observers = new LinkedList<>();;
    Timer timer = new Timer();

    public Altitude(Phaser phaser) {
        this.phaser = phaser;
        phaser.register();
    }

    @Override
    public void run() {
        phaser.arriveAndAwaitAdvance();
        changeState(new TakeoffState(this));
    }

    public void changeState(AltitudeState newAltitudeState) {
        if (altitudeState != null) {
            altitudeState.stopExecuting();
        }

        this.altitudeState = newAltitudeState;
        for (Observer observer : observers) {
            observer.update();
        }

        altitudeState.generateAltitude();
    }

    @Override
    public void update() {
        if (phaser.getPhase() == 3) {
            changeState(new LandingState(this));
        }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public AltitudeState getAltitudeState() {
        return altitudeState;
    }
}
