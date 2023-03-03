package org.flightcontrol.actuator.wingflap;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.flightcontrol.Observer;
import org.flightcontrol.sensor.altitude.Altitude;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_QUEUE_NAME;

enum Direction {UP, DOWN, NEUTRAL}

public class WingFlap extends TimerTask implements Observer {

    Altitude altitude;
    Integer currentAltitude;
    Phaser phaser;
    WingFlapState wingFlapState;
    Direction direction;
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channel;
    DeliverCallback altitudeCallback;


    // Plane attempts to fly 10500-11500
    public static final Integer CRUISING_ALTITUDE = 11000;
    static final Integer MAX_FLUCTUATION_UP_DOWN = 5;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 500;
    static final Integer INCREMENT_VALUE_UP_DOWN = 15;
    static final Integer ACCEPTED_RANGE = 500;

    public WingFlap(Altitude altitude, Phaser phaser) {
        this.altitude = altitude;
        this.phaser = phaser;

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        altitudeCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            currentAltitude = Integer.valueOf(  message);
            System.out.println("Message received: " + message);
        };

    }

    @Override
    public void run() {

        try {
            channel.queueDeclare(ALTITUDE_QUEUE_NAME, false, false, false, null);
            channel.basicConsume(ALTITUDE_QUEUE_NAME, true, altitudeCallback, consumerTag -> {});
        } catch (IOException ignored) { }

        wingFlapState.controlFlaps();
    }

    @Override
    public void update() {
        switch (phaser.getPhase()) {
            case 1 -> direction = Direction.DOWN;
            case 2 -> {
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
