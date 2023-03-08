package org.flightcontrol;

import org.flightcontrol.flight.Flight;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.flightcontrol.actuator.landinggear.LandingGear.LANDING_GEAR_ALTITUDE;
import static org.flightcontrol.actuator.landinggear.LandingGear.LANDING_GEAR_ID;
import static org.flightcontrol.actuator.oxygenmasks.OxygenMask.OXYGEN_MASK_ID;
import static org.flightcontrol.actuator.tailflap.TailFlap.TAIL_FLAP_ID;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_ID;
import static org.flightcontrol.flight.Flight.*;
import static org.flightcontrol.sensor.altitude.Altitude.*;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.*;
import static org.flightcontrol.sensor.engine.Engine.*;
import static org.flightcontrol.sensor.gps.GPS.BEARING_DESTINATION;
import static org.flightcontrol.sensor.gps.GPS.GPS_ID;
import static org.flightcontrol.sensor.gps.GPS.BEARING_UNIT;

public class ControlSystem extends TimerTask implements Observer {


    private static final String GUI_FONT = "Arial";

    LinkedList<JPanel> panels = new LinkedList<>();
    LinkedList<JLabel> labels = new LinkedList<>();
    LinkedList<JButton> buttons = new LinkedList<>();

    JLabel flightValue = new JLabel();
    JLabel altitudeValue = new JLabel();
    JLabel wingFlapValue = new JLabel();
    JLabel gpsValue = new JLabel();
    JLabel tailFlapValue = new JLabel();
    JLabel landingGearValue = new JLabel();
    JLabel cabinPressureStatusValue = new JLabel();
    JLabel cabinPressurePsiValue = new JLabel();
    JLabel oxygenMaskValue = new JLabel();
    JLabel engineValue = new JLabel();

    Flight flight = new Flight(this);

    public static void main(String[] args) {
        ControlSystem controlSystem = new ControlSystem();
        Timer timer = new Timer();
        timer.schedule(controlSystem, 0L, 2000L);
    }


    // TODO: Add description column

    public void restartApplication()
    {
        try {
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar = new File(ControlSystem.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            /* is it a jar file? */
            if(!currentJar.getName().endsWith(".jar")) {
                return;
            }

            /* Build command: java -jar application.jar */
            final ArrayList<String> command = new ArrayList<String>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());

            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        } catch (URISyntaxException | IOException ignored) {}
    }

    @Override
    public void update(String... updatedValue) {
        if (updatedValue.length != 0) {
            switch (updatedValue[0]) {
                case FLIGHT_ID -> {
                    String flightPhase = updatedValue[1];
                    if (!flightValue.getText().equals(flightPhase)){
                        flightValue.setText(flightPhase);
                        flightValue.setForeground(Color.BLUE);
                    }

                }
                case ALTITUDE_ID -> altitudeValue.setText(updatedValue[1]);
                case WING_FLAP_ID -> {
                    String wingFlap = updatedValue[1];
                    if(!wingFlapValue.getText().equals(wingFlap)){
                        wingFlapValue.setText(wingFlap);
                        wingFlapValue.setForeground(Color.BLUE);
                    }
                }
                case GPS_ID -> gpsValue.setText(updatedValue[1]);
                case TAIL_FLAP_ID -> {
                    String tailFlap = updatedValue[1];
                    if (!tailFlapValue.getText().equals(tailFlap)){
                        tailFlapValue.setText(tailFlap);
                        tailFlapValue.setForeground(Color.BLUE);
                    }
                }
                case LANDING_GEAR_ID -> {
                    String landingGear = updatedValue[1];
                    if (!landingGearValue.getText().equals(landingGear)){
                        landingGearValue.setText(landingGear);
                        landingGearValue.setForeground(Color.BLUE);
                    }
                }
                case CABIN_PRESSURE_ID -> {
                    if (updatedValue[1].equals(STATUS_ID)) {
                        String cabinPressureStatus = updatedValue[2];
                        if (!cabinPressureStatusValue.getText().equals(cabinPressureStatus)){
                            cabinPressureStatusValue.setText(cabinPressureStatus);
                            cabinPressureStatusValue.setForeground(Color.BLUE);
                        }
                    } else if (updatedValue[1].equals(PSI_ID)) {
                        cabinPressurePsiValue.setText(updatedValue[2]);
                    }
                }
                case OXYGEN_MASK_ID -> {
                    String oxygenMask = updatedValue[1];
                    if(!oxygenMaskValue.getText().equals(oxygenMask)) {
                        oxygenMaskValue.setText(oxygenMask);
                        oxygenMaskValue.setForeground(Color.BLUE);
                    }
                }
                case ENGINE_ID -> engineValue.setText(updatedValue[1]);
            }
        }
    }

    @Override
    public void run() {
        for (JLabel label: labels) {
            label.setForeground(Color.BLACK);
        }
    }

