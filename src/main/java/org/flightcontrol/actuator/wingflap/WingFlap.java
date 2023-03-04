package org.flightcontrol.actuator.wingflap;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.*;
import static org.flightcontrol.sensor.altitude.Altitude.*;

enum WingFlapDirection {UP, DOWN, NEUTRAL}

public class WingFlap extends TimerTask {

    public static final String WING_FLAP_EXCHANGE_NAME = "WingFlapExchange";
    public static final String WING_FLAP_EXCHANGE_KEY = "WingFlapKey";

    // Plane attempts to fly 10500-11500
    public static final String WING_FLAP_ID = "WingFlap";
    static final Integer MAX_FLUCTUATION_UP_DOWN = 10;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 750;
    static final Integer INCREMENT_VALUE_UP_DOWN = 30;

    Integer currentAltitude = CRUISING_ALTITUDE;
    WingFlapState wingFlapState;
    WingFlapDirection wingFlapDirection;
    Timer timer = new Timer();
    LinkedList<Observer> observers = new LinkedList<>();

    // RabbitMQ variables
    Connection connection;
    Channel channelReceive;
    Channel channelSend;
    Channel channelFlight;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        currentAltitude = Integer.valueOf(message);
    };
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    public WingFlap() {

        // Create channels for Rabbit MQ
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelReceive = connection.createChannel();
            channelSend = connection.createChannel();
            channelFlight = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        listenForFlight();
    }

    @Override
    public void run() {
        wingFlapState.controlFlaps();
    }

    private void listenForAltitude() {
        try {
            channelReceive.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, ALTITUDE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY);
            channelReceive.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (IOException ignored) { }
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
    protected void sendNewAltitude(Integer newAltitude) {
        try {
            channelSend.exchangeDeclare(WING_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = newAltitude.toString();
            channelSend.basicPublish(WING_FLAP_EXCHANGE_NAME, WING_FLAP_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED -> setDirection(WingFlapDirection.NEUTRAL);
            case FLIGHT_PHASE_TAKEOFF -> setDirection(WingFlapDirection.DOWN);
            case FLIGHT_PHASE_CRUISING -> {
                listenForAltitude();
                wingFlapState = new WingFlapNeutralState(this);
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_LANDING -> {
                timer.cancel();
                setDirection(WingFlapDirection.UP);
            }
            case FLIGHT_PHASE_LANDED -> {
                setDirection(WingFlapDirection.NEUTRAL);
                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void setDirection(WingFlapDirection wingFlapDirection) {
        this.wingFlapDirection = wingFlapDirection;
        System.out.println("WingFlap: " + wingFlapDirection.toString());
        for (Observer observer : observers) {
            observer.update(WING_FLAP_ID, wingFlapDirection.toString());
        }
    }
}
