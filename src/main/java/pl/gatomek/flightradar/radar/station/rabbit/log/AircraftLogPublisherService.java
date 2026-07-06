package pl.gatomek.flightradar.radar.station.rabbit.log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.gatomek.flightradar.radar.station.rabbit.config.RabbitMQConnectionFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPOutputStream;

public class AircraftLogPublisherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AircraftLogPublisherService.class);
    private static final String QUEUE_NAME = "RADAR_DATA";

    private final RabbitMQConnectionFactory connectionFactory;
    private final ExecutorService es = Executors.newSingleThreadExecutor();
    private Connection connection;
    private Channel channel;

    public AircraftLogPublisherService(RabbitMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void open() throws IOException, TimeoutException {
        connection = connectionFactory.getConnectionFactory().newConnection(es);
        channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
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

    public void publishAircraftLog(String aircraftLog) {
        try {
            byte[] messageBody;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                 GZIPOutputStream gzip = new GZIPOutputStream(os)) {
                gzip.write(aircraftLog.getBytes(StandardCharsets.UTF_8));
                gzip.finish();
                messageBody = os.toByteArray();
            }

            AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
            propsBuilder.contentType("application/json");
            propsBuilder.contentEncoding("gzip");
            BasicProperties props = propsBuilder.build();

            channel.basicPublish("", QUEUE_NAME, props, messageBody);
        } catch (IOException ioe) {
            LOGGER.error("Publish Aircraft Log", ioe);
        }
    }
}
