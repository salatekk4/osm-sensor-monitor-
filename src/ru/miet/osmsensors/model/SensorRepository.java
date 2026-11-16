package ru.miet.osmsensors.model;

import java.util.Collection;

/** Repository contract. Returned sensors are detached from stored state. */
public interface SensorRepository {
    /** false for null, duplicate ID or full storage; no checked exceptions. */
    boolean add(Sensor s);
    Sensor findById(int id);
    Sensor[] findByType(String type);
    Sensor[] getAll();
    int getCount();
    int getCapacity();
    Sensor[] findAlarmSensors();
    void addUnique(Sensor s) throws DuplicateSensorException, StorageFullException;
    Sensor getById(int id) throws SensorNotFoundException;
    void updateById(int id, Sensor updatedSensor)
            throws SensorNotFoundException, DuplicateSensorException;
    void deleteById(int id) throws SensorNotFoundException;

    /**
     * Validate and replace the whole set atomically, or leave it unchanged.
     * No delete/add fallback: implementations must explicitly support atomicity.
     * Invalid geographic data throws IllegalArgumentException.
     */
    default void replaceAllValidated(Collection<? extends Sensor> sensors)
            throws DuplicateSensorException, StorageFullException {
        throw new UnsupportedOperationException("Atomic replacement is not supported");
    }

    default void updateSensor(int id, String type, double x, double y,
                              double radius, double value, String status)
            throws SensorNotFoundException, DuplicateSensorException {
        getById(id);
        Sensor updated = SensorValidator.createSensor(Integer.toString(id),
                Double.toString(x), Double.toString(y), Double.toString(radius),
                Double.toString(value), type, status);
        updateById(id, updated);
    }
}