    ControlSystem() {
        JFrame jFrame = new JFrame("Flight Control System");
        jFrame.setSize(900, 750);

        /*
        * Panels
        * */
        JPanel mainPanel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEmptyBorder(25,25,25,25);
        mainPanel.setBorder(border);
        panels.add(mainPanel);

        JPanel labelPanel = new JPanel(new GridLayout(10, 3));
        panels.add(labelPanel);
        JPanel buttonPanel = new JPanel();
        panels.add(buttonPanel);

        for (JPanel panel: panels) {
            panel.setBackground(Color.LIGHT_GRAY);
        }

        /*
         * Labels
         * */
        JLabel title = new JLabel("Boeing 777 Control System");
        title.setForeground(Color.RED);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel flightLabel = new JLabel("Phase:", SwingConstants.LEFT);
        JLabel altitudeLabel = new JLabel("Altitude:", SwingConstants.LEFT);
        JLabel wingFlapLabel = new JLabel("Wing Flaps:", SwingConstants.LEFT);
        JLabel gpsLabel = new JLabel("GPS:", SwingConstants.LEFT);
        JLabel tailFlapLabel = new JLabel("Tail Flap:", SwingConstants.LEFT);
        JLabel landingGearLabel = new JLabel("Landing Gear:", SwingConstants.LEFT);
        JLabel cabinPressureStatusLabel = new JLabel("Cabin Pressure Status:", SwingConstants.LEFT);
        JLabel cabinPressurePsiLabel = new JLabel("Cabin Pressure:", SwingConstants.LEFT);
        JLabel oxygenMaskLabel = new JLabel("Oxygen Masks:", SwingConstants.LEFT);
        JLabel engineLabel = new JLabel("Engine:", SwingConstants.LEFT);

        JLabel flightDescription = new JLabel("<html><b>Description:");

        JLabel altitudeDescription = new JLabel("<html><p style=\"text-align:center\">Cruising altitude is<br>" + (CRUISING_ALTITUDE-ALTITUDE_ACCEPTED_DIFFERENCE) + HEIGHT_UNIT +
                " to " + (CRUISING_ALTITUDE+ALTITUDE_ACCEPTED_DIFFERENCE) + HEIGHT_UNIT);
        JLabel wingFlapDescription = new JLabel("");
        JLabel gpsDescription = new JLabel("Destination is at " + BEARING_DESTINATION + BEARING_UNIT);
        JLabel tailFlapDescription = new JLabel("");
        JLabel landingGearDescription = new JLabel("Deploys at " + LANDING_GEAR_ALTITUDE + HEIGHT_UNIT);
        JLabel cabinPressureStatusDescription = new JLabel("");
        JLabel cabinPressurePsiDescription = new JLabel("");
        JLabel oxygenMaskDescription = new JLabel("");
        JLabel engineDescription = new JLabel("Takeoff/Landing at " + TAKEOFF_LANDING_PERCENTAGE + PERCENTAGE_UNIT);

        labels.add(flightLabel);
        labels.add(flightValue);
        labels.add(flightDescription);

        labels.add(altitudeLabel);
        labels.add(altitudeValue);
        labels.add(altitudeDescription);

        labels.add(wingFlapLabel);
        labels.add(wingFlapValue);
        labels.add(wingFlapDescription);

        labels.add(gpsLabel);
        labels.add(gpsValue);
        labels.add(gpsDescription);

        labels.add(tailFlapLabel);
        labels.add(tailFlapValue);
        labels.add(tailFlapDescription);

        labels.add(landingGearLabel);
        labels.add(landingGearValue);
        labels.add(landingGearDescription);

        labels.add(cabinPressureStatusLabel);
        labels.add(cabinPressureStatusValue);
        labels.add(cabinPressureStatusDescription);

        labels.add(cabinPressurePsiLabel);
        labels.add(cabinPressurePsiValue);
        labels.add(cabinPressurePsiDescription);

        labels.add(oxygenMaskLabel);
        labels.add(oxygenMaskValue);
        labels.add(oxygenMaskDescription);

        labels.add(engineLabel);
        labels.add(engineValue);
        labels.add(engineDescription);

        for (JLabel label: labels) {
            if (label.getHorizontalAlignment() != SwingConstants.LEFT){
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font(GUI_FONT, Font.PLAIN, 20));
            } else {
                label.setFont(new Font(GUI_FONT, Font.BOLD, 20));
            }

            labelPanel.add(label);
        }

        /*
         * Buttons
         */
        JButton takeoffButton = new JButton("Takeoff");
        takeoffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flight.initiateTakeoff();
            }
        });
        buttons.add(takeoffButton);

        JButton landingButton = new JButton("Land");
        landingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flight.initiateLanding();
            }
        });
        buttons.add(landingButton);

        JButton pressureButton = new JButton("Cabin Pressure");
        pressureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flight.toggleCabinPressure();
            }
        });
        buttons.add(pressureButton);

        JButton relaunchButton = new JButton("Relaunch");
        relaunchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartApplication();
            }
        });
        buttons.add(relaunchButton);

        for (JButton button : buttons){
            button.setPreferredSize(new Dimension(150, 40));
            buttonPanel.add(button);
        }

        mainPanel.add(title, BorderLayout.PAGE_START);
        mainPanel.add(labelPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
        jFrame.add(mainPanel);
        jFrame.setVisible(true);


    }

}