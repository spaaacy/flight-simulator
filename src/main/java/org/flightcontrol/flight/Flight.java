package org.flightcontrol.flight;

import com.rabbitmq.client.*;
import org.flightcontrol.ControlSystem;
import org.flightcontrol.Observer;
import org.flightcontrol.Performance;
import org.flightcontrol.actuator.landinggear.LandingGear;
import org.flightcontrol.actuator.oxygenmasks.OxygenMask;
import org.flightcontrol.actuator.tailflap.TailFlap;
import org.flightcontrol.actuator.wingflap.WingFlap;
import org.flightcontrol.sensor.altitude.Altitude;
import org.flightcontrol.sensor.cabinpressure.CabinPressure;
import org.flightcontrol.sensor.engine.Engine;
import org.flightcontrol.sensor.gps.GPS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_NAME;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.CABIN_PRESSURE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.TOGGLE_PRESSURE_FLAG;
import static org.flightcontrol.sensor.engine.Engine.ENGINE_EXCHANGE_NAME;

public class Flight {

    // Flight phases
    public static final String FLIGHT_PHASE_PARKED = "PARKED";
    public static final String FLIGHT_PHASE_TAKEOFF = "TAKEOFF";
    public static final String FLIGHT_PHASE_CRUISING = "CRUISING";
    public static final String FLIGHT_PHASE_LANDING = "LANDING";
    public static final String FLIGHT_PHASE_LANDED = "LANDED";

    public static final String TAKEOFF_FLAG = "TakeoffFlag";
    public static final String CRUISING_FLAG = "CruisingFlag";
    public static final String LANDING_FLAG = "LandingFlag";
    public static final String LANDED_FLAG = "LandedFlag";

    public static final String FLIGHT_ID = "Flight";
    public static final String FLIGHT_EXCHANGE_NAME = "FlightExchange";
    public static final String FLIGHT_EXCHANGE_KEY = "FlightKey";
    public static final Long TICK_RATE = 250L; // Used to control execution speed

    // Benchmarking purposes
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    Scanner scanner = new Scanner(System.in);


    // RabbitMQ variables
    Connection connection;
    Channel channel;

    /*
     * Callback to be used by Rabbit MQ receive
     */
    DeliverCallback engineCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        if (LANDED_FLAG.equals(message)) {
            setFlightPhase(FLIGHT_PHASE_LANDED);
            connection.close();
        }
//        System.exit(0);
    };

    DeliverCallback altitudeCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        if (CRUISING_FLAG.equals(message)) {
            setFlightPhase(FLIGHT_PHASE_CRUISING);

            // Benchmarking purposes
//            scanner.nextLine();
            scheduledExecutorService.schedule(this::toggleCabinPressure, 5L, TimeUnit.SECONDS);
            scheduledExecutorService.schedule(this::toggleCabinPressure, 10L, TimeUnit.SECONDS);
            scheduledExecutorService.schedule(this::initiateLanding, 15L, TimeUnit.SECONDS);

        }
    };

    LinkedList<Observer> observers = new LinkedList<>();
    String flightPhase = FLIGHT_PHASE_PARKED;

    // Sensors
    Altitude altitude = new Altitude();
    GPS gps = new GPS();
    CabinPressure cabinPressure = new CabinPressure();
    OxygenMask oxygenMask = new OxygenMask();
    Engine engine = new Engine();

    // Actuators
    WingFlap wingFlap = new WingFlap();
    TailFlap tailFlap = new TailFlap();
    LandingGear landingGear = new LandingGear();


    public Flight(ControlSystem controlSystem) {
        addObserver(controlSystem);
        altitude.addObserver(controlSystem);
        wingFlap.addObserver(controlSystem);
        gps.addObserver(controlSystem);
        tailFlap.addObserver(controlSystem);
        landingGear.addObserver(controlSystem);
        cabinPressure.addObserver(controlSystem);
        oxygenMask.addObserver(controlSystem);
        engine.addObserver(controlSystem);

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForCruisingFlagFromAltitude();
        listenForLandedFlagFromEngine();

        setFlightPhase(FLIGHT_PHASE_PARKED);

        // Benchmarking purposes
//        scanner.nextLine();
//        setFlightPhase(FLIGHT_PHASE_TAKEOFF);
        Runnable takeoff = () -> setFlightPhase(FLIGHT_PHASE_TAKEOFF);
        scheduledExecutorService.schedule(takeoff, 5L ,TimeUnit.SECONDS);

    }

    private void setFlightPhase(String flightPhase) {
        this.flightPhase = flightPhase;
        sendNewFlightPhase(this.flightPhase);
        System.out.println("Flight: " + flightPhase);

        for (Observer observer : observers) {
            observer.update(FLIGHT_ID, flightPhase);
        }
    }

    public void initiateLanding() {
        if (flightPhase.equals(FLIGHT_PHASE_CRUISING)) {
            setFlightPhase(FLIGHT_PHASE_LANDING);
        }
    }

    public void initiateTakeoff() {
        if (flightPhase.equals(FLIGHT_PHASE_PARKED)) {
            setFlightPhase(FLIGHT_PHASE_TAKEOFF);
        }
    }

    public void toggleCabinPressure() {
        if (flightPhase.equals(FLIGHT_PHASE_CRUISING)) {
            try {
                channel.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
                channel.basicPublish(FLIGHT_EXCHANGE_NAME, CABIN_PRESSURE_EXCHANGE_KEY, null, TOGGLE_PRESSURE_FLAG.getBytes());
            } catch (IOException ignored) {
            }
        }
    }

    private void sendNewFlightPhase(String newPhase) {
        Performance.recordSendFlight();
        try {
            channel.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(FLIGHT_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY, null, newPhase.getBytes());
        } catch(IOException ignored) {}
    }

    private void listenForCruisingFlagFromAltitude() {
        try {
            channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ALTITUDE_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, altitudeCallback, consumerTag -> {});
        } catch (IOException ignored) { }
    }

    private void listenForLandedFlagFromEngine() {
        try {
            channel.exchangeDeclare(ENGINE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ENGINE_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, engineCallback, consumerTag -> {
            });
        } catch (IOException ignored) {}
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }
}
