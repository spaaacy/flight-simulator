package org.flightcontrol;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.TestOne.EXCHANGE_ONE_KEY;
import static org.flightcontrol.TestOne.EXCHANGE_ONE_NAME;

public class TestTwo extends TimerTask {

    static final String EXCHANGE_TWO_NAME = "TestTwoExchange";
    static final String EXCHANGE_TWO_KEY = "TestTwoKey";

    Channel channelReceive;
    Channel channelSend;
    DeliverCallback deliverCallback;
    int number = 0;

    public TestTwo() {
        try {
            ConnectionFactory cf = new ConnectionFactory();
            Connection connection = cf.newConnection();
            channelReceive = connection.createChannel();
            channelSend = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(message);
        };
    }

    public static void main(String[] args) {
        TestTwo testReceiver = new TestTwo();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(testReceiver, 0L, 1000L);
        testReceiver.receive();
    }

    @Override
    public void run() {
        send();
    }

    public void receive() {
        try {
            channelReceive.exchangeDeclare(EXCHANGE_ONE_NAME, BuiltinExchangeType.FANOUT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, EXCHANGE_ONE_NAME, EXCHANGE_ONE_KEY);
            channelReceive.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (IOException ignored) { }
    }

    public void send() {
        try {
            number++;
            channelSend.exchangeDeclare(EXCHANGE_TWO_NAME, BuiltinExchangeType.FANOUT);
            String randomMessage = "TestTwo: This is another random message number " + number;
            channelSend.basicPublish(EXCHANGE_TWO_NAME, EXCHANGE_TWO_KEY, null, randomMessage.getBytes());
        } catch (IOException ignored) { }
    }
}
