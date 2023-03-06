package org.flightcontrol.sensor.altitude;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_KEY;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_NAME;
import static org.flightcontrol.flight.Flight.*;

public class Altitude extends TimerTask {

    public static final String ALTITUDE_ID = "Altitude";
    public static final String CRUISING_FLAG = "CruisingFlag";
    public static final String LANDED_FLAG = "LandedFlag";
    static final Integer INCREMENT_TAKEOFF_LANDING = 500;
    static final Integer MAX_FLUCTUATION_TAKEOFF_LANDING = 100;
    public static final Integer ALTITUDE_ACCEPTED_DIFFERENCE = 500;
    public static final Integer CRUISING_ALTITUDE = 11000;
    public static final Integer BREACHED_PRESSURE_ALTITUDE = 8000;

    public static final String ALTITUDE_EXCHANGE_NAME = "AltitudeExchange";
    public static final String ALTITUDE_EXCHANGE_KEY = "AltitudeKey";
    private static final String HEIGHT_UNIT = " m";

    Integer currentAltitude;
    AltitudeState altitudeState;
    LinkedList<Observer> observers = new LinkedList<>();;
    Timer timerCruising = new Timer();
    Timer timerLanding = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        setCurrentAltitude(Integer.valueOf(message));
    };

    public Altitude() {

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();
    }

    @Override
    public void run() {
        altitudeState.generateAltitude();
    }

    private void sendCurrentAltitude() {
        try {
            channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = currentAltitude.toString();
            channel.basicPublish(ALTITUDE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) { }
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED ->
                setCurrentAltitude(0);
            case FLIGHT_PHASE_TAKEOFF -> {
                altitudeState = new TakeoffState(this);
                timerCruising.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_CRUISING -> {
                timerCruising.cancel();
                listenForWingFlap();
            }
            case FLIGHT_PHASE_LANDING -> {
                altitudeState = new LandingState(this);
                timerLanding.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_LANDED -> {
                timerLanding.cancel();

                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void listenForWingFlap() {
        try {
            channel.exchangeDeclare(WING_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, WING_FLAP_EXCHANGE_NAME, WING_FLAP_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (IOException ignored) { }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void setCurrentAltitude(Integer currentAltitude) {
        this.currentAltitude = currentAltitude;
        sendCurrentAltitude();

        String currentAltitudeString = currentAltitude.toString() + HEIGHT_UNIT;
        System.out.println("Altitude: " + currentAltitudeString);

        for (Observer observer : observers) {
            observer.update(ALTITUDE_ID, currentAltitudeString);
        }
    }

    protected void sendNewFlightPhase(String flag) {
            try {
                channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
                channel.basicPublish(ALTITUDE_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY, null, flag.getBytes());
            } catch (IOException ignored) {
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

}
