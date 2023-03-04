package org.flightcontrol.sensor.altitude;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.*;

public class Altitude extends TimerTask {

    public static final String ALTITUDE_ID = "Altitude";
    public static final String CRUISING_FLAG = "Cruising";
    static final Integer INCREMENT_TAKEOFF_LANDING = 500;
    static final Integer MAX_FLUCTUATION_TAKEOFF_LANDING = 100;
    public static final Integer ALTITUDE_ACCEPTED_DIFFERENCE = 500;
    public static final Integer CRUISING_ALTITUDE = 11000;

    public static final String ALTITUDE_EXCHANGE_NAME = "AltitudeExchange";
    public static final String ALTITUDE_EXCHANGE_KEY = "AltitudeKey";

    Integer currentAltitude = 0;
    AltitudeState altitudeState;
    LinkedList<Observer> observers = new LinkedList<>();;
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channelFlight;
    Channel channelAltitude;
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    public Altitude() {

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelFlight = connection.createChannel();
            channelAltitude = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();
    }

    @Override
    public void run() {
        altitudeState.generateAltitude();
    }

    public void changeState(AltitudeState newAltitudeState) {
        this.altitudeState = newAltitudeState;
        sendNewState();
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_TAKEOFF -> {
                changeState(new TakeoffState(this));
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_LANDING -> {
                changeState(new LandingState(this));
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }


    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void setCurrentAltitude(Integer currentAltitude) {
        this.currentAltitude = currentAltitude;
        for (Observer observer : observers) {
            observer.update(ALTITUDE_ID, currentAltitude.toString());
        }
    }

    private void sendNewState() {
        if (altitudeState.getClass().equals(CruisingState.class)) {
            try {
                channelAltitude.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
                channelAltitude.basicPublish(ALTITUDE_EXCHANGE_NAME, PHASE_EXCHANGE_KEY, null, CRUISING_FLAG.getBytes());
            } catch (IOException ignored) {
            }
        }
    }

    private void listenForFlight() {
        try {
            channelFlight.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelFlight.queueDeclare().getQueue();
            channelFlight.queueBind(queueName, FLIGHT_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY);
            channelFlight.basicConsume(queueName, true, flightCallback, consumerTag -> {
            });
        } catch (IOException ignored) {}
    }

}
