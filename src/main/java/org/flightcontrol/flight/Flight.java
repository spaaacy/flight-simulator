package org.flightcontrol.flight;

import org.flightcontrol.ControlSystem;
import org.flightcontrol.Observer;
import org.flightcontrol.actuator.tailflap.TailFlap;
import org.flightcontrol.actuator.wingflap.WingFlap;
import org.flightcontrol.sensor.altitude.Altitude;
import org.flightcontrol.sensor.gps.GPS;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_ID;
import static org.flightcontrol.sensor.altitude.Altitude.CRUISING_FLAG;

public class Flight implements Runnable, Observer {

    public static final String FLIGHT_IDENTIFIER = "Flight";
    public static final Long TICK_RATE = 250L;

    Phaser phaser = new Phaser(1);
    LinkedList<Observer> observers = new LinkedList<>();
    Timer timer = new Timer();
    ControlSystem controlSystem;

    // Sensors
    Altitude altitude = new Altitude(phaser);
    GPS gps = new GPS();

    // Actuators
    WingFlap wingFlap = new WingFlap();
    TailFlap tailFlap = new TailFlap();


    public Flight(ControlSystem controlSystem) {
        this.controlSystem = controlSystem;

        altitude.addObserver(this);

        // Objects observing flight phase
        addObserver(altitude);
        addObserver(wingFlap);
        addObserver(gps);
        addObserver(tailFlap);

        // Objects observed by control system for GUI
        altitude.addObserver(controlSystem);
        wingFlap.addObserver(controlSystem);
        gps.addObserver(controlSystem);
        tailFlap.addObserver(controlSystem);
    }

    @Override
    public void run() {
        nextPhase();
        System.out.println("Flight: TAKING OFF");
    }

    private void nextPhase() {
        phaser.arrive();

        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                for (Observer observer : observers) {
                    observer.update(FLIGHT_IDENTIFIER, Integer.toString(phaser.getPhase()));
                }
            }
        };
        timer.schedule(updateTask, 500L);
    }

    public void initiateLanding() {
        if (phaser.getPhase() == 2) {
            nextPhase();
            System.out.println("Flight: LANDING");

            phaser.arriveAndAwaitAdvance();
            System.out.println("Flight: LANDED");
        }
    }

    @Override
    public void update(String... updatedValue) {
        if (updatedValue.length != 0 && updatedValue[0].equals(CRUISING_FLAG)) {
            nextPhase();
            System.out.println("Flight: CRUISING");
        }
    }

    private void addObserver(Observer observer) {
        observers.add(observer);
    }

    public Altitude getAltitude() {
        return altitude;
    }


}
