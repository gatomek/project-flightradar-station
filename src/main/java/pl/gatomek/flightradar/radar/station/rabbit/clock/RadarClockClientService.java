package pl.gatomek.flightradar.radar.station.rabbit.clock;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.gatomek.flightradar.radar.station.rabbit.config.RabbitMQConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RadarClockClientService {
    private static final String EXCHANGE_NAME = "RADAR_CLOCK";
    private static final Logger LOGGER = LoggerFactory.getLogger(RadarClockClientService.class);
    private final RabbitMQConnectionFactory connectionFactory;
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private final Runnable processingUnit;
    private Connection connection;
    private Channel channel;

    public RadarClockClientService(RabbitMQConnectionFactory connectionFactory, Runnable processingUnit) {
        this.connectionFactory = connectionFactory;
        this.processingUnit = processingUnit;
    }

    public void open() throws IOException, TimeoutException {
        connection = connectionFactory.getConnectionFactory().newConnection(es);
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                processingUnit.run();
            } catch (Exception ex) {
                LOGGER.error("Processing unit error", ex);
            }
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    public void close() throws IOException, TimeoutException {
        if (channel != null) {
            channel.close();
        }
        if (connection != null) {
            connection.close();
        }

        es.shutdown();
        try {
            if (!es.awaitTermination(60, TimeUnit.SECONDS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException ie) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
