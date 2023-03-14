package org.flightcontrol.actuator.tailflap;

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
import static org.flightcontrol.sensor.gps.GPS.GPS_EXCHANGE_KEY;
import static org.flightcontrol.sensor.gps.GPS.GPS_EXCHANGE_NAME;

enum TailFlapDirection {LEFT, RIGHT, NEUTRAL}

public class TailFlap implements Runnable {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> scheduledFuture;

    public static final String TAIL_FLAP_ID = "TailFlapNeutralState";
    public static final String TAIL_FLAP_EXCHANGE_NAME = "TailFlapExchange";
    public static final String TAIL_FLAP_EXCHANGE_KEY = "TailFlapKey";

    static final Integer MAX_FLUCTUATION_LEFT_RIGHT = 2;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 20;
    static final Integer FLUCTUATION_OFF_COURSE = 5;
    static final Integer INCREMENT_VALUE_LEFT_RIGHT = 5;

    Timer timer = new Timer();
    LinkedList<Observer> observers = new LinkedList<>();
    Integer currentBearing;
    TailFlapDirection tailFlapDirection;
    TailFlapState tailFlapState;
    Boolean onCourse = false; // Used initially during cruising phase
    Boolean isTakingOffOrLanding = false; // Used to prevent changing direction when taking off or landing

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        currentBearing = Integer.valueOf(message);
    };
    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        Performance.recordReceiveFlightTailFlap();
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    public TailFlap() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();
    }

    @Override
    public void run() {
        tailFlapState.controlFlaps();
    }

    public void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED -> {
                setTailFlapDirection(TailFlapDirection.NEUTRAL);
                listenForGPS();
            }
            case FLIGHT_PHASE_TAKEOFF -> {
                isTakingOffOrLanding = true;
                tailFlapState = new TailFlapNeutralState(this);
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this, 0L, TICK_RATE, TimeUnit.MILLISECONDS);
            }
            case FLIGHT_PHASE_CRUISING -> isTakingOffOrLanding = false;
            case FLIGHT_PHASE_LANDING -> {
                isTakingOffOrLanding = true;
                tailFlapState = new TailFlapNeutralState(this);
            }
            case FLIGHT_PHASE_LANDED -> {
//                timer.cancel();
                scheduledFuture.cancel(false);
                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void setTailFlapDirection(TailFlapDirection newTailFlapDirection) {
        this.tailFlapDirection = newTailFlapDirection;
        System.out.println("TailFlap: " + tailFlapDirection.toString());
        for (Observer observer : observers) {
            observer.update(TAIL_FLAP_ID, tailFlapDirection.toString());
        }
    }

    private void listenForGPS() {
        try {
            channel.exchangeDeclare(GPS_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, GPS_EXCHANGE_NAME, GPS_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
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

    protected void sendNewBearing(Integer newDirection) {
        try {
            channel.exchangeDeclare(TAIL_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = newDirection.toString();
            channel.basicPublish(TAIL_FLAP_EXCHANGE_NAME, TAIL_FLAP_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }
}
