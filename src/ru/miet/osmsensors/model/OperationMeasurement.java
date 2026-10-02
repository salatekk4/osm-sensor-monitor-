package ru.miet.osmsensors.model;

/**
 * Результат замера одной операции над коллекцией.
 */
public class OperationMeasurement {
    public static final String ADD = "ADD";
    public static final String REMOVE = "REMOVE";

    private final String operation;
    private final int index;
    private final int sensorId;
    private final long durationNanos;

    public OperationMeasurement(String operation, int index, int sensorId, long durationNanos) {
        this.operation = operation;
        this.index = index;
        this.sensorId = sensorId;
        this.durationNanos = durationNanos;
    }

    public String getOperation() { return operation; }

    /** Индекс, с которым выполнялась операция (для ADD - позиция вставленного элемента). */
    public int getIndex() { return index; }

    public int getSensorId() { return sensorId; }

    /** Длительность операции в наносекундах. */
    public long getDurationNanos() { return durationNanos; }

    @Override
    public String toString() {
        return String.format("%-6s | индекс: %-7d | ID датчика: %-7d | время: %d нс",
                operation, index, sensorId, durationNanos);
    }
}
