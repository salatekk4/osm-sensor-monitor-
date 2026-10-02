package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Хранилище датчиков на коллекциях.
 * List сохраняет порядок добавления, Map обеспечивает быстрый поиск по ID.
 * Лимита на количество датчиков нет.
 */
public class SensorStorage implements SensorRepository {
    private final List<Sensor> items = new ArrayList<>();
    private final Map<Integer, Sensor> byId = new HashMap<>();

    public SensorStorage() {
    }

    /** Оставлен для совместимости со старым кодом; параметр игнорируется. */
    public SensorStorage(int ignoredCapacity) {
        this();
    }

    /**
     * @return true, если датчик добавлен; false, если s == null
     * @throws DuplicateSensorException если датчик с таким ID уже есть
     */
    @Override
    public boolean add(Sensor s) {
        if (s == null) {
            return false;
        }
        if (byId.containsKey(s.getId())) {
            throw new DuplicateSensorException(s.getId());
        }
        items.add(s);
        byId.put(s.getId(), s);
        return true;
    }

    /** @throws SensorNotFoundException если датчика с таким ID нет */
    @Override
    public Sensor findById(int id) {
        Sensor s = byId.get(id);
        if (s == null) {
            throw new SensorNotFoundException(id);
        }
        return s;
    }

    /**
     * Удаляет датчик по ID.
     * @return удалённый датчик
     * @throws SensorNotFoundException если датчика с таким ID нет
     */
    @Override
    public Sensor removeById(int id) {
        Sensor s = byId.remove(id);
        if (s == null) {
            throw new SensorNotFoundException(id);
        }
        items.remove(s);
        return s;
    }

    @Override
    public Sensor[] findByType(String type) {
        List<Sensor> result = new ArrayList<>();
        if (type != null) {
            for (Sensor s : items) {
                if (type.equalsIgnoreCase(s.getType())) {
                    result.add(s);
                }
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

    /** Лимита больше нет. Оставлен только чтобы не ломать старый код. */
    @Override
    @Deprecated
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }
}
