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

public class Engine extends TimerTask {

    public static final String ENGINE_ID = "Engine";
    public static final String PERCENTAGE_UNIT = "%";
    public static final String ENGINE_EXCHANGE_NAME = "EngineExchange";
    static final Integer MAX_FLUCTUATION = 3;
    static final Integer INCREMENT_TAKEOFF_LANDING = 9;
    static final Integer CRUISING_PERCENTAGE = 100;
    public static final String LANDED_FLAG = "LandedFlag";

    Integer currentPercentage;
    EngineState engineState;
    LinkedList<Observer> observers = new LinkedList<>();
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
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

    // TODO: sendCurrentPercentage()

    private void receiveFlightPhase(String flightPhase) {
        switch(flightPhase) {
            case FLIGHT_PHASE_PARKED -> {
                setCurrentPercentage(0);
            }
            case FLIGHT_PHASE_TAKEOFF -> {
                engineState = new EngineTakeoffState(this);
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_CRUISING ->
                engineState = new EngineCruisingState(this);
            case FLIGHT_PHASE_LANDING ->
                engineState = new EngineLandingState(this);
            case FLIGHT_PHASE_LANDED -> {
                timer.cancel();
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    protected void sendLandedFlag() {
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
