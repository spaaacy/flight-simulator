package org.flightcontrol;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class TestReceiver extends TimerTask {

//    static final String QUEUE_NAME = "TestQueue";
    static final String QUEUE_NAME = "AltitudeQueue";

    Connection connection;
    Channel channel;

    public TestReceiver() {
        try {
            ConnectionFactory cf = new ConnectionFactory();
            connection = cf.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException ignored) {}
    }

    public static void main(String[] args) {
        TestReceiver testReceiver = new TestReceiver();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(testReceiver, 0L, 1000L);

    }

    @Override
    public void run() {
        receive();
    }

    public void receive() {
        try {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            DeliverCallback consumeCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(message);
            };

            channel.basicConsume(QUEUE_NAME, true, consumeCallback, consumerTag -> {});
        } catch (IOException ignored) {}
    }
}
