package ru.miet.osmsensors.model;

/** Standalone checks; throws AssertionError and exits unsuccessfully on failure. */
public final class GeoCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static boolean valid(String x, String y, String radius) {
        return SensorValidator.validate("1", x, y, radius, "20", "T", "OK").isValid();
    }
    public static void main(String[] args) {
        check(valid("37.6", "55.7", "100"), "ordinary coordinates");
        check(valid("-180", "-90", "0"), "lower bounds");
        check(valid("180", "90", "0"), "upper bounds");
        check(valid("37,6", "55,7", "100"), "decimal comma");
        check(!valid("180.01", "0", "1"), "longitude range");
        check(!valid("0", "-90.01", "1"), "latitude range");
        check(!valid("NaN", "0", "1"), "NaN");
        check(!valid("0", "Infinity", "1"), "infinity");
        check(!valid(null, "0", "1"), "null");
        check(!valid("0", "0", "-1"), "negative radius");
        check(!valid("0", "0", "Infinity"), "infinite radius");
        Sensor sensor = SensorValidator.createSensor("1", "37.6", "55.7", "100", "20", "t", "ok");
        GeoPosition position = GeoPosition.fromSensor(sensor);
        check(position.getLatitude() == 55.7 && position.getLongitude() == 37.6, "coordinate order");
        check(position.isWebMercatorSupported(), "map latitude");
        check(!new GeoPosition(90, 0).isWebMercatorSupported(), "pole not supported by Mercator");
        boolean rejected = false;
        try { new GeoPosition(Double.NaN, 0); } catch (IllegalArgumentException ex) { rejected = true; }
        check(rejected, "position validates constructor");
        rejected = false;
        try { SensorValidator.createSensor("1", "181", "0", "1", "20", "T", "OK"); }
        catch (IllegalArgumentException ex) { rejected = true; }
        check(rejected, "creation validates input");
        System.out.println("GeoCheck: OK");
    }
}
