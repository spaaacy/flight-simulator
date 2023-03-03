package org.flightcontrol;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.TestTwo.EXCHANGE_TWO_KEY;
import static org.flightcontrol.TestTwo.EXCHANGE_TWO_NAME;

public class TestOne extends TimerTask {

    static final String EXCHANGE_ONE_NAME = "TestOneExchange";
    static final String EXCHANGE_ONE_KEY = "TestOneKey";



    Channel channelSend;
    Channel channelReceive;
    DeliverCallback deliverCallback;
    int number = 0;

    public TestOne() {
        try {
            ConnectionFactory cf = new ConnectionFactory();
            Connection connection = cf.newConnection();
            channelSend = connection.createChannel();
            channelReceive = connection.createChannel();
        } catch (IOException | TimeoutException ignored) { }

        deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(message);
        };
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        TestOne testSender = new TestOne();
        timer.schedule(testSender, 0L, 1000L);
        testSender.receive();
    }

    @Override
    public void run() {
        send();
    }


    public void receive() {
        try {
            channelReceive.exchangeDeclare(EXCHANGE_TWO_NAME, BuiltinExchangeType.FANOUT);
            String queueName = channelReceive.queueDeclare().getQueue();
            channelReceive.queueBind(queueName, EXCHANGE_TWO_NAME, EXCHANGE_TWO_KEY);
            channelReceive.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (IOException ignored) { }
    }

    public void send() {
        try {
            number++;
            channelSend.exchangeDeclare(EXCHANGE_ONE_NAME, BuiltinExchangeType.FANOUT);
            String randomMessage = "TestOne: This is a random message number " + number;
            channelSend.basicPublish(EXCHANGE_ONE_NAME, EXCHANGE_ONE_KEY, null, randomMessage.getBytes());
        } catch (IOException ignored) { }
    }
}
