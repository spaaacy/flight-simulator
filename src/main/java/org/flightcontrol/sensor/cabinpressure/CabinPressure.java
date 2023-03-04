package org.flightcontrol.sensor.cabinpressure;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.FLIGHT_EXCHANGE_NAME;

public class CabinPressure extends TimerTask {

    static final Float NORMAL_PRESSURE = 11.3f;
    static final String PRESSURE_UNIT = "psi";
    private static final String CABIN_PRESSURE_EXCHANGE_KEY = "CabinPressureKey";
    public static final String NORMAL_STATUS = "Normal";
    public static final String BREACHED_STATUS = "Breached";


    CabinPressureState cabinPressureState;
    String status;
    LinkedList<Observer> observers = new LinkedList<>();

    @Override
    public void run() {
        // TODO: Generate psi
    }

    // RabbitMQ variables
    Connection connection;
    Channel channelReceive;
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        setNewStatus(message);
    };

    public CabinPressure() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channelReceive = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForTrigger();
    }

    private void listenForTrigger() {
        try {
            channelReceive.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, FLIGHT_EXCHANGE_NAME, CABIN_PRESSURE_EXCHANGE_KEY);
            channelReceive.basicConsume(queueName, true, deliverCallback, consumerTag -> {})
        } catch (IOException ignored) {}
    }

    private void setNewStatus(String newStatus) {
        this.status = newStatus;

        for (Observer observer : observers) {
            observer.update();
        }
    }
}
