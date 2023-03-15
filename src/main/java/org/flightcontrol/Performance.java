package org.flightcontrol;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.flightcontrol.ControlSystem.GUI_FONT;

public class Performance {

    // Altitude
    private static ArrayList<Long> sendAltitudeTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveAltitudeTimestamps = new ArrayList<>();
    private static int altitudeCount = 0;
    private static Long averageAltitude = 0L;

    // GPS
    private static ArrayList<Long> sendGpsTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveGpsTimestamps = new ArrayList<>();
    private static int gpsCount = 0;
    private static Long averageGps = 0L;

    // Engine
    private static ArrayList<Long> sendEngineTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveEngineTimestamps = new ArrayList<>();
    private static int engineCount = 0;
    private static Long averageEngine = 0L;

    // Cabin Pressure
    private static ArrayList<Long> sendCabinPressureTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveCabinPressureTimestamps = new ArrayList<>();
    private static int cabinPressureCount = 0;
    private static Long averageCabinPressure = 0L;

    // Landing Gear
    private static ArrayList<Long> sendLandingGearTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveLandingGearTimestamps = new ArrayList<>();
    private static int landingGearCount = 0;
    private static Long averageLandingGear = 0L;

    // Flight
    private static ArrayList<Long> sendFlightTimestamps = new ArrayList<>();
    private static ArrayList<Long> receiveFlightAltitudeTimestamps = new ArrayList<>();
    private static int flightAltitudeCount = 0;
    private static Long averageFlightAltitude = 0L;
    private static ArrayList<Long> receiveFlightWingFlapTimestamps = new ArrayList<>();
    private static int flightWingFlapCount = 0;
    private static Long averageFlightWingFlap = 0L;
    private static ArrayList<Long> receiveFlightGpsTimestamps = new ArrayList<>();
    private static int flightGpsCount = 0;
    private static Long averageFlightGps = 0L;
    private static ArrayList<Long> receiveFlightTailFlapTimestamps = new ArrayList<>();
    private static int flightTailFlapCount = 0;
    private static Long averageFlightTailFlap = 0L;
    private static ArrayList<Long> receiveFlightCabinPressureTimestamps = new ArrayList<>();
    private static int flightCabinPressureCount = 0;
    private static Long averageFlightCabinPressure = 0L;
    private static ArrayList<Long> receiveFlightEngineTimestamps = new ArrayList<>();
    private static int flightEngineCount = 0;
    private static Long averageFlightEngine = 0L;
    private static ArrayList<Long> receiveFlightLandingGearTimestamps = new ArrayList<>();
    private static int flightLandingGearCount = 0;
    private static Long averageFlightLandingGear = 0L;

    // JLabels
    private static LinkedList<JLabel> labels = new LinkedList<>();
    private static JLabel altitudeLatency = new JLabel();
    private static JLabel gpsLatency = new JLabel();
    private static JLabel engineLatency = new JLabel();
    private static JLabel cabinPressureLatency = new JLabel();
    private static JLabel landingGearLatency = new JLabel();
    // Flight related JLabels
    private static JLabel flightAltitudeLatency = new JLabel();
    private static JLabel flightWingFlapLatency = new JLabel();
    private static JLabel flightGpsLatency = new JLabel();
    private static JLabel flightTailFlapLatency = new JLabel();
    private static JLabel flightCabinPressureLatency = new JLabel();
    private static JLabel flightLandingGearLatency = new JLabel();
    private static JLabel flightEngineLatency = new JLabel();

    private static Long calculateAverageLatency(ArrayList<Long> array1, ArrayList<Long> array2,
                                                Long average, int averageCount, JLabel label) {
        Long value1 = array1.get(array1.size() - 1);
        Long value2 = array2.get(array2.size() - 1);
        Long difference = value2 - value1;
        Long newAverage = average * averageCount + difference;
        averageCount++;
        newAverage = newAverage / averageCount;
        label.setText(newAverage + " ms");
        return newAverage;
    }

