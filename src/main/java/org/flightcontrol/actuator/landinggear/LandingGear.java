package org.flightcontrol.actuator.landinggear;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.*;

enum LandingGearStatus {DEPLOYED, STOWED}

public class LandingGear {

    public static final String LANDING_GEAR_ID = "LandingGear";

    LandingGearStatus landingGearStatus;
    LinkedList<Observer> observers = new LinkedList<>();

    // RabbitMQ variables
    Connection connection;
    Channel channelReceive;
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };


    public LandingGear() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelReceive = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED, FLIGHT_PHASE_CRUISING ->
                setLandingGearStatus(LandingGearStatus.STOWED);
            case FLIGHT_PHASE_TAKEOFF, FLIGHT_PHASE_LANDING ->
                setLandingGearStatus(LandingGearStatus.DEPLOYED);
            case FLIGHT_PHASE_LANDED -> {
                setLandingGearStatus(LandingGearStatus.DEPLOYED);
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void listenForFlight() {
        try {
            channelReceive.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, FLIGHT_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY);
            channelReceive.basicConsume(queueName, true, flightCallback, consumerTag -> {
            });
        } catch (IOException ignored) {}
    }

    public void setLandingGearStatus(LandingGearStatus landingGearStatus) {
        this.landingGearStatus = landingGearStatus;
        System.out.println("Landing Gear: " + landingGearStatus.toString());

        for (Observer observer : observers) {
            observer.update(LANDING_GEAR_ID, landingGearStatus.toString());
        }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }
}
