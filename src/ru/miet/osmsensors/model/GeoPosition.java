package ru.miet.osmsensors.model;

/** Immutable WGS84 position in decimal degrees. Constructor order: latitude, longitude. */
public final class GeoPosition {
    public static final double WEB_MERCATOR_MAX_LATITUDE = 85.0511287798066;
    private final double latitude;
    private final double longitude;

    public GeoPosition(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Latitude must be finite and in [-90, 90]");
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Longitude must be finite and in [-180, 180]");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isWebMercatorSupported() {
        return Math.abs(latitude) <= WEB_MERCATOR_MAX_LATITUDE;
    }

    /** Explicit adapter only: legacy x=longitude, y=latitude. No migration or guessing. */
    public static GeoPosition fromSensor(Sensor sensor) {
        if (sensor == null) throw new IllegalArgumentException("Sensor must not be null");
        return new GeoPosition(sensor.getY(), sensor.getX());
    }
}
