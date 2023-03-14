package org.flightcontrol.actuator.wingflap;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;
import org.flightcontrol.Performance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import static org.flightcontrol.flight.Flight.*;
import static org.flightcontrol.sensor.altitude.Altitude.*;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.*;

enum WingFlapDirection {UP, DOWN, NEUTRAL}

public class WingFlap implements Runnable {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> scheduledFuture;

    public static final String WING_FLAP_EXCHANGE_NAME = "WingFlapExchange";
    public static final String WING_FLAP_EXCHANGE_KEY = "WingFlapKey";

    // Plane attempts to fly 10500-11500
    public static final String WING_FLAP_ID = "WingFlap";
    static final Integer MAX_FLUCTUATION_UP_DOWN = 25;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 1500;
    static final Integer INCREMENT_VALUE_UP_DOWN = 100;

    Integer currentAltitude;
    WingFlapState wingFlapState;
    WingFlapDirection wingFlapDirection;
    Timer timer = new Timer();
    LinkedList<Observer> observers = new LinkedList<>();
    Integer targetAltitude = CRUISING_ALTITUDE;

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    /*
     * Callback to be used by Rabbit MQ receive
     */
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        setCurrentAltitude(Integer.valueOf(message));
    };

    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        Performance.recordReceiveFlightWingFlap();
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    DeliverCallback cabinPressureCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        if (message.equals(TOGGLE_PRESSURE_FLAG)) {
            if (targetAltitude.equals(CRUISING_ALTITUDE)) {
                targetAltitude = BREACHED_PRESSURE_ALTITUDE;
                wingFlapState = new WingFlapUpState(this);
            } else {
                targetAltitude = CRUISING_ALTITUDE;
                wingFlapState = new WingFlapDownState(this);
            }
        }
    };

    public WingFlap() {

        // Create channels for Rabbit MQ
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        listenForFlight();
    }

    @Override
    public void run() {
        wingFlapState.controlFlaps();
    }

    private void listenForAltitude() {
        try {
            channel.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ALTITUDE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (IOException ignored) { }
    }

    private void listenForCabinPressure() {
        try {
            channel.exchangeDeclare(CABIN_PRESSURE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, CABIN_PRESSURE_EXCHANGE_NAME, CABIN_PRESSURE_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, cabinPressureCallback, consumerTag -> {
            });
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

    public void setCurrentAltitude(Integer currentAltitude) {
        this.currentAltitude = currentAltitude;
    }

    protected void sendNewAltitude(Integer newAltitude) {
        try {
            channel.exchangeDeclare(WING_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = newAltitude.toString();
            channel.basicPublish(WING_FLAP_EXCHANGE_NAME, WING_FLAP_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED -> setWingFlapDirection(WingFlapDirection.NEUTRAL);
            case FLIGHT_PHASE_TAKEOFF -> setWingFlapDirection(WingFlapDirection.DOWN);
            case FLIGHT_PHASE_CRUISING -> {
                listenForCabinPressure();
                listenForAltitude();
                wingFlapState = new WingFlapNeutralState(this);
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this, 0L, TICK_RATE, TimeUnit.MILLISECONDS);
            }
            case FLIGHT_PHASE_LANDING -> {
//                timer.cancel();
                scheduledFuture.cancel(false);
                setWingFlapDirection(WingFlapDirection.UP);
            }
            case FLIGHT_PHASE_LANDED -> {
                setWingFlapDirection(WingFlapDirection.NEUTRAL);
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

    public void setWingFlapDirection(WingFlapDirection newWingFlapDirection) {
        this.wingFlapDirection = newWingFlapDirection;
        System.out.println("WingFlap: " + wingFlapDirection.toString());
        for (Observer observer : observers) {
            observer.update(WING_FLAP_ID, wingFlapDirection.toString());
        }
    }
}
