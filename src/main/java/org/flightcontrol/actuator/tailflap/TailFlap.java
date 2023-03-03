package org.flightcontrol.actuator.tailflap;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.gps.GPS.*;

enum TailFlapDirection {LEFT, RIGHT, NEUTRAL};

public class TailFlap extends TimerTask implements Observer {

    public static final String TAIL_FLAP_EXCHANGE_NAME = "TailFlapExchange";
    public static final String TAIL_FLAP_EXCHANGE_KEY = "TailFlapKey";

    public static final int BEARING_DESTINATION = 290;
    static final Integer MAX_FLUCTUATION_LEFT_RIGHT = 2;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 20;
    static final Integer INCREMENT_VALUE_LEFT_RIGHT = 4;
    static final Integer ACCEPTED_RANGE = 10;

    Phaser phaser;
    Timer timer = new Timer();
    Integer currentBearing = STARTING_BEARING;
    TailFlapDirection tailFlapDirection;
    TailFlapState tailFlapState;
    Boolean onCourse = false;

    // RabbitMQ variables
    Connection connection;
    Channel channelSend;
    Channel channelReceive;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        currentBearing = Integer.valueOf(message);
    };

    public TailFlap(Phaser phaser) {
        this.phaser = phaser;

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelSend = connection.createChannel();
            channelReceive = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

    }

    @Override
    public void run() {
        tailFlapState.controlFlaps();
    }

    @Override
    public void update() {
        switch (phaser.getPhase()) {
            case 1 -> setTailFlapDirection(TailFlapDirection.NEUTRAL);
            case 2 -> {
                listenForDirection();
                tailFlapState = new TailFlapNeutralState(this);
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case 3 -> {
                timer.cancel();
                setTailFlapDirection(TailFlapDirection.NEUTRAL);
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public void setTailFlapDirection(TailFlapDirection tailFlapDirection) {
        this.tailFlapDirection = tailFlapDirection;
//        System.out.println("TailFlap: " + tailFlapDirection.toString());
    }

    private void listenForDirection() {
        try {
            channelReceive.exchangeDeclare(GPS_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, GPS_EXCHANGE_NAME, GPS_EXCHANGE_KEY);
            channelReceive.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (IOException ignored) {}
    }

    protected void sendNewBearing(Integer newDirection) {
        try {
            channelSend.exchangeDeclare(TAIL_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = newDirection.toString();
            channelSend.basicPublish(TAIL_FLAP_EXCHANGE_NAME, TAIL_FLAP_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }
}
