package pl.gatomek.flightradar.radar.station.rabbit.config;

import com.rabbitmq.client.ConnectionFactory;
import pl.gatomek.flightradar.radar.station.config.RabbitProperties;

public class RabbitMQConnectionFactory {

    private final ConnectionFactory connectionFactory;

    public RabbitMQConnectionFactory(RabbitProperties props) {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(props.getRabbitHost());
        factory.setPort(props.getRabbitPort());
        factory.setUsername(props.getRabbitUsername());
        factory.setPassword(props.getRabbitPassword());
        factory.setAutomaticRecoveryEnabled(true);

        this.connectionFactory = factory;
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
}
