package org.flightcontrol;

import org.flightcontrol.flight.Flight;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.flightcontrol.actuator.tailflap.TailFlap.TAIL_FLAP_ID;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_ID;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_ID;
import static org.flightcontrol.sensor.gps.GPS.GPS_ID;

public class ControlSystem implements Observer {

    JLabel altitudeValue;
    JLabel wingFlapValue;
    JLabel gpsValue;
    JLabel tailFlapValue;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Timer timer = new Timer();
        ControlSystem controlSystem = new ControlSystem();
        Flight flight = new Flight(controlSystem);

        ScheduledExecutorService flightControlSystem = Executors.newScheduledThreadPool(2);
        flightControlSystem.schedule(flight, 500L, TimeUnit.MILLISECONDS); // Delay to prevent race-condition with phaser
        flightControlSystem.submit(flight.getAltitude());

        TimerTask landingTask = new TimerTask() {
            @Override
            public void run() {
                scanner.nextLine();
                flight.initiateLanding();
            }
        };
        timer.scheduleAtFixedRate(landingTask, 500L, 500L);

        controlSystem.gui();

    }

    @Override
    public void update(String... updatedValue) {
        if (updatedValue.length != 0) {
            switch (updatedValue[0]) {
                case ALTITUDE_ID -> altitudeValue.setText(updatedValue[1]);
                case WING_FLAP_ID -> wingFlapValue.setText(updatedValue[1]);
                case GPS_ID -> gpsValue.setText(updatedValue[1]);
                case TAIL_FLAP_ID -> tailFlapValue.setText(updatedValue[1]);
            }
        }
    }

    private void gui() {

        LinkedList<JLabel> jLabels = new LinkedList<>();

        JFrame jFrame = new JFrame("Flight Control System");
        JPanel 
        JPanel jPanel = new JPanel(new GridLayout(4, 2));
        jFrame.setSize(400, 400);

        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Boeing 777 Control System");
        title.setFont(new Font("Arial", Font.BOLD, 35));
        titlePanel.add(title);

        JLabel altitudeLabel = new JLabel("Altitude:");
        JLabel wingFlapLabel = new JLabel("Wing Flaps:");
        JLabel gpsLabel = new JLabel("GPS:");
        JLabel tailFlapLabel = new JLabel("Tail Flap:");

        altitudeValue = new JLabel();
        wingFlapValue = new JLabel();
        gpsValue = new JLabel();
        tailFlapValue = new JLabel();

        // Add all JLabel to linked list
        jLabels.add(altitudeLabel);
        jLabels.add(altitudeValue);
        jLabels.add(wingFlapLabel);
        jLabels.add(wingFlapValue);
        jLabels.add(gpsLabel);
        jLabels.add(gpsValue);
        jLabels.add(tailFlapLabel);
        jLabels.add(tailFlapValue);

        // Set parameters for JLabels
        for (JLabel jLabel: jLabels) {
            jLabel.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel.setFont(new Font("Arial", Font.BOLD, 25));
            jPanel.add(jLabel);
        }


        jFrame.add(titlePanel);
        jFrame.add(jPanel);
        jFrame.setVisible(true);

    }

}