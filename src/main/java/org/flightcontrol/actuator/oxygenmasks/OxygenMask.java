package org.flightcontrol.actuator.oxygenmasks;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.sensor.cabinpressure.CabinPressure.*;

enum OxygenMaskState {DEPLOYED, STOWED}

public class OxygenMask {

    public static final String OXYGEN_MASK_ID = "OxygenMask";

    LinkedList<Observer> observers = new LinkedList<>();
    OxygenMaskState oxygenMaskState;

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    // Callback to be used by Rabbit MQ receive
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        if (message.equals(TOGGLE_PRESSURE_FLAG)) {
            if (oxygenMaskState == null ||
                    oxygenMaskState.equals(OxygenMaskState.DEPLOYED)) {
                setOxygenMaskState(OxygenMaskState.STOWED);
            } else {
                setOxygenMaskState(OxygenMaskState.DEPLOYED);
            }
        }
    };

    public void setOxygenMaskState(OxygenMaskState oxygenMaskState) {
        this.oxygenMaskState = oxygenMaskState;

        for (Observer observer : observers) {
            observer.update(OXYGEN_MASK_ID, oxygenMaskState.toString());
        }
    }


    public OxygenMask() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        listenForCabinPressure();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    private void listenForCabinPressure() {
        try {
            channel.exchangeDeclare(CABIN_PRESSURE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, CABIN_PRESSURE_EXCHANGE_NAME, CABIN_PRESSURE_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException ignored) {}

    }
}
