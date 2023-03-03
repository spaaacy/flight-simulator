package org.flightcontrol.sensor.altitude;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_QUEUE_NAME;

public class CruisingState extends TimerTask implements AltitudeState {

    Altitude altitude;
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    public CruisingState(Altitude altitude) {
        this.altitude = altitude;

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

    }

    @Override
    public void run() {

        try {
            channel.queueDeclare(ALTITUDE_QUEUE_NAME, false, false, false, null);
            String message = altitude.currentAltitude.toString();
            channel.basicPublish("", ALTITUDE_QUEUE_NAME, null, message.getBytes());
        } catch (IOException ignored) { }

        System.out.println("Altitude: " + altitude.currentAltitude);
    }

    @Override
    public void generateAltitude() {
        altitude.phaser.arriveAndAwaitAdvance(); // First arrive to go into phase 2: Cruising
        altitude.phaser.arrive(); // Second arrive to give thumbs-up for phase 3: Landing
        timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
    }

    @Override
    public void stopExecuting() {
        timer.cancel();
        try {
            connection.close();
        } catch (IOException ignored) {}
    }

}
