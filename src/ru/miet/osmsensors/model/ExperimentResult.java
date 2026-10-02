package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Итог одного запуска эксперимента: одна коллекция + один размер.
 */
public class ExperimentResult {
    private final String collectionName;
    private final int size;
    private final List<OperationMeasurement> addMeasurements = new ArrayList<>();
    private final List<OperationMeasurement> removeMeasurements = new ArrayList<>();

    public ExperimentResult(String collectionName, int size) {
        this.collectionName = collectionName;
        this.size = size;
    }

    public void addMeasurement(OperationMeasurement m) {
        if (OperationMeasurement.ADD.equals(m.getOperation())) {
            addMeasurements.add(m);
        } else {
            removeMeasurements.add(m);
        }
    }

    public String getCollectionName() { return collectionName; }
    public int getSize() { return size; }

    public List<OperationMeasurement> getAddMeasurements() {
        return new ArrayList<>(addMeasurements);
    }

    public List<OperationMeasurement> getRemoveMeasurements() {
        return new ArrayList<>(removeMeasurements);
    }

    public long getTotalAddNanos() { return sum(addMeasurements); }
    public long getTotalRemoveNanos() { return sum(removeMeasurements); }

    public double getAverageAddNanos() { return average(addMeasurements); }
    public double getAverageRemoveNanos() { return average(removeMeasurements); }

    private static long sum(List<OperationMeasurement> list) {
        long total = 0;
        for (OperationMeasurement m : list) {
            total += m.getDurationNanos();
        }
        return total;
    }

    private static double average(List<OperationMeasurement> list) {
        if (list.isEmpty()) {
            return 0.0;
        }
        return (double) sum(list) / list.size();
    }

    /** Имя лог-файла по соглашению: коллекция_размер.log */
    public String getSuggestedLogName() {
        return collectionName + "_" + size + ".log";
    }

    @Override
    public String toString() {
        return String.format(
                "%s, размер %d: add (всего %d нс, среднее %.1f нс), remove (всего %d нс, среднее %.1f нс)",
                collectionName, size,
                getTotalAddNanos(), getAverageAddNanos(),
                getTotalRemoveNanos(), getAverageRemoveNanos());
    }
}
