package ru.miet.osmsensors.model;

import java.util.Locale;
import java.util.Set;

/** Geographic form validation: x=longitude, y=latitude (WGS84 degrees), radius=metres. */
public class SensorValidator {
    public static final Set<String> TYPES = Set.of("T", "CO2", "N2");
    public static final Set<String> STATUSES = Set.of("OK", "WARNING", "ERROR");

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }

    private static double parseDouble(String text, String field, ValidationResult result) {
        try {
            double value = Double.parseDouble(text.trim().replace(',', '.'));
            if (!Double.isFinite(value)) {
                result.add(field, "ожидалось конечное число");
                return Double.NaN;
            }
            return value;
        } catch (NumberFormatException | NullPointerException ex) {
            result.add(field, "ожидалось число");
            return Double.NaN;
        }
    }

    /** Signature retained for controllers: x is longitude, y is latitude. */
    public static ValidationResult validate(String id, String x, String y, String radius,
                                            String value, String type, String status) {
        ValidationResult result = new ValidationResult();
        try {
            if (Integer.parseInt(id.trim()) < 0)
                result.add("ID", "не может быть отрицательным");
        } catch (NumberFormatException | NullPointerException ex) {
            result.add("ID", "ожидалось целое число");
        }
        double longitude = parseDouble(x, "X", result);
        double latitude = parseDouble(y, "Y", result);
        if (longitude < -180 || longitude > 180)
            result.add("X", "долгота WGS84 должна быть от -180 до 180 градусов");
        if (latitude < -90 || latitude > 90)
            result.add("Y", "широта WGS84 должна быть от -90 до 90 градусов");
        if (parseDouble(radius, "Радиус", result) < 0)
            result.add("Радиус", "радиус в метрах не может быть отрицательным");
        parseDouble(value, "Значение", result);
        if (!TYPES.contains(normalize(type))) result.add("Тип", "допустимы T, CO2, N2");
        if (!STATUSES.contains(normalize(status))) result.add("Статус", "допустимы OK, WARNING, ERROR");
        return result;
    }

    /** Performs validation itself; invalid data cannot bypass the form check here. */
    public static Sensor createSensor(String id, String x, String y, String radius,
                                      String value, String type, String status) {
        ValidationResult result = validate(id, x, y, radius, value, type, status);
        if (!result.isValid()) throw new IllegalArgumentException(result.toMessage());
        int parsedId = Integer.parseInt(id.trim());
        double longitude = Double.parseDouble(x.trim().replace(',', '.'));
        double latitude = Double.parseDouble(y.trim().replace(',', '.'));
        double radiusMeters = Double.parseDouble(radius.trim().replace(',', '.'));
        double reading = Double.parseDouble(value.trim().replace(',', '.'));
        switch (normalize(type)) {
            case "CO2": return new Co2Sensor(parsedId, longitude, latitude, radiusMeters, reading, normalize(status));
            case "N2": return new GasSensor(parsedId, longitude, latitude, radiusMeters, reading, normalize(status));
            default: return new TemperatureSensor(parsedId, longitude, latitude, radiusMeters, reading, normalize(status));
        }
    }
}
