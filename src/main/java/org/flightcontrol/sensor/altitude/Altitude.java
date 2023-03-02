package org.flightcontrol.sensor.altitude;

import org.flightcontrol.flight.Observer;

import java.util.LinkedList;
import java.util.concurrent.Phaser;

public class Altitude implements Runnable, Observer {

    static final Long UPDATE_RATE = 100L;
    static final Integer CRUISING_ALTITUDE = 11000;
    static final Integer TAKEOFF_LANDING_INCREMENT = 500;


    Phaser phaser;
    Integer currentAltitude = 0;
    public AltitudeState altitudeState;
    LinkedList<Observer> observers = new LinkedList<>();;

    public Altitude(Phaser phaser) {
        this.phaser = phaser;
        phaser.register();
    }

    @Override
    public void run() {
        phaser.arriveAndAwaitAdvance();
        changeState(new TakeoffState(this));
    }

    public void changeState(AltitudeState newState) {


        if (altitudeState != null){
            altitudeState.stopExecuting();
        }

        this.altitudeState = newState;

        for (Observer observer : observers) {
            observer.update();
        }

        altitudeState.generateAltitude();
    }

    @Override
    public void update() {
        if (phaser.getPhase() == 3) {

            System.out.println("Altitude: Changing to landing state");
            changeState(new LandingState(this));
        }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

}
