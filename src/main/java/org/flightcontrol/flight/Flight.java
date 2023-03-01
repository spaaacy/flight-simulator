package org.flightcontrol.flight;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.concurrent.Phaser;

public class Flight {

    Phaser phaser;
    public Altitude altitude;

    public Flight(Phaser phaser) {
        this.phaser = phaser;
        altitude = new Altitude(phaser);
    }

    public void changeState(FlightState newState) {

    }
}