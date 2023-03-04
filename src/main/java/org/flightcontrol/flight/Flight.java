package org.flightcontrol.flight;

import com.rabbitmq.client.*;
import org.flightcontrol.ControlSystem;
import org.flightcontrol.Observer;
import org.flightcontrol.actuator.landinggear.LandingGear;
import org.flightcontrol.actuator.tailflap.TailFlap;
import org.flightcontrol.actuator.wingflap.WingFlap;
import org.flightcontrol.sensor.altitude.Altitude;
import org.flightcontrol.sensor.gps.GPS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.sensor.altitude.Altitude.*;

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
    public static final String PHASE_EXCHANGE_KEY = "PhaseKey";
    public static final Long TICK_RATE = 1000L;

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
    ControlSystem controlSystem;
    String flightPhase = FLIGHT_PHASE_PARKED;

    // Sensors
    Altitude altitude = new Altitude();
    GPS gps = new GPS();

    // Actuators
    WingFlap wingFlap = new WingFlap();
    TailFlap tailFlap = new TailFlap();
    LandingGear landingGear = new LandingGear();


    public Flight(ControlSystem controlSystem) {
        this.controlSystem = controlSystem;

        // Objects observed by control system for GUI
        addObserver(controlSystem);
        altitude.addObserver(controlSystem);
        wingFlap.addObserver(controlSystem);
        gps.addObserver(controlSystem);
        tailFlap.addObserver(controlSystem);
        landingGear.addObserver(controlSystem);

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
            channelAltitude.queueBind(queueName, ALTITUDE_EXCHANGE_NAME, PHASE_EXCHANGE_KEY);
            channelAltitude.basicConsume(queueName, true, altitudeCallback, consumerTag -> {});
        } catch (IOException ignored) { }
    }
    public Altitude getAltitude() {
        return altitude;
    }

    private void addObserver(Observer observer) {
        observers.add(observer);
    }


}
