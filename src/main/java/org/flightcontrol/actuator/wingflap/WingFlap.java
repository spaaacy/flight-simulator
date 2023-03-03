package org.flightcontrol.actuator.wingflap;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_NAME;

enum WingFlapDirection {UP, DOWN, NEUTRAL}

public class WingFlap extends TimerTask implements Observer {

    public static final String WING_FLAP_EXCHANGE_NAME = "WingFlapExchange";
    public static final String WING_FLAP_EXCHANGE_KEY = "WingFlapKey";

    // Plane attempts to fly 10500-11500
    public static final Integer CRUISING_ALTITUDE = 11000;
    static final Integer MAX_FLUCTUATION_UP_DOWN = 10;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 750;
    static final Integer INCREMENT_VALUE_UP_DOWN = 30;
    static final Integer ACCEPTED_DIFFERENCE = 500;

    Integer currentAltitude = CRUISING_ALTITUDE;
    Phaser phaser;
    WingFlapState wingFlapState;
    WingFlapDirection wingFlapDirection;
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channelReceive;
    Channel channelSend;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        currentAltitude = Integer.valueOf(message);
    };



    public WingFlap(Phaser phaser) {
        this.phaser = phaser;

        // Create channels for Rabbit MQ
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelReceive = connection.createChannel();
            channelSend = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }



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
    public void update(String... updatedValue) {
        switch (phaser.getPhase()) {
            case 1 -> setDirection(WingFlapDirection.DOWN);
            case 2 -> {
                listenForAltitude();
                wingFlapState = new WingFlapNeutralState(this);
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case 3 -> {
                timer.cancel();
                setDirection(WingFlapDirection.UP);
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public void setDirection(WingFlapDirection wingFlapDirection) {
        this.wingFlapDirection = wingFlapDirection;
        System.out.println("WingFlap: " + wingFlapDirection.toString());
    }
}
