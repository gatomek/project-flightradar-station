package pl.gatomek.flightradar.radar.station.config;

public class RabbitProperties extends BaseProperties {
    public String getRabbitHost() {
        return get("rabbit.host");
    }

    public int getRabbitPort() {
        String port = get("rabbit.port");
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("RabbitMQ configuration error: 'rabbit.port' must be a valid integer, but was: '" + port + "'", e);
        }
    }

    public String getRabbitUsername() {
        return get("rabbit.username");
    }

    public String getRabbitPassword() {
        return get("rabbit.password");
    }
}
