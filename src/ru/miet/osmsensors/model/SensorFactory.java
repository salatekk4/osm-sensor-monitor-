package ru.miet.osmsensors.model;

import java.util.Random;

/**
 * Генератор случайных датчиков на основе существующих конкретных классов.
 */
public class SensorFactory {
    private static final String[] STATUSES = {"OK", "WARNING", "ERROR"};

    private final Random random;

    public SensorFactory(Random random) {
        this.random = random;
    }

    public Sensor createRandomSensor(int id) {
        double x = random.nextDouble() * 1000;
        double y = random.nextDouble() * 1000;
        double radius = 1 + random.nextDouble() * 20;
        String status = STATUSES[random.nextInt(STATUSES.length)];

        switch (random.nextInt(3)) {
            case 0:
                return new TemperatureSensor(id, x, y, radius, -30 + random.nextDouble() * 100, status);
            case 1:
                return new Co2Sensor(id, x, y, radius, 300 + random.nextDouble() * 1200, status);
            default:
                return new GasSensor(id, x, y, radius, random.nextDouble() * 100, status);
        }
    }

    public Sensor[] createSensors(int count) {
        Sensor[] result = new Sensor[count];
        for (int i = 0; i < count; i++) {
            result[i] = createRandomSensor(i + 1);
        }
        return result;
    }
}
