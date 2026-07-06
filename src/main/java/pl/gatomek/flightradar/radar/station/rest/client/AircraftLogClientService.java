package pl.gatomek.flightradar.radar.station.rest.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

public class AircraftLogClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AircraftLogClientService.class);
    private final OkHttpClient httpClient;
    private final Request request;
    private Instant timeout = Instant.now();

    public AircraftLogClientService(OkHttpClient httpClient, String url) {
        this.httpClient = httpClient;
        this.request = new Request.Builder().url(url).build();
    }

    public String getAircraftLogs() {
        if (Instant.now().isBefore(timeout)) {
            return null;
        }

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 429) {
                    String header = response.header("Retry-After");
                    if (header != null) {
                        long tmt = Long.parseLong(header);
                        timeout = Instant.now().plusSeconds(tmt);
                    }
                }

                return null;
            }

            ResponseBody body = response.body();
            if (body == null) {
                LOGGER.warn("Response body is null");
                return null;
            }

            return body.string();
        } catch (IOException ioe) {
            LOGGER.error("IO Exception", ioe);
        }

        return null;
    }
}
