package org.flightcontrol;

import org.flightcontrol.flight.Flight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.flightcontrol.actuator.landinggear.LandingGear.LANDING_GEAR_ID;
import static org.flightcontrol.actuator.oxygenmasks.OxygenMask.OXYGEN_MASK_ID;
import static org.flightcontrol.actuator.tailflap.TailFlap.TAIL_FLAP_ID;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_ID;
import static org.flightcontrol.flight.Flight.FLIGHT_ID;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_ID;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.*;
import static org.flightcontrol.sensor.gps.GPS.GPS_ID;

public class ControlSystem implements Observer {

    Flight flight = new Flight(this);

    JLabel flightValue = new JLabel();
    JLabel altitudeValue = new JLabel();
    JLabel wingFlapValue = new JLabel();
    JLabel gpsValue = new JLabel();
    JLabel tailFlapValue = new JLabel();
    JLabel landingGearValue = new JLabel();
    JLabel cabinPressureStatusValue = new JLabel();
    JLabel cabinPressurePsiValue = new JLabel();
    JLabel oxygenMaskValue = new JLabel();

    public static void main(String[] args) {
        ControlSystem controlSystem = new ControlSystem();

        // TODO: Research pool size
        ScheduledExecutorService flightControlSystem = Executors.newScheduledThreadPool(1);
        flightControlSystem.submit(controlSystem.flight);
    }

    // TODO: Add gps destination

    @Override
    public void update(String... updatedValue) {
        if (updatedValue.length != 0) {
            switch (updatedValue[0]) {
                case FLIGHT_ID -> flightValue.setText(updatedValue[1]);
                case ALTITUDE_ID -> altitudeValue.setText(updatedValue[1]);
                case WING_FLAP_ID -> wingFlapValue.setText(updatedValue[1]);
                case GPS_ID -> gpsValue.setText(updatedValue[1]);
                case TAIL_FLAP_ID -> tailFlapValue.setText(updatedValue[1]);
                case LANDING_GEAR_ID -> landingGearValue.setText(updatedValue[1]);
                case CABIN_PRESSURE_ID -> {
                    if (updatedValue[1].equals(STATUS_ID)) {
                        cabinPressureStatusValue.setText(updatedValue[2]);
                    } else if (updatedValue[1].equals(PSI_ID)) {
                        cabinPressurePsiValue.setText(updatedValue[2]);
                    }
                }
                case OXYGEN_MASK_ID -> oxygenMaskValue.setText(updatedValue[1]);
            }
        }
    }

    ControlSystem() {
        LinkedList<JLabel> labels = new LinkedList<>();
        LinkedList<JButton> buttons = new LinkedList<>();

        JFrame jFrame = new JFrame("Flight Control System");
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel jPanel = new JPanel(new GridLayout(9, 2));
        jFrame.setSize(500, 500);

        JLabel title = new JLabel("Boeing 777 Control System");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel();

        JLabel flightLabel = new JLabel("Phase:");
        JLabel altitudeLabel = new JLabel("Altitude:");
        JLabel wingFlapLabel = new JLabel("Wing Flaps:");
        JLabel gpsLabel = new JLabel("GPS:");
        JLabel tailFlapLabel = new JLabel("Tail Flap:");
        JLabel landingGearLabel = new JLabel("Landing Gear:");
        JLabel cabinPressureStatusLabel = new JLabel("Cabin Pressure Status:");
        JLabel cabinPressurePsiLabel = new JLabel("Cabin Pressure:");
        JLabel oxygenMaskLabel = new JLabel("Oxygen Masks:");

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
        labels.add(landingGearLabel);
        labels.add(landingGearValue);
        labels.add(cabinPressureStatusLabel);
        labels.add(cabinPressureStatusValue);
        labels.add(cabinPressurePsiLabel);
        labels.add(cabinPressurePsiValue);
        labels.add(oxygenMaskLabel);
        labels.add(oxygenMaskValue);

        // Set parameters for JLabels
        for (JLabel label: labels) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Courier", Font.PLAIN, 20));
            jPanel.add(label);
        }

        /*
         * Buttons
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

        JButton pressureButton = new JButton("Toggle Cabin Pressure");
        pressureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flight.toggleCabinPressure();
            }
        });
        buttons.add(pressureButton);

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