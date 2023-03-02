package org.flightcontrol.sensor.altitude;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.flightcontrol.Observer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.flight.Flight.TICK_RATE;

public class Altitude implements Runnable, Observer {

    static final Integer INCREMENT_TAKEOFF_LANDING = 500;
    static final Integer MAX_FLUCTUATION_TAKEOFF_LANDING = 100;
    static final String QUEUE_NAME = "AltitudeQueue";

    Phaser phaser;
    Integer currentAltitude = 0;
    AltitudeState altitudeState;
    LinkedList<Observer> observers = new LinkedList<>();;
    Timer timer = new Timer();

    Channel channel;

    public Altitude(Phaser phaser) {
        this.phaser = phaser;
        phaser.register();
    }

    @Override
    public void run() {
        phaser.arriveAndAwaitAdvance();
        changeState(new TakeoffState(this));

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            Connection connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}

        TimerTask messageTask = new TimerTask() {
            @Override
            public void run() {

                try {
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    String stringAltitude = currentAltitude.toString();
                    channel.basicPublish(stringAltitude, QUEUE_NAME, null, stringAltitude.getBytes());
                } catch (IOException ignored) {}

            }
        };
        timer.scheduleAtFixedRate(messageTask, 0L, TICK_RATE);


    }

    public void changeState(AltitudeState newAltitudeState) {
        if (altitudeState != null) {
            altitudeState.stopExecuting();
        }

        this.altitudeState = newAltitudeState;
        for (Observer observer : observers) {
            observer.update();
        }

        altitudeState.generateAltitude();
    }

    @Override
    public void update() {
        if (phaser.getPhase() == 3) {
            changeState(new LandingState(this));
        }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public AltitudeState getAltitudeState() {
        return altitudeState;
    }

    public Integer getCurrentAltitude() {
        return currentAltitude;
    }

    public void setCurrentAltitude(Integer newAltitude) {
        this.currentAltitude = newAltitude;
    }
}
