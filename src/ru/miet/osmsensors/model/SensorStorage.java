package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Synchronized storage with detached copies on both input and output.
 * Existing single-record methods retain support for legacy planar data.
 * Bulk validated methods enforce the WGS84 contract via SensorValidator.
 * Mutate returned objects freely; persist changes through updateById/upsert.
 */
public class SensorStorage implements SensorRepository {
    private final List<Sensor> items;
    private final int capacity;
    private final Map<Integer, Long> lastWrite = new HashMap<>();
    private long collisionCount;

    public SensorStorage() { this(20); }

    public SensorStorage(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Negative capacity");
        this.capacity = capacity;
        this.items = new ArrayList<>(capacity);
    }

    private static Sensor copy(Sensor sensor) {
        if (sensor == null) throw new IllegalArgumentException("Sensor must not be null");
        return sensor.copy();
    }

    private static Sensor validatedCopy(Sensor sensor) {
        Sensor s = copy(sensor);
        ValidationResult result = SensorValidator.validate(Integer.toString(s.getId()),
                Double.toString(s.getX()), Double.toString(s.getY()),
                Double.toString(s.getRadius()), Double.toString(s.getValue()),
                s.getType(), s.getStatus());
        if (!result.isValid()) throw new IllegalArgumentException(result.toMessage());
        return s;
    }

    @Override
    public synchronized boolean add(Sensor sensor) {
        if (sensor == null || items.size() >= capacity) return false;
        Sensor s = copy(sensor);
        if (indexOf(s.getId()) >= 0) return false;
        items.add(s);
        lastWrite.put(s.getId(), System.currentTimeMillis());
        return true;
    }

    @Override
    public synchronized Sensor findById(int id) {
        int index = indexOf(id);
        return index < 0 ? null : copy(items.get(index));
    }

    @Override
    public synchronized Sensor[] findByType(String type) {
        List<Sensor> result = new ArrayList<>();
        if (type != null) for (Sensor s : items)
            if (type.equalsIgnoreCase(s.getType())) result.add(copy(s));
        return result.toArray(new Sensor[0]);
    }

    @Override
    public synchronized Sensor[] findAlarmSensors() {
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : items) if (s.isAlarm()) result.add(copy(s));
        return result.toArray(new Sensor[0]);
    }

    @Override
    public synchronized Sensor[] getAll() {
        Sensor[] result = new Sensor[items.size()];
        for (int i = 0; i < result.length; i++) result[i] = copy(items.get(i));
        return result;
    }

    @Override
    public synchronized int getCount() { return items.size(); }
    @Override
    public int getCapacity() { return capacity; }

    @Override
    public synchronized void addUnique(Sensor sensor)
            throws DuplicateSensorException, StorageFullException {
        Sensor s = copy(sensor);
        if (indexOf(s.getId()) >= 0) throw new DuplicateSensorException(s.getId());
        if (items.size() >= capacity) throw new StorageFullException(capacity);
        items.add(s);
        lastWrite.put(s.getId(), System.currentTimeMillis());
    }

    @Override
    public synchronized Sensor getById(int id) throws SensorNotFoundException {
        Sensor found = findById(id);
        if (found == null) throw new SensorNotFoundException(id);
        return found;
    }

    @Override
    public synchronized void updateById(int id, Sensor updatedSensor)
            throws SensorNotFoundException, DuplicateSensorException {
        Sensor updated = copy(updatedSensor);
        int index = indexOf(id);
        if (index < 0) throw new SensorNotFoundException(id);
        if (updated.getId() != id && indexOf(updated.getId()) >= 0)
            throw new DuplicateSensorException(updated.getId());
        items.set(index, updated);
        lastWrite.remove(id);
        lastWrite.put(updated.getId(), System.currentTimeMillis());
    }

    @Override
    public synchronized void deleteById(int id) throws SensorNotFoundException {
        int index = indexOf(id);
        if (index < 0) throw new SensorNotFoundException(id);
        items.remove(index);
        lastWrite.remove(id);
    }

    /** Validate first; a duplicate, invalid record or full storage changes nothing. */
    public synchronized void addAllUnique(Collection<? extends Sensor> sensors)
            throws DuplicateSensorException, StorageFullException {
        List<Sensor> staged = stage(sensors, true);
        long timestamp = System.currentTimeMillis();
        items.addAll(staged);
        for (Sensor s : staged) lastWrite.put(s.getId(), timestamp);
    }

    @Override
    public synchronized void replaceAllValidated(Collection<? extends Sensor> sensors)
            throws DuplicateSensorException, StorageFullException {
        List<Sensor> staged = stage(sensors, false);
        long timestamp = System.currentTimeMillis();
        items.clear();
        items.addAll(staged);
        lastWrite.clear();
        for (Sensor s : staged) lastWrite.put(s.getId(), timestamp);
    }

    private List<Sensor> stage(Collection<? extends Sensor> sensors, boolean append)
            throws DuplicateSensorException, StorageFullException {
        if (sensors == null) throw new IllegalArgumentException("Sensors must not be null");
        Set<Integer> ids = new HashSet<>();
        if (append) for (Sensor s : items) ids.add(s.getId());
        List<Sensor> staged = new ArrayList<>();
        for (Sensor source : sensors) {
            Sensor s = validatedCopy(source);
            if (!ids.add(s.getId())) throw new DuplicateSensorException(s.getId());
            staged.add(s);
            if ((long) staged.size() + (append ? items.size() : 0) > capacity)
                throw new StorageFullException(capacity);
        }
        return staged;
    }

    /** Last-write-wins by timestamp; equal timestamps keep last received state. */
    public synchronized WriteResult upsert(Sensor incoming, long timestamp) {
        if (timestamp < 0) throw new IllegalArgumentException("Negative timestamp");
        Sensor s = copy(incoming);
        int index = indexOf(s.getId());
        if (index < 0) {
            if (items.size() >= capacity) return WriteResult.STORAGE_FULL;
            items.add(s);
            lastWrite.put(s.getId(), timestamp);
            return WriteResult.ADDED;
        }
        collisionCount++;
        Long stored = lastWrite.get(s.getId());
        if (stored != null && timestamp < stored) return WriteResult.STALE_IGNORED;
        items.set(index, s);
        lastWrite.put(s.getId(), timestamp);
        return WriteResult.UPDATED;
    }

    /** Lifetime count; bulk replacement does not reset it. */
    public synchronized long getCollisionCount() { return collisionCount; }

    private int indexOf(int id) {
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).getId() == id) return i;
        return -1;
    }
}
