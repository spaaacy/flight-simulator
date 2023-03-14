package org.flightcontrol;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.flightcontrol.ControlSystem.GUI_FONT;

public class Performance {

    private static ArrayList<Long> sendAltitudeTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveAltitudeTimestamps = new ArrayList<>();
    private static int altitudeCount = 0;
    private static Long averageAltitude = 0L;
    private static JLabel altitudeLatency = new JLabel();

    private static ArrayList<Long> sendGpsTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveGpsTimestamps = new ArrayList<>();
    private static int gpsCount = 0;
    private static Long averageGps = 0L;

    private static ArrayList<Long> sendCabinPressureTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveCabinPressureTimestamps = new ArrayList<>();
    private static int cabinPressureCount = 0;

    private static ArrayList<Long> receiveLandingGearTimestamps = new ArrayList<>();
    private static int landingGearCount = 0;
    private static Long averageLandingGear = 0L;
    private static Long averageCabinPressure = 0L;

    private static ArrayList<Long> sendFlightTimestamps = new ArrayList<>();

    private static LinkedList<JLabel> labels = new LinkedList<>();
    private static JLabel gpsLatency = new JLabel();
    private static JLabel cabinPressureLatency = new JLabel();
    private static JLabel landingGearLatency = new JLabel();

    public static void recordSendAltitude() {
        sendAltitudeTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveAltitude() {
        receiveAltitudeTimestamps.add(System.currentTimeMillis());
        averageAltitudeLatency();
    }

    private static void averageAltitudeLatency() {
        Long value1 = sendAltitudeTimestamps.get(sendAltitudeTimestamps.size() - 1);
        Long value2 = receiveAltitudeTimestamps.get(receiveAltitudeTimestamps.size() - 1);
        Long difference = value2 - value1;

        Long newAverage = averageAltitude * altitudeCount + difference;
        altitudeCount++;
        newAverage = newAverage / altitudeCount;
        averageAltitude = newAverage;

        altitudeLatency.setText(averageAltitude + " ms");
    }

    public static void recordSendGps() {
        sendGpsTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveGps() {
        receiveGpsTimestamps.add(System.currentTimeMillis());
        averageGpsLatency();
    }

    private static void averageGpsLatency() {
        Long value1 = sendGpsTimestamps.get(sendGpsTimestamps.size() - 1);
        Long value2 = receiveGpsTimestamps.get(receiveGpsTimestamps.size() - 1);
        Long difference = value2 - value1;
        Long newAverage = averageGps * gpsCount + difference;
        gpsCount++;
        newAverage = newAverage / gpsCount;
        averageGps = newAverage;
        gpsLatency.setText(averageGps + " ms");
    }

    public static void recordSendCabinPressure() {
        sendCabinPressureTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveCabinPressure() {
        receiveCabinPressureTimestamps.add(System.currentTimeMillis());
        averageCabinPressureLatency();
    }

    private static void averageCabinPressureLatency() {
        Long value1 = sendCabinPressureTimestamps.get(sendCabinPressureTimestamps.size() - 1);
        Long value2 = receiveCabinPressureTimestamps.get(receiveCabinPressureTimestamps.size() - 1);
        Long difference = value2 - value1;
        Long newAverage = averageCabinPressure * cabinPressureCount + difference;
        cabinPressureCount++;
        newAverage = newAverage / cabinPressureCount;
        averageCabinPressure = newAverage;
        cabinPressureLatency.setText(averageCabinPressure + " ms");
    }

    public static void recordReceiveLandingGear() {
        receiveLandingGearTimestamps.add(System.currentTimeMillis());
        averageLandingGearLatency();
    }

    private static void averageLandingGearLatency () {
        Long value1 = sendAltitudeTimestamps.get(sendAltitudeTimestamps.size() - 1);
        Long value2 = receiveLandingGearTimestamps.get(receiveLandingGearTimestamps.size() - 1);
        Long difference = value2 - value1;
        Long newAverage = averageLandingGear * landingGearCount + difference;
        landingGearCount++;
        newAverage = newAverage / landingGearCount;
        averageLandingGear = newAverage;
        landingGearLatency.setText(averageLandingGear + " ms");
    }

    public static void recordSendFlight() {
        sendFlightTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveFlightAltitude() {

    }

    public static void recordReceiveFlightWingFlap() {

    }

    public static void recordReceiveFlightGps() {

    }

    public static void recordReceiveFlightTailFlap() {

    }

    public static void gui() {
        JFrame jFrame = new JFrame("Performance");
        jFrame.setSize(400, 200);
        JPanel mainPanel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEmptyBorder(25, 25, 25, 25);
        mainPanel.setBorder(border);
        JPanel labelPanel = new JPanel(new GridLayout(5, 2));
        JPanel titlePanel = new JPanel(new FlowLayout());
        JLabel title = new JLabel("Performance");
        title.setFont(new Font(GUI_FONT, Font.BOLD, 25));
        title.setForeground(Color.RED);
        titlePanel.add(title);

        mainPanel.add(titlePanel, BorderLayout.PAGE_START);
        mainPanel.add(labelPanel, BorderLayout.CENTER);
        jFrame.add(mainPanel);

        JLabel altitudeLabel = new JLabel("Altitude latency");
        labels.add(altitudeLabel);
        labels.add(altitudeLatency);

        JLabel gpsLabel = new JLabel("GPS latency");
        labels.add(gpsLabel);
        labels.add(gpsLatency);

        JLabel cabinPressureLabel = new JLabel("Cabin Pressure latency");
        labels.add(cabinPressureLabel);
        labels.add(cabinPressureLatency);

        JLabel landingGearLabel = new JLabel("Landing Gear latency");
        labels.add(landingGearLabel);
        labels.add(landingGearLatency);

        for (JLabel label : labels) {
            labelPanel.add(label);
        }

        jFrame.setVisible(true);
    }

}
