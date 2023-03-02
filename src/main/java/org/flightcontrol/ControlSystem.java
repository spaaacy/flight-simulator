package org.flightcontrol;

import org.flightcontrol.flight.Flight;
import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ControlSystem {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Timer timer = new Timer();
        Flight flight = new Flight();

        ScheduledExecutorService flightControlSystem = Executors.newScheduledThreadPool(2);
        flightControlSystem.schedule(flight, 500L, TimeUnit.MILLISECONDS); // Delay to prevent race-condition
        flightControlSystem.submit(flight.getAltitude());

        TimerTask landingTask = new TimerTask() {
            @Override
            public void run() {
                scanner.nextLine();
                flight.initiateLanding();
            }
        };
        timer.scheduleAtFixedRate(landingTask, 500L, 500L);

    }

}