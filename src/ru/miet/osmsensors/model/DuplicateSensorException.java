package ru.miet.osmsensors.model;

/** Датчик с таким id уже есть в хранилище. */
public class DuplicateSensorException extends Exception {
    private final int sensorId;

    public DuplicateSensorException(int sensorId) {
        super("Датчик с id=" + sensorId + " уже существует");
        this.sensorId = sensorId;
    }

    public int getSensorId() {
        return sensorId;
    }
}
