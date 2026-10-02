package ru.miet.osmsensors.model;

/** Датчик с таким id не найден в хранилище. */
public class SensorNotFoundException extends Exception {
    private final int sensorId;

    public SensorNotFoundException(int sensorId) {
        super("Датчик с id=" + sensorId + " не найден");
        this.sensorId = sensorId;
    }

    public int getSensorId() {
        return sensorId;
    }
}
