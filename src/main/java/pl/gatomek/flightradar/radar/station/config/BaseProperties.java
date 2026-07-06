package pl.gatomek.flightradar.radar.station.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BaseProperties {
    private final Properties props = new Properties();
    private final Map<String, String> values = HashMap.newHashMap(7);

    public void load(InputStream is) throws IOException {
        props.load(is);
    }

    public String get(String name) throws UnsupportedOperationException {
        String s = values.get(name);
        if (s != null) return s;

        String v = getCore(name);

        if (v == null) {
            throw new IllegalArgumentException("Configuration error: '" + name + "' property is missing.");
        }

        if (v.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration error: '" + name + "' property is empty.");
        }

        values.put(name, v);
        return v;
    }

    private String getCore(String name) throws UnsupportedOperationException {
        String key = name.replace(".", "_").toUpperCase();
        String ev = System.getenv(key);
        if (ev != null) {
            return ev;
        }

        return props.getProperty(name);
    }

}
