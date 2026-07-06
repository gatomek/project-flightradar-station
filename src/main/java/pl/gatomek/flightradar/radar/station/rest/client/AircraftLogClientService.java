package pl.gatomek.flightradar.radar.station.rest.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AircraftLogClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AircraftLogClientService.class);
    private final OkHttpClient httpClient;
    private final Request request;

    public AircraftLogClientService(OkHttpClient httpClient, String url) {
        this.httpClient = httpClient;
        this.request = new Request.Builder().url(url).build();
    }

    public String getAircraftLogs() {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }

            okhttp3.ResponseBody body = response.body();
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
