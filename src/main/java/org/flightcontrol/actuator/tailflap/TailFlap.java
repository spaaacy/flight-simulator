package org.flightcontrol.actuator.tailflap;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.*;
import static org.flightcontrol.sensor.gps.GPS.*;

enum TailFlapDirection {LEFT, RIGHT, NEUTRAL};

public class TailFlap extends TimerTask {

    public static final String TAIL_FLAP_ID = "TailFlapNeutralState";
    public static final String TAIL_FLAP_EXCHANGE_NAME = "TailFlapExchange";
    public static final String TAIL_FLAP_EXCHANGE_KEY = "TailFlapKey";

    static final Integer MAX_FLUCTUATION_LEFT_RIGHT = 2;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 20;
    static final Integer INCREMENT_VALUE_LEFT_RIGHT = 4;

    Timer timer = new Timer();
    LinkedList<Observer> observers = new LinkedList<>();
    Integer currentBearing = STARTING_BEARING;
    TailFlapDirection tailFlapDirection;
    TailFlapState tailFlapState;
    Boolean onCourse = false; // Used initially during cruising phase

    // RabbitMQ variables
    Connection connection;
    Channel channelSend;
    Channel channelReceive;
    Channel channelFlight;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        currentBearing = Integer.valueOf(message);
    };
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    public TailFlap() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelSend = connection.createChannel();
            channelReceive = connection.createChannel();
            channelFlight = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();

    }

    @Override
    public void run() {
        tailFlapState.controlFlaps();
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED ->
                    setTailFlapDirection(TailFlapDirection.NEUTRAL);
            case FLIGHT_PHASE_CRUISING -> {
                listenForGPS();
                tailFlapState = new TailFlapNeutralState(this);
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_LANDING -> {
                timer.cancel();
                setTailFlapDirection(TailFlapDirection.NEUTRAL);
            }
            case FLIGHT_PHASE_LANDED -> {
                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void setTailFlapDirection(TailFlapDirection tailFlapDirection) {
        this.tailFlapDirection = tailFlapDirection;
        System.out.println("TailFlap: " + tailFlapDirection.toString());
        for (Observer observer : observers) {
            observer.update(TAIL_FLAP_ID, tailFlapDirection.toString());
        }
    }

    private void listenForGPS() {
        try {
            channelReceive.exchangeDeclare(GPS_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, GPS_EXCHANGE_NAME, GPS_EXCHANGE_KEY);
            channelReceive.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (IOException ignored) {}
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

    protected void sendNewBearing(Integer newDirection) {
        try {
            channelSend.exchangeDeclare(TAIL_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = newDirection.toString();
            channelSend.basicPublish(TAIL_FLAP_EXCHANGE_NAME, TAIL_FLAP_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }
}