    public static void recordSendAltitude() {
        sendAltitudeTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveAltitude() {
        receiveAltitudeTimestamps.add(System.currentTimeMillis());
        averageAltitude = calculateAverageLatency(sendAltitudeTimestamps, receiveAltitudeTimestamps,
                averageAltitude, altitudeCount, altitudeLatency);
    }

    public static void recordSendGps() {
        sendGpsTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveGps() {
        receiveGpsTimestamps.add(System.currentTimeMillis());
        averageGps = calculateAverageLatency(sendGpsTimestamps, receiveGpsTimestamps,
                averageGps, gpsCount, gpsLatency);
    }

    public static void recordSendEngine() {
        sendEngineTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveEngine() {
        receiveEngineTimestamps.add(System.currentTimeMillis());
        averageEngine = calculateAverageLatency(sendEngineTimestamps, receiveEngineTimestamps,
                averageEngine, engineCount, engineLatency);
    }

    public static void recordSendCabinPressure() {
        sendCabinPressureTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveCabinPressure() {
        receiveCabinPressureTimestamps.add(System.currentTimeMillis());
        averageCabinPressure = calculateAverageLatency(sendCabinPressureTimestamps, receiveCabinPressureTimestamps,
                averageCabinPressure, cabinPressureCount, cabinPressureLatency) ;
    }
    public static void recordSendLandingGear() {
        sendLandingGearTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveLandingGear() {
        receiveLandingGearTimestamps.add(System.currentTimeMillis());
        averageLandingGear = calculateAverageLatency(sendLandingGearTimestamps, receiveLandingGearTimestamps,
                averageLandingGear, landingGearCount, landingGearLatency);
    }


    /*
    * Flight functions
    * */
    public static void recordSendFlight() {
        sendFlightTimestamps.add(System.currentTimeMillis());
    }

    public static void recordReceiveFlightAltitude() {
        receiveFlightAltitudeTimestamps.add(System.currentTimeMillis());
        averageFlightAltitude = calculateAverageLatency(sendFlightTimestamps, receiveFlightAltitudeTimestamps,
                averageFlightAltitude, flightAltitudeCount, flightAltitudeLatency);
    }

    public static void recordReceiveFlightWingFlap() {
        receiveFlightWingFlapTimestamps.add(System.currentTimeMillis());
        averageFlightWingFlap = calculateAverageLatency(sendFlightTimestamps, receiveFlightWingFlapTimestamps,
                averageFlightWingFlap, flightWingFlapCount, flightWingFlapLatency);
    }

    public static void recordReceiveFlightGps() {
        receiveFlightGpsTimestamps.add(System.currentTimeMillis());
        averageFlightGps = calculateAverageLatency(sendFlightTimestamps, receiveFlightGpsTimestamps,
                averageFlightGps, flightGpsCount, flightGpsLatency);
    }

    public static void recordReceiveFlightTailFlap() {
        receiveFlightTailFlapTimestamps.add(System.currentTimeMillis());
        averageFlightTailFlap = calculateAverageLatency(sendFlightTimestamps, receiveFlightTailFlapTimestamps,
                averageFlightTailFlap, flightTailFlapCount, flightTailFlapLatency);
    }

    public static void recordReceiveFlightCabinPressure() {
        receiveFlightCabinPressureTimestamps.add(System.currentTimeMillis());
        averageFlightCabinPressure = calculateAverageLatency(sendFlightTimestamps, receiveFlightCabinPressureTimestamps,
                averageFlightCabinPressure, flightCabinPressureCount, flightCabinPressureLatency);
    }

    public static void recordReceiveFlightEngine() {
        receiveFlightEngineTimestamps.add(System.currentTimeMillis());
        averageFlightEngine = calculateAverageLatency(sendFlightTimestamps, receiveFlightEngineTimestamps,
                averageFlightEngine, flightEngineCount, flightEngineLatency);
    }

    public static void recordReceiveFlightLandingGear() {
        receiveFlightLandingGearTimestamps.add(System.currentTimeMillis());
        averageFlightLandingGear = calculateAverageLatency(sendFlightTimestamps, receiveFlightLandingGearTimestamps,
                averageFlightLandingGear, flightLandingGearCount, flightLandingGearLatency);
    }

    public static void gui() {
        JFrame jFrame = new JFrame("Performance");
        jFrame.setSize(600, 400);
        JPanel mainPanel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEmptyBorder(25, 25, 25, 25);
        mainPanel.setBorder(border);
        JPanel labelPanel = new JPanel(new GridLayout(8, 4));
        JPanel titlePanel = new JPanel(new FlowLayout());
        JLabel title = new JLabel("Performance");
        title.setFont(new Font(GUI_FONT, Font.BOLD, 25));
        title.setForeground(Color.RED);
        titlePanel.add(title);

        mainPanel.add(titlePanel, BorderLayout.PAGE_START);
        mainPanel.add(labelPanel, BorderLayout.CENTER);
        jFrame.add(mainPanel);

        labels.add(new JLabel(""));
        labels.add(new JLabel("<html>Inter-Object<br>Latency"));
        labels.add(new JLabel(""));
        labels.add(new JLabel("<html>Flight-Object<br>Latency"));

        JLabel altitudeLabel = new JLabel("Wing Flaps-Altitude:");
        labels.add(altitudeLabel);
        labels.add(altitudeLatency);
        JLabel flightAltitudeLabel = new JLabel("Altitude:");
        labels.add(flightAltitudeLabel);
        labels.add(flightAltitudeLatency);

        JLabel gpsLabel = new JLabel("GPS-Tail Flaps:");
        labels.add(gpsLabel);
        labels.add(gpsLatency);
        JLabel flightGpsLabel = new JLabel("Gps:");
        labels.add(flightGpsLabel);
        labels.add(flightGpsLatency);

        JLabel engineLabel = new JLabel("Engine-Altitude:");
        labels.add(engineLabel);
        labels.add(engineLatency);
        JLabel flightEngineLabel = new JLabel("Engine:");
        labels.add(flightEngineLabel);
        labels.add(flightEngineLatency);

        JLabel cabinPressureLabel = new JLabel("<html>Cabin Pressure-<br>Oxygen Masks:");
        labels.add(cabinPressureLabel);
        labels.add(cabinPressureLatency);
        JLabel flightCabinPressureLabel = new JLabel("Cabin Pressure:");
        labels.add(flightCabinPressureLabel);
        labels.add(flightCabinPressureLatency);

        JLabel landingGearLabel = new JLabel("Altitude-Landing Gear:");
        labels.add(landingGearLabel);
        labels.add(landingGearLatency);
        JLabel flightLandingGearLabel = new JLabel("Landing Gear:");
        labels.add(flightLandingGearLabel);
        labels.add(flightLandingGearLatency);

        labels.add(new JLabel(""));
        labels.add(new JLabel(""));
        JLabel flightWingFlapLabel = new JLabel("Wing Flap:");
        labels.add(flightWingFlapLabel);
        labels.add(flightWingFlapLatency);

        labels.add(new JLabel(""));
        labels.add(new JLabel(""));
        JLabel flightTailFlapLabel = new JLabel("Tail Flap:");
        labels.add(flightTailFlapLabel);
        labels.add(flightTailFlapLatency);

        for (JLabel label : labels) {
            labelPanel.add(label);
        }

        jFrame.setVisible(true);
    }

}
