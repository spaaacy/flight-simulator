package org.flightcontrol.actuator.landinggear;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.*;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_NAME;

enum LandingGearStatus {DEPLOYED, STOWED}

public class LandingGear {

    public static final String LANDING_GEAR_ID = "LandingGear";
    private static final Integer LANDING_GEAR_ALTITUDE = 5000;

    LandingGearStatus landingGearStatus;
    LinkedList<Observer> observers = new LinkedList<>();

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    /*
     * Callback to be used by Rabbit MQ receive
     */
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    DeliverCallback altitudeCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveAltitude(Integer.valueOf(message));
    };

    private void receiveAltitude(Integer newAltitude) {
        if (newAltitude < LANDING_GEAR_ALTITUDE) {
            setLandingGearStatus(LandingGearStatus.DEPLOYED);
        } else {
            setLandingGearStatus(LandingGearStatus.STOWED);
        }
    }


    public LandingGear() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();
        listenForAltitude();
    }



    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED ->
                setLandingGearStatus(LandingGearStatus.DEPLOYED);
            case FLIGHT_PHASE_LANDED -> {
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void listenForFlight() {
        try {
            channel.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, FLIGHT_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, flightCallback, consumerTag -> {
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

    private void listenForAltitude() {
        try {
            channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ALTITUDE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, altitudeCallback, consumerTag -> {});
        } catch (IOException ignored) { }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }
}
