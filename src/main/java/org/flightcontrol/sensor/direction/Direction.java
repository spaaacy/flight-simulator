package org.flightcontrol.sensor.direction;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.actuator.tailflap.TailFlap.TAIL_FLAP_EXCHANGE_KEY;
import static org.flightcontrol.actuator.tailflap.TailFlap.TAIL_FLAP_EXCHANGE_NAME;
import static org.flightcontrol.flight.Flight.TICK_RATE;

public class Direction extends TimerTask implements Observer {

    // Directions are in degrees (0-360)
    static final int BEARING_TO_DESTINATION = 290;
    public static final String DIRECTION_EXCHANGE_NAME = "DirectionExchange";
    public static final String DIRECTION_EXCHANGE_KEY = "DirectionKey";

    Phaser phaser;
    Integer currentDirection = 180;
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channelSend;
    Channel channelReceive;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        currentDirection = Integer.valueOf(message);
    };

    @Override
    public void run() {
        sendCurrentDirection();
        System.out.println("Direction: " + currentDirection);
    }

    public Direction(Phaser phaser) {
        this.phaser = phaser;

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelSend = connection.createChannel();
            channelReceive = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }
    }

    @Override
    public void update() {
        if (phaser.getPhase() == 2) {
            System.out.println("Direction: Destination is at a bearing of " + BEARING_TO_DESTINATION);
            listenForTailFlap();
            timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
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

    private void sendCurrentDirection() {
        try {
            channelSend.exchangeDeclare(DIRECTION_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = currentDirection.toString();
            channelSend.basicPublish(DIRECTION_EXCHANGE_NAME, DIRECTION_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

}
