package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранилище датчиков на ArrayList. Лимит ёмкости сохранён, чтобы поведение
 * старых методов не изменилось.
 */
public class SensorStorage implements SensorRepository {
    private final List<Sensor> items;
    private final int capacity;

    public SensorStorage() {
        this(20);
    }

    public SensorStorage(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>(Math.max(capacity, 0));
    }

    @Override
    public boolean add(Sensor s) {
        if (s == null || items.size() >= capacity) {
            return false;
        }
        items.add(s);
        return true;
    }

    @Override
    public Sensor findById(int id) {
        for (Sensor s : items) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    @Override
    public Sensor[] findByType(String type) {
        if (type == null) {
            return new Sensor[0];
        }
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : items) {
            if (type.equalsIgnoreCase(s.getType())) {
                result.add(s);
            }
        }
        return result.toArray(new Sensor[0]);
    }

    @Override
    public Sensor[] findAlarmSensors() {
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : items) {
            if (s.isAlarm()) {
                result.add(s);
            }
        }
        return result.toArray(new Sensor[0]);
    }

    @Override
    public Sensor[] getAll() {
        return items.toArray(new Sensor[0]);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void addUnique(Sensor s) throws DuplicateSensorException, StorageFullException {
        if (s == null) {
            throw new IllegalArgumentException("Датчик не может быть null");
        }
        if (findById(s.getId()) != null) {
            throw new DuplicateSensorException(s.getId());
        }
        if (items.size() >= capacity) {
            throw new StorageFullException(capacity);
        }
        items.add(s);
    }

    @Override
    public Sensor getById(int id) throws SensorNotFoundException {
        Sensor found = findById(id);
        if (found == null) {
            throw new SensorNotFoundException(id);
        }
        return found;
    }
}
