package pl.gatomek.flightradar.radar.station;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.gatomek.flightradar.radar.station.config.LocalizationProperties;
import pl.gatomek.flightradar.radar.station.config.RabbitProperties;
import pl.gatomek.flightradar.radar.station.rabbit.clock.RadarClockClientService;
import pl.gatomek.flightradar.radar.station.rabbit.config.RabbitMQConnectionFactory;
import pl.gatomek.flightradar.radar.station.rabbit.log.AircraftLogPublisherService;
import pl.gatomek.flightradar.radar.station.rest.client.AircraftLogClientService;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String URL_PATTERN = "https://api.airplanes.live/v2/point/{0}/{1}/{2}";
    private static final String RADAR_DATA_QUEUE_NAME = "RADAR_DATA";
    private static final String RADAR_EVENT_QUEUE_NAME = "RADAR_EVENT";

    public static void main(String[] args) {
        String localization = getLocalizationFromArguments(args);
        LOGGER.info("Localization: {}", localization);
        LocalizationProperties localizationProps = loadLocalizationProps(localization);

        String url = resolveUrl(localizationProps);
        LOGGER.info("URL: {}", url);
        OkHttpClient httpClient = makeHttpClient();
        AircraftLogClientService logClientService = new AircraftLogClientService(httpClient, url);

        RabbitProperties rabbitProps = loadRabbitProps();
        RabbitMQConnectionFactory rabbitMQConnectionFactory = new RabbitMQConnectionFactory(rabbitProps);
        AircraftLogPublisherService logPublisherService =
                new AircraftLogPublisherService(RADAR_DATA_QUEUE_NAME, rabbitMQConnectionFactory);
        AircraftLogPublisherService evtPublisherService =
                new AircraftLogPublisherService(RADAR_EVENT_QUEUE_NAME, rabbitMQConnectionFactory);

        try (ExecutorService es = Executors.newSingleThreadExecutor()) {
            Runnable task = () ->
                    CompletableFuture
                            .supplyAsync(logClientService::getAircraftLogs, es)
                            .thenAcceptAsync(log -> {
                                    logPublisherService.publishAircraftLog(log);
                                    evtPublisherService.publishAircraftLog(log);
                            }, es)
                            .exceptionallyAsync(ex -> {
                                        LOGGER.error("Main", ex);
                                        return null;
                                    }, es
                            );

            RadarClockClientService clockClientService = new RadarClockClientService(rabbitMQConnectionFactory, task);

            boolean logPublisherOpened = false;
            boolean evtPublisherOpened = false;
            boolean clockClientOpened = false;

            try {
                logPublisherService.open();
                logPublisherOpened = true;

                evtPublisherService.open();
                evtPublisherOpened = true;

                clockClientService.open();
                clockClientOpened = true;

                awaitForever();
            } catch (IOException ex) {
                LOGGER.error("IO Exception", ex);
            } catch (TimeoutException ex) {
                LOGGER.error("Timeout Exception", ex);
            } finally {
                if (clockClientOpened) {
                    try {
                        clockClientService.close();
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to close RadarClockClientService", ex);
                    }
                }

                if (logPublisherOpened) {
                    try {
                        logPublisherService.close();
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to close Data AircraftLogPublisherService", ex);
                    }
                }

                if (evtPublisherOpened) {
                    try {
                        evtPublisherService.close();
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to close Event AircraftLogPublisherService", ex);
                    }
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
    }

    private static String resolveUrl(LocalizationProperties props) {
        String lat = props.getRadarLat();
        String lon = props.getRadarLon();
        String range = props.getRadarRange();

        return MessageFormat.format(URL_PATTERN, lat, lon, range);
    }

    private static void awaitForever() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static OkHttpClient makeHttpClient() {
        return new OkHttpClient.Builder()
                .protocols(List.of(Protocol.HTTP_1_1))
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    private static LocalizationProperties loadLocalizationProps(String localization) {
        String propFileName = "application-" + localization + ".properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = loader.getResourceAsStream(propFileName);
        if (in == null) {
            throw new IllegalStateException("Application property file '" + propFileName + "' not found on the classpath");
        }

        LocalizationProperties radarProps = new LocalizationProperties();
        try (InputStream is = in) {
            radarProps.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application property file '" + propFileName + "'", e);
        }

        return radarProps;
    }

    private static RabbitProperties loadRabbitProps() {
        String propFileName = "application.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = loader.getResourceAsStream(propFileName);
        if (in == null) {
            throw new IllegalStateException("Application property file '" + propFileName + "' not found on the classpath");
        }

        RabbitProperties rabbitProps = new RabbitProperties();
        try (InputStream is = in) {
            rabbitProps.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application property file '" + propFileName + "'", e);
        }

        return rabbitProps;
    }

    private static String getLocalizationFromArguments(String[] args) {
        if (args.length > 0) {
            return args[0];
        }

        throw new UnsupportedOperationException("Localization is not given");
    }
}
