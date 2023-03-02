package org.flightcontrol.flight;

import org.flightcontrol.sensor.altitude.Altitude;
import org.flightcontrol.sensor.altitude.AltitudeState;
import org.flightcontrol.sensor.altitude.CruisingState;

import java.util.LinkedList;
import java.util.concurrent.Phaser;

public class Flight implements Runnable, Observer {

    Phaser phaser = new Phaser(1);
    public Altitude altitude = new Altitude(phaser);
    LinkedList<Observer> observers = new LinkedList<>();

    public Flight() {
        addObserver(altitude);
        altitude.addObserver(this);
    }

    @Override
    public void run() {
        nextPhase();
        System.out.println("Flight: Taking off");
    }

    private void nextPhase() {
        phaser.arrive();
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public void initiateLanding() {
        if (phaser.getPhase() == 2){
            nextPhase();
            System.out.println("Flight: Landing");
            phaser.arriveAndAwaitAdvance();
            System.out.println("Flight: We have landed successfully");
        }
    }

    @Override
    public void update() {
        if (altitude.getAltitudeState().getClass().equals(CruisingState.class)) {
            nextPhase();
            System.out.println("Flight: Cruising");
        }
    }

    private void addObserver(Observer observer) {
        observers.add(observer);
    }

    public Altitude getAltitude() {
        return altitude;
    }
}
