package org.flightcontrol.sensor.cabinpressure;

import com.rabbitmq.client.*;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.*;

enum CabinPressureStatus {NORMAL, BREACHED}

public class CabinPressure extends TimerTask {

    public static final String CABIN_PRESSURE_ID = "CabinPressure";
    public static final Float NORMAL_CABIN_PRESSURE = 11.5f;
    static final Float BREACHED_CABIN_PRESSURE = 8.0f;
    static final Float MAX_FLUCTUATION = 0.5f;
    public static final String PRESSURE_UNIT = " psi";
    public static final String PSI_ID = "Psi";
    public static final String STATUS_ID = "Status";
    public static final String CABIN_PRESSURE_EXCHANGE_KEY = "CabinPressureKey";
    public static final String CABIN_PRESSURE_EXCHANGE_NAME = "CabinPressureExchange";
    public static final String TOGGLE_PRESSURE_FLAG = "ToggleFlag";

    Float currentCabinPressure;
    CabinPressureState cabinPressureState;
    CabinPressureStatus cabinPressureStatus;
    LinkedList<Observer> observers = new LinkedList<>();
    Timer timer = new Timer();

    // RabbitMQ variables
    Connection connection;
    Channel channel;

    /*
    * Callback to be used by RabbitMQ
    */
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        if (message.equals(TOGGLE_PRESSURE_FLAG)){
            switch (cabinPressureStatus) {
                case BREACHED -> cabinPressureState = new CabinPressureNormalState(this);
                case NORMAL -> cabinPressureState = new CabinPressureBreachedState(this);
            }
        }
    };

    DeliverCallback flightCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        receiveFlightPhase(message);
    };

    @Override
    public void run() {
        cabinPressureState.generatePsi();
    }

    public CabinPressure() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        listenForFlight();
        listenForToggle();
    }

    private void sendCabinPressure() {
        try {
            channel.exchangeDeclare(CABIN_PRESSURE_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.basicPublish(CABIN_PRESSURE_EXCHANGE_NAME, CABIN_PRESSURE_EXCHANGE_KEY, null, TOGGLE_PRESSURE_FLAG.getBytes());
        } catch (IOException ignored) {}
    }

    private void receiveFlightPhase(String flightPhase) {
        switch (flightPhase) {
            case FLIGHT_PHASE_PARKED -> {
                cabinPressureState = new CabinPressureNormalState(this);
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case FLIGHT_PHASE_LANDED -> {
                timer.cancel();
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void listenForFlight() {
        try {
            channel.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, FLIGHT_EXCHANGE_NAME, FLIGHT_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, flightCallback, consumerTag -> {
            });
        } catch (IOException ignored) {}
    }

    public void setCurrentCabinPressure(Float newCabinPressure) {
        this.currentCabinPressure = newCabinPressure;

        String cabinPressureString = String.format("%.2f" + PRESSURE_UNIT, newCabinPressure);
        System.out.println("Cabin Pressure: " + cabinPressureString);

        for (Observer observer : observers) {
            observer.update(CABIN_PRESSURE_ID, PSI_ID, cabinPressureString);
        }
    }

    private void listenForToggle() {
        try {
            channel.exchangeDeclare(FLIGHT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, FLIGHT_EXCHANGE_NAME, CABIN_PRESSURE_EXCHANGE_KEY);
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (IOException ignored) {}
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    protected void setCabinPressureStatus(CabinPressureStatus newCabinPressureStatus) {
        this.cabinPressureStatus = newCabinPressureStatus;
        sendCabinPressure();
        System.out.println("Cabin Pressure: " + newCabinPressureStatus);

        for (Observer observer : observers) {
            observer.update(CABIN_PRESSURE_ID, STATUS_ID, newCabinPressureStatus.toString());
        }
    }
}
