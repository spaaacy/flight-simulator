package org.flightcontrol;

import org.flightcontrol.sensor.Altitude;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;

public class FlightControlSystem {

    static Phaser phaser;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        phaser = new Phaser(1);

        System.out.println("FlightControlSystem: Beginning takeoff");

        ScheduledExecutorService flightControlSystem = Executors.newScheduledThreadPool(1);
        flightControlSystem.submit(new Altitude(phaser));

        phaser.arriveAndAwaitAdvance();

//        scanner.nextLine();

        phaser.arriveAndAwaitAdvance();

    }
}