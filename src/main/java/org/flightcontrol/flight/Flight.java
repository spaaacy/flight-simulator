package org.flightcontrol.flight;

import com.rabbitmq.client.*;
import org.flightcontrol.ControlSystem;
import org.flightcontrol.Observer;
import org.flightcontrol.actuator.landinggear.LandingGear;
import org.flightcontrol.actuator.tailflap.TailFlap;
import org.flightcontrol.actuator.wingflap.WingFlap;
import org.flightcontrol.sensor.altitude.Altitude;
import org.flightcontrol.sensor.cabinpressure.CabinPressure;
import org.flightcontrol.sensor.gps.GPS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.sensor.altitude.Altitude.*;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.CABIN_PRESSURE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.TOGGLE_PRESSURE_FLAG;

public class Flight implements Runnable {

    // Flight phases
    public static final String FLIGHT_PHASE_PARKED = "PARKED";
    public static final String FLIGHT_PHASE_TAKEOFF = "TAKEOFF";
    public static final String FLIGHT_PHASE_CRUISING = "CRUISING";
    public static final String FLIGHT_PHASE_LANDING = "LANDING";
    public static final String FLIGHT_PHASE_LANDED = "LANDED";

    public static final String FLIGHT_ID = "Flight";
    public static final String FLIGHT_EXCHANGE_NAME = "FlightExchange";
    public static final String FLIGHT_EXCHANGE_KEY = "FlightKey";
    public static final Long TICK_RATE = 500L; // Used to control execution speed

    // RabbitMQ variables
    Connection connection;
    Channel channelFlight;
    Channel channelAltitude;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback altitudeCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        switch (message) {
            case CRUISING_FLAG -> setFlightPhase(FLIGHT_PHASE_CRUISING);
            case LANDED_FLAG -> {
                setFlightPhase(FLIGHT_PHASE_LANDED);
                connection.close();
            }
        }
    };

    LinkedList<Observer> observers = new LinkedList<>();
    String flightPhase = FLIGHT_PHASE_PARKED;

    // Sensors
    Altitude altitude = new Altitude();
    GPS gps = new GPS();
    CabinPressure cabinPressure = new CabinPressure();

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

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelFlight = connection.createChannel();
            channelAltitude = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}
    }

    @Override
    public void run() {
        listenForAltitude();
        setFlightPhase(FLIGHT_PHASE_PARKED);
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
                channelFlight.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
                channelFlight.basicPublish(FLIGHT_EXCHANGE_NAME, CABIN_PRESSURE_EXCHANGE_KEY, null, TOGGLE_PRESSURE_FLAG.getBytes());
            } catch (IOException ignored) {
            }
        }
    }

    private void sendNewFlightPhase(String newPhase) {
        try {
            channelFlight.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channelFlight.basicPublish(FLIGHT_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY, null, newPhase.getBytes());
        } catch(IOException ignored) {}
    }

    private void listenForAltitude() {
        try {
            channelAltitude.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelAltitude.queueDeclare().getQueue();
            channelAltitude.queueBind(queueName, ALTITUDE_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY);
            channelAltitude.basicConsume(queueName, true, altitudeCallback, consumerTag -> {});
        } catch (IOException ignored) { }
    }
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public Altitude getAltitude() {
        return altitude;
    }

    public GPS getGps() {
        return gps;
    }

    public WingFlap getWingFlap() {
        return wingFlap;
    }

    public TailFlap getTailFlap() {
        return tailFlap;
    }

    public LandingGear getLandingGear() {
        return landingGear;
    }

    public CabinPressure getCabinPressure() {
        return cabinPressure;
    }
}
