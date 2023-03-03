package org.flightcontrol;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class TestSender extends TimerTask {

    static final String QUEUE_NAME = "TestQueue";

    public static void main(String[] args) {
        Timer timer = new Timer();
        TestSender testSender = new TestSender();
//        testSender.send();
        timer.schedule(testSender, 0L, 1000L);
    }

    @Override
    public void run() {
        try {
            send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TestSender() {
        try {
            ConnectionFactory cf = new ConnectionFactory();
            Connection connection = cf.newConnection();
            channel = connection.createChannel();
//            connection.close();
        } catch (IOException | TimeoutException ignored) {
        }

    }

    Channel channel;

    public void send() throws IOException {
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String randomMessage = "This is a random message!";
        channel.basicPublish("", QUEUE_NAME, null, randomMessage.getBytes());
    }
}
