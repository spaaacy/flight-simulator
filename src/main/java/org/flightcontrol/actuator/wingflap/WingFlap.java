package org.flightcontrol.actuator.wingflap;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;
import org.flightcontrol.sensor.altitude.Altitude;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_NAME;

enum Direction {UP, DOWN, NEUTRAL}

public class WingFlap extends TimerTask implements Observer {

    public static final String WING_FLAP_EXCHANGE_NAME = "WingFlapExchange";
    public static final String WING_FLAP_EXCHANGE_KEY = "WingFlapKey";

    Altitude altitude;
    Integer currentAltitude;
    Phaser phaser;
    WingFlapState wingFlapState;
    Direction direction;
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channelReceive;
    Channel channelSend;
    DeliverCallback deliverCallback;


    // Plane attempts to fly 10500-11500
    public static final Integer CRUISING_ALTITUDE = 11000;
    static final Integer MAX_FLUCTUATION_UP_DOWN = 5;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 500;
    static final Integer INCREMENT_VALUE_UP_DOWN = 15;
    static final Integer ACCEPTED_RANGE = 500;

    public WingFlap(Altitude altitude, Phaser phaser) {
        this.altitude = altitude;
        this.phaser = phaser;

        // Create channels for Rabbit MQ
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelReceive = connection.createChannel();
            channelSend = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        // Callback to be used by Rabbit MQ receive
        deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            currentAltitude = Integer.valueOf(  message);
        };

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

    protected void sendNewAltitude(Integer newAltitude) {
        try {
            channelSend.exchangeDeclare(WING_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = newAltitude.toString();
            channelSend.basicPublish(WING_FLAP_EXCHANGE_NAME, WING_FLAP_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

    @Override
    public void update() {
        switch (phaser.getPhase()) {
            case 1 -> direction = Direction.DOWN;
            case 2 -> {
                listenForAltitude();
                setWingFlapState(new WingFlapNeutralState(this, altitude));
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case 3 -> {
                timer.cancel();
                direction = Direction.UP;
            }
        }
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        System.out.println("WingFlap: " + direction.toString());
    }

    public void setWingFlapState(WingFlapState newWingFlapState) {
        if (newWingFlapState != null) {
            newWingFlapState.stopExecution();
        }

        this.wingFlapState = newWingFlapState;
        System.out.println("WingFlap: " + newWingFlapState.toString());
    }

}
