package org.flightcontrol;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class TestSender extends TimerTask {

    public static void main(String[] args) {
        TestSender testSender = new TestSender();
        Timer timer = new Timer();
        timer.schedule(testSender, 0L, 1000L);
    }

    String QUEUE_NAME = "TestQueue";

    @Override
    public void run() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String randomMessage = Double.toString (Math.random() * 1000);
            channel.basicPublish(randomMessage, QUEUE_NAME, null, randomMessage.getBytes());
        } catch (IOException | TimeoutException ignored) {}
    }
}
