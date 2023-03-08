package org.flightcontrol.sensor.engine;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.*;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_NAME;

public class Engine extends TimerTask {

    public static final String ENGINE_ID = "Engine";
    public static final String PERCENTAGE_UNIT = "%";
    public static final String ENGINE_EXCHANGE_NAME = "EngineExchange";
    public static final int TAKEOFF_LANDING_PERCENTAGE = 50;
    public static final String ENGINE_EXCHANGE_KEY = "EngineKey";
    static final Integer MAX_FLUCTUATION = 2;
    static final Integer INCREMENT_TAKEOFF_LANDING = 5;
    static final Integer CRUISING_PERCENTAGE = 100;

    Integer currentPercentage;
    EngineState engineState;
    LinkedList<Observer> observers = new LinkedList<>();
    Timer timer = new Timer();
    Boolean isAltitudeZero;

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
        if (message.equals(LANDING_FLAG)) {
            isAltitudeZero = true;
        }
    };


    @Override
    public void run() {
        engineState.generateRpm();
    }


    public Engine() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();
    }

    protected void setCurrentPercentage(Integer currentPercentage) {
        this.currentPercentage = currentPercentage;

        String currentPercentageString = currentPercentage.toString() + PERCENTAGE_UNIT;
        System.out.println("Engine: " + currentPercentageString);
        for(Observer observer : observers) {
            observer.update(ENGINE_ID, currentPercentageString);
        }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void sendTakeoffFlagToAltitude() {
        try {
            channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(ALTITUDE_EXCHANGE_NAME, ENGINE_EXCHANGE_KEY, null, TAKEOFF_FLAG.getBytes());
        } catch (IOException ignored) {}
    }

    private void listenForLandedFlagFromAltitude() {
        try {
            channel.exchangeDeclare(ENGINE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ENGINE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, altitudeCallback, consumerTag -> {});
        } catch (IOException ignored) {}
    }

    private void receiveFlightPhase(String flightPhase) {
        switch(flightPhase) {
            case FLIGHT_PHASE_PARKED -> {
                isAltitudeZero = true;
                setCurrentPercentage(0);
            }
            case FLIGHT_PHASE_TAKEOFF -> {
                engineState = new EngineTakeoffState(this);
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_CRUISING -> {
                engineState = new EngineCruisingState(this);
            }
            case FLIGHT_PHASE_LANDING -> {
                engineState = new EngineLandingState(this);
                listenForLandedFlagFromAltitude();
            }
            case FLIGHT_PHASE_LANDED -> {
                timer.cancel();
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    protected void sendLandedFlagToFlight() {
        try {
            channel.exchangeDeclare(ENGINE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(ENGINE_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY, null, LANDED_FLAG.getBytes());
        } catch (IOException ignored) {}
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
}
