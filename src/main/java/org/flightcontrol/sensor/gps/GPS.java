package org.flightcontrol.sensor.gps;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.actuator.tailflap.TailFlap.*;
import static org.flightcontrol.flight.Flight.TICK_RATE;

public class GPS extends TimerTask implements Observer {

    // Directions are in degrees (0-360)
    static final String DEGREE_SYMBOL = "°";
    public static final Integer STARTING_BEARING = 180;

    public static final String GPS_EXCHANGE_NAME = "GPSExchange";
    public static final String GPS_EXCHANGE_KEY = "GPSKey";

    Phaser phaser;
    Integer currentBearing = STARTING_BEARING;
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channelSend;
    Channel channelReceive;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        currentBearing = Integer.valueOf(message);
    };

    @Override
    public void run() {
        sendCurrentDirection();
        System.out.println("GPS: " + currentBearing + DEGREE_SYMBOL);
    }

    public GPS(Phaser phaser) {
        this.phaser = phaser;

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelSend = connection.createChannel();
            channelReceive = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }
    }

    @Override
    public void update(String... updatedValue) {
        switch (phaser.getPhase()) {
            case 1 -> {
                System.out.println("GPS: " + currentBearing + DEGREE_SYMBOL);
                System.out.println("GPS: Destination is at a bearing of " + BEARING_DESTINATION + DEGREE_SYMBOL);
            }
            case 2 -> {
                listenForTailFlap();
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case 3 -> {
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
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
            channelSend.exchangeDeclare(GPS_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = currentBearing.toString();
            channelSend.basicPublish(GPS_EXCHANGE_NAME, GPS_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) {}
    }

}
