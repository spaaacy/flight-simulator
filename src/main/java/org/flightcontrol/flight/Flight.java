package org.flightcontrol.flight;

import org.flightcontrol.Observer;
import org.flightcontrol.actuator.wingflap.WingFlap;
import org.flightcontrol.sensor.altitude.Altitude;
import org.flightcontrol.sensor.altitude.CruisingState;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

public class Flight implements Runnable, Observer {

    public static final Long TICK_RATE = 500L;

    Phaser phaser = new Phaser(1);
    LinkedList<Observer> observers = new LinkedList<>();
    Timer timer = new Timer();

    // Sensors
    Altitude altitude = new Altitude(phaser);

    // Actuators
    WingFlap wingFlap = new WingFlap(altitude, phaser);


    public Flight() {
        // Objects observing flight
        addObserver(altitude);
        addObserver(wingFlap);

        // Objects observing altitude
        altitude.addObserver(this);
    }

    @Override
    public void run() {
        nextPhase();
        System.out.println("Flight: Taking off");
    }

    private void nextPhase() {
        phaser.arrive();

        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                for (Observer observer : observers) {
                    observer.update();
                }
            }
        };
        timer.schedule(updateTask, 500L);
    }

    public void initiateLanding() {
        if (phaser.getPhase() == 2) {
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
