package ru.miet.osmsensors.model;

import java.util.Objects;
import java.util.Random;

/** Seeded geographic generator: x=longitude, y=latitude, radius in metres. */
public class SensorFactory {
    private static final String[] STATUSES = {"OK", "WARNING", "ERROR"};
    private final Random random;
    private final double minLon, maxLon, minLat, maxLat, minRadius, maxRadius;

    /** Default geographic area; existing GUI and benchmark calls remain valid. */
    public SensorFactory(Random random) {
        this(random, 35.2, 40.0, 54.5, 56.8, 1000, 5000);
    }

    public SensorFactory(Random random, double minLon, double maxLon,
                         double minLat, double maxLat, double minRadius, double maxRadius) {
        this.random = Objects.requireNonNull(random, "random");
        range(minLon, maxLon, -180, 180);
        range(minLat, maxLat, -90, 90);
        range(minRadius, maxRadius, 0, Double.MAX_VALUE);
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
    }

    private static void range(double min, double max, double lower, double upper) {
        if (!Double.isFinite(min) || !Double.isFinite(max)
                || min < lower || max > upper || min > max)
            throw new IllegalArgumentException("Invalid generator bounds");
    }

    private double between(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    public Sensor createRandomSensor(int id) {
        if (id < 0) throw new IllegalArgumentException("Negative sensor ID");
        double x = between(minLon, maxLon);
        double y = between(minLat, maxLat);
        double radius = between(minRadius, maxRadius);
        String status = STATUSES[random.nextInt(STATUSES.length)];
        switch (random.nextInt(3)) {
            case 0: return new TemperatureSensor(id, x, y, radius, -30 + random.nextDouble() * 100, status);
            case 1: return new Co2Sensor(id, x, y, radius, 300 + random.nextDouble() * 1200, status);
            default: return new GasSensor(id, x, y, radius, random.nextDouble() * 100, status);
        }
    }

    public Sensor[] createSensors(int count) {
        if (count < 0) throw new IllegalArgumentException("Negative sensor count");
        Sensor[] result = new Sensor[count];
        for (int i = 0; i < count; i++) result[i] = createRandomSensor(i + 1);
        return result;
    }
}
