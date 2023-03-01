package org.flightcontrol;

import org.flightcontrol.flight.Flight;
import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;

public class ControlSystem {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Phaser phaser = new Phaser(1);
        Flight flight = new Flight(phaser);

        ScheduledExecutorService flightControlSystem = Executors.newScheduledThreadPool(1);
        flightControlSystem.submit(flight.altitude);
        phaser.awaitAdvance(3);
        System.out.println("Landing");

    }

}