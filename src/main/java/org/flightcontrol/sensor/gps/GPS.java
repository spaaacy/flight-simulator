package org.flightcontrol.sensor.gps;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.actuator.tailflap.TailFlap.*;
import static org.flightcontrol.flight.Flight.*;

public class GPS extends TimerTask {

    // Directions are in degrees (0-360)
    static final String DEGREE_SYMBOL = "°";
    public static final Integer STARTING_BEARING = 180;
    public static final Integer GPS_ACCEPTED_DIFFERENCE = 10;
    public static final int BEARING_DESTINATION = 290;

    public static final String GPS_ID = "GPS";
    public static final String GPS_EXCHANGE_NAME = "GPSExchange";
    public static final String GPS_EXCHANGE_KEY = "GPSKey";

    Integer currentBearing;
    Timer timer = new Timer();
    LinkedList<Observer> observers = new LinkedList<>();

    // RabbitMQ variables
    Connection connection;
    Channel channelSend;
    Channel channelReceive;
    Channel channelFlight;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        System.out.println("RECEIVED");
        setCurrentBearing(Integer.valueOf(message));
    };
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    @Override
    public void run() {
        sendCurrentDirection();
        System.out.println("GPS: " + currentBearing + DEGREE_SYMBOL);
    }

    public GPS() {

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelSend = connection.createChannel();
            channelReceive = connection.createChannel();
            channelFlight = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        listenForFlight();
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_TAKEOFF -> {
                setCurrentBearing(STARTING_BEARING);
                System.out.println("GPS: " + currentBearing + DEGREE_SYMBOL);
                System.out.println("GPS: Destination is at a bearing of " + BEARING_DESTINATION + DEGREE_SYMBOL);
            }
            case FLIGHT_PHASE_CRUISING -> {
                listenForTailFlap();
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_LANDING -> {
                timer.cancel();
                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void listenForTailFlap() {
        try {
            channelReceive.exchangeDeclare(TAIL_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, TAIL_FLAP_EXCHANGE_NAME, TAIL_FLAP_EXCHANGE_KEY);
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

    private void sendCurrentDirection() {
        try {
            channelSend.exchangeDeclare(GPS_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = currentBearing.toString();
            channelSend.basicPublish(GPS_EXCHANGE_NAME, GPS_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void setCurrentBearing(Integer currentBearing) {
        this.currentBearing = currentBearing;
        for (Observer observer : observers) {
            observer.update(GPS_ID, currentBearing.toString() + DEGREE_SYMBOL);
        }
    }
}
