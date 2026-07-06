package pl.gatomek.flightradar.radar.station.config;


public class LocalizationProperties extends BaseProperties {

    public String getRadarLat() {
        return get("radar.lat");
    }

    public String getRadarLon() {
        return get("radar.lon");
    }

    public String getRadarRange() {
        return get("radar.range");
    }
}
