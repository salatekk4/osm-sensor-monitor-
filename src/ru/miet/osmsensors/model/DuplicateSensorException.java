package ru.miet.osmsensors.model;

/** Попытка добавить датчик с уже существующим ID. */
public class DuplicateSensorException extends RuntimeException {
    private final int sensorId;

    public DuplicateSensorException(int sensorId) {
        super("Датчик с ID " + sensorId + " уже существует");
        this.sensorId = sensorId;
    }

    public int getSensorId() {
        return sensorId;
    }
}
