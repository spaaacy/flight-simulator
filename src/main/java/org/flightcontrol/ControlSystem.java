package org.flightcontrol;

import org.flightcontrol.flight.Flight;
import org.flightcontrol.sensor.altitude.Altitude;

import javax.swing.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_CLASS_NAME;

public class ControlSystem implements Observer {

    JLabel altitudeValue;

    public static void main(String[] args) {
        ControlSystem controlSystem = new ControlSystem();
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

//        flight.getAltitude().addObserver(controlSystem);
        controlSystem.gui();

    }

    @Override
    public void update(String... updatedValue) {
        switch (updatedValue[0]) {
            case ALTITUDE_CLASS_NAME -> {
                altitudeValue.setText(updatedValue[1]);
            }
        }
    }

    private void gui() {

        JFrame jFrame = new JFrame("Flight Control System");
        JPanel jPanel = new JPanel();
        jFrame.setSize(500, 500);
        JLabel altitudeLabel = new JLabel("Altitude");
        altitudeValue = new JLabel();
        jPanel.add(altitudeLabel);
        jPanel.add(altitudeValue);
        jFrame.add(jPanel);
        jFrame.setVisible(true);


    }

}