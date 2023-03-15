package org.flightcontrol.sensor.altitude;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;
import org.flightcontrol.Performance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_KEY;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_NAME;
import static org.flightcontrol.flight.Flight.*;
import static org.flightcontrol.sensor.engine.Engine.ENGINE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.engine.Engine.ENGINE_EXCHANGE_NAME;

public class Altitude implements Runnable {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> scheduledFuture;

    public static final String ALTITUDE_ID = "Altitude";
    static final Integer INCREMENT_TAKEOFF_LANDING = 500;
    static final Integer MAX_FLUCTUATION_TAKEOFF_LANDING = 100;
    public static final Integer ALTITUDE_ACCEPTED_DIFFERENCE = 1000;
    public static final Integer CRUISING_ALTITUDE = 11000;
    public static final Integer BREACHED_PRESSURE_ALTITUDE = 8000;

    public static final String ALTITUDE_EXCHANGE_NAME = "AltitudeExchange";
    public static final String ALTITUDE_EXCHANGE_KEY = "AltitudeKey";
    public static final String HEIGHT_UNIT = " m";

    Boolean isCruising = false;
    Integer currentAltitude;
    AltitudeState altitudeState;
    LinkedList<Observer> observers = new LinkedList<>();
    Timer timer = new Timer();
    Boolean isEngineReady;

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        Performance.recordReceiveFlightAltitude();
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        Performance.recordReceiveAltitude();
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        setCurrentAltitude(Integer.valueOf(message));
    };

    DeliverCallback engineCallback = (consumerTag, delivery) -> {
        Performance.recordReceiveEngine();
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        if (message.equals(TAKEOFF_FLAG)) {
            isEngineReady = true;
        }
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
        if (!isCruising) {
            altitudeState.generateAltitude();
        }
    }

    private void sendCurrentAltitude() {
        Performance.recordSendLandingGear();
        try {
            channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = currentAltitude.toString();
            channel.basicPublish(ALTITUDE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) { }
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED -> {
                isEngineReady = false;
                setCurrentAltitude(0);
            }
            case FLIGHT_PHASE_TAKEOFF -> {
                listenForTakeoffFlagFromEngine();
                altitudeState = new AltitudeTakeoffState(this);
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this, 0L, TICK_RATE, TimeUnit.MILLISECONDS);
            }
            case FLIGHT_PHASE_CRUISING -> {
                isCruising = true;
                listenForWingFlap();
            }
            case FLIGHT_PHASE_LANDING -> {
                isCruising = false;
                altitudeState = new AltitudeLandingState(this);
            }
            case FLIGHT_PHASE_LANDED -> {
                scheduledFuture.cancel(false);
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

    public void sendLandedFlagToEngine() {
        Performance.recordSendEngine();
        try {
            channel.exchangeDeclare(ENGINE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(ENGINE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY, null, LANDING_FLAG.getBytes());
        } catch (IOException ignored) {}
    }

    private void listenForTakeoffFlagFromEngine() {
        try {
            channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ALTITUDE_EXCHANGE_NAME, ENGINE_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, engineCallback, consumerTag -> {
            });
        } catch (IOException ignored) {}
    }

    protected void sendCruisingFlagToFlight() {
            try {
                channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
                channel.basicPublish(ALTITUDE_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY, null, CRUISING_FLAG.getBytes());
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
