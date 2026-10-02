package ru.miet.osmsensors.model;

/** Датчик с указанным ID не найден. */
public class SensorNotFoundException extends RuntimeException {
    private final int sensorId;

    public SensorNotFoundException(int sensorId) {
        super("Датчик с ID " + sensorId + " не найден");
        this.sensorId = sensorId;
    }

    public int getSensorId() {
        return sensorId;
    }
}
