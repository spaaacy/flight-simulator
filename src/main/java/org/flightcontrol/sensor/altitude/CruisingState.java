package org.flightcontrol.sensor.altitude;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_KEY;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_NAME;
import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_NAME;


public class CruisingState implements AltitudeState {

    Altitude altitude;

    // RabbitMQ variables
    Connection connection;
    Channel channelSend;
    Channel channelReceive;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        altitude.setCurrentAltitude(Integer.valueOf(message));
    };

    public CruisingState(Altitude altitude) {
        this.altitude = altitude;

        // Create channels for Rabbit MQ
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelSend = connection.createChannel();
            channelReceive = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForWingFlap();

    }

    @Override
    public void generateAltitude() {
        sendCurrentAltitude();
    }

    private void listenForWingFlap() {
        try {
            channelReceive.exchangeDeclare(WING_FLAP_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, WING_FLAP_EXCHANGE_NAME, WING_FLAP_EXCHANGE_KEY);
            channelReceive.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (IOException ignored) { }
    }

    private void sendCurrentAltitude() {
        try {
            channelSend.exchangeDeclare(ALTITUDE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message = altitude.currentAltitude.toString();
            channelSend.basicPublish(ALTITUDE_EXCHANGE_NAME, ALTITUDE_EXCHANGE_KEY, null, message.getBytes());
        } catch (IOException ignored) { }
    }

}
