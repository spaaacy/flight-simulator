package org.flightcontrol;

import org.flightcontrol.flight.Flight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.flightcontrol.actuator.tailflap.TailFlap.TAIL_FLAP_ID;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_ID;
import static org.flightcontrol.flight.Flight.FLIGHT_ID;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_ID;
import static org.flightcontrol.sensor.gps.GPS.GPS_ID;

// TODO: User constructor for GUI
public class ControlSystem implements Observer {

    JLabel flightValue;
    JLabel altitudeValue;
    JLabel wingFlapValue;
    JLabel gpsValue;
    JLabel tailFlapValue;

    public static void main(String[] args) {
        ControlSystem controlSystem = new ControlSystem();
        Flight flight = new Flight(controlSystem);

        controlSystem.gui(flight);

        ScheduledExecutorService flightControlSystem = Executors.newScheduledThreadPool(1);
        flightControlSystem.submit(flight);
    }

    @Override
    public void update(String... updatedValue) {
        if (updatedValue.length != 0) {
            switch (updatedValue[0]) {
                case FLIGHT_ID -> flightValue.setText(updatedValue[1]);
                case ALTITUDE_ID -> altitudeValue.setText(updatedValue[1]);
                case WING_FLAP_ID -> wingFlapValue.setText(updatedValue[1]);
                case GPS_ID -> gpsValue.setText(updatedValue[1]);
                case TAIL_FLAP_ID -> tailFlapValue.setText(updatedValue[1]);
            }
        }
    }

    private void gui(Flight flight) {

        LinkedList<JLabel> labels = new LinkedList<>();
        LinkedList<JButton> buttons = new LinkedList<>();

        JFrame jFrame = new JFrame("Flight Control System");
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel jPanel = new JPanel(new GridLayout(5, 2));
        jFrame.setSize(600, 400);

        JLabel title = new JLabel("Boeing 777 Control System");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel();

        JLabel flightLabel = new JLabel("Phase:");
        JLabel altitudeLabel = new JLabel("Altitude:");
        JLabel wingFlapLabel = new JLabel("Wing Flaps:");
        JLabel gpsLabel = new JLabel("GPS:");
        JLabel tailFlapLabel = new JLabel("Tail Flap:");

        flightValue = new JLabel();
        altitudeValue = new JLabel();
        wingFlapValue = new JLabel();
        gpsValue = new JLabel();
        tailFlapValue = new JLabel();

        // Add all JLabels to linked list
        labels.add(flightLabel);
        labels.add(flightValue);
        labels.add(altitudeLabel);
        labels.add(altitudeValue);
        labels.add(wingFlapLabel);
        labels.add(wingFlapValue);
        labels.add(gpsLabel);
        labels.add(gpsValue);
        labels.add(tailFlapLabel);
        labels.add(tailFlapValue);

        // Set parameters for JLabels
        for (JLabel label: labels) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Courier", Font.PLAIN, 20));
            jPanel.add(label);
        }

        /*
         Buttons
         */
        JButton takeoffButton = new JButton("Initiate Takeoff");
        takeoffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flight.initiateTakeoff();
            }
        });
        buttons.add(takeoffButton);

        JButton landingButton = new JButton("Initiate Landing");
        landingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flight.initiateLanding();
            }
        });
        buttons.add(landingButton);

        for (JButton button : buttons){
            buttonPanel.add(button);
        }


        mainPanel.add(title, BorderLayout.PAGE_START);
        mainPanel.add(jPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
        jFrame.add(mainPanel);
        jFrame.setVisible(true);

    }

}