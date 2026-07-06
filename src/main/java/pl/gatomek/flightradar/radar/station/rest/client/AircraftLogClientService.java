package pl.gatomek.flightradar.radar.station.rest.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AircraftLogClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AircraftLogClientService.class);
    private final String url;
    private final OkHttpClient httpClient;

    public AircraftLogClientService(OkHttpClient httpClient, String url) {
        this.url = url;
        this.httpClient = httpClient;
    }

    public String getAircraftLogs() {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                LOGGER.error("Response error: {}", response.code());
            }
        } catch (NullPointerException npe) {
            LOGGER.warn("Null Pointer Exception", npe);
        } catch (IOException ioe) {
            LOGGER.error("IO Exception", ioe);
        }

        return null;
    }
}
