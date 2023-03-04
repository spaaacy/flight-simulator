package org.flightcontrol.sensor.altitude;

import org.flightcontrol.Observer;

import java.util.LinkedList;
import java.util.Timer;
import java.util.concurrent.Phaser;

import static org.flightcontrol.flight.Flight.FLIGHT_IDENTIFIER;

public class Altitude implements Runnable, Observer {

    public static final String ALTITUDE_ID = "Altitude";
    public static final String CRUISING_FLAG = "Cruising";
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
        if (altitudeState.getClass().equals(CruisingState.class)){
            for (Observer observer : observers) {
                observer.update(CRUISING_FLAG);
            }
        }

        altitudeState.generateAltitude();
    }

    @Override
    public void update(String... updatedValue) {
        if (updatedValue.length != 0 &&
                updatedValue[0].equals(FLIGHT_IDENTIFIER) &&
                updatedValue[1].equals("3")) {
            changeState(new LandingState(this));
        }
    }


    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void setCurrentAltitude(Integer currentAltitude) {
        this.currentAltitude = currentAltitude;
        for (Observer observer : observers) {
            observer.update(ALTITUDE_ID, currentAltitude.toString());
        }
    }
}
