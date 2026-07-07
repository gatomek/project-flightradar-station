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
    private Instant silence = Instant.now();
    private static final String RETRY_AFTER = "Retry-After";
    private static final String RESPONSE_BODY_IS_NULL = "Response body is null";
    private static final String IO_EXCEPTION = "IO Exception";

    public AircraftLogClientService(OkHttpClient httpClient, String url) {
        this.httpClient = httpClient;
        this.request = new Request.Builder().url(url).build();
    }

    public String getAircraftLogs() {
        if (Instant.now().isBefore(timeout)) {
            return null;
        }

        if(Instant.now().isBefore(silence)) {
            return null;
        }

        silence = Instant.now().plusSeconds(5);
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 429) {
                    String header = response.header(RETRY_AFTER);
                    if (header != null) {
                        long tmt = Long.parseLong(header);
                        timeout = Instant.now().plusSeconds(tmt + 1);
                    }
                }

                return null;
            }

            ResponseBody body = response.body();
            if (body == null) {
                LOGGER.warn(RESPONSE_BODY_IS_NULL);
                return null;
            }

            return body.string();
        } catch (IOException ioe) {
            LOGGER.error(IO_EXCEPTION, ioe);
        }

        return null;
    }
}
