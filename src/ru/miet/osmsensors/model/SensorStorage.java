package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Потокобезопасное хранилище датчиков на ArrayList.
 * Все публичные методы synchronized (монитор - сам объект хранилища), поэтому
 * одновременная запись из нескольких потоков не портит внутреннюю структуру.
 * Составные операции (проверка id + добавление, поиск + замена) выполняются
 * атомарно в рамках одного вызова.
 *
 * Разрешение коллизий при записи в один id - upsert(): побеждает показание
 * с большей меткой времени (last-write-wins по timestamp); более старое отбрасывается.
 * Датчики не изменяются на месте, а заменяются новым объектом, поэтому массив,
 * возвращённый getAll(), остаётся согласованным снимком для GUI.
 */
public class SensorStorage implements SensorRepository {
    private final List<Sensor> items;
    private final int capacity;
    private final Map<Integer, Long> lastWrite = new HashMap<>();
    private long collisionCount = 0;

    public SensorStorage() {
        this(20);
    }

    public SensorStorage(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>(Math.max(capacity, 0));
    }

    @Override
    public synchronized boolean add(Sensor s) {
        if (s == null || items.size() >= capacity) {
            return false;
        }
        items.add(s);
        lastWrite.put(s.getId(), System.currentTimeMillis());
        return true;
    }

    @Override
    public synchronized Sensor findById(int id) {
        for (Sensor s : items) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    @Override
    public synchronized Sensor[] findByType(String type) {
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
    public synchronized Sensor[] findAlarmSensors() {
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : items) {
            if (s.isAlarm()) {
                result.add(s);
            }
        }
        return result.toArray(new Sensor[0]);
    }

    @Override
    public synchronized Sensor[] getAll() {
        return items.toArray(new Sensor[0]);
    }

    @Override
    public synchronized int getCount() {
        return items.size();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public synchronized void addUnique(Sensor s) throws DuplicateSensorException, StorageFullException {
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
        lastWrite.put(s.getId(), System.currentTimeMillis());
    }

    @Override
    public synchronized Sensor getById(int id) throws SensorNotFoundException {
        Sensor found = findById(id);
        if (found == null) {
            throw new SensorNotFoundException(id);
        }
        return found;
    }

    @Override
    public synchronized void updateById(int id, Sensor updatedSensor)
            throws SensorNotFoundException, DuplicateSensorException {
        if (updatedSensor == null) {
            throw new IllegalArgumentException("Датчик не может быть null");
        }
        int index = indexOf(id);
        if (index < 0) {
            throw new SensorNotFoundException(id);
        }
        if (updatedSensor.getId() != id && findById(updatedSensor.getId()) != null) {
            throw new DuplicateSensorException(updatedSensor.getId());
        }
        items.set(index, updatedSensor);
        lastWrite.remove(id);
        lastWrite.put(updatedSensor.getId(), System.currentTimeMillis());
    }

    @Override
    public synchronized void deleteById(int id) throws SensorNotFoundException {
        int index = indexOf(id);
        if (index < 0) {
            throw new SensorNotFoundException(id);
        }
        items.remove(index);
        lastWrite.remove(id);
    }

    /**
     * Атомарная запись показания: добавляет датчик, если id свободен, иначе
     * разрешает коллизию по метке времени (побеждает более новое показание).
     *
     * @param incoming  новое состояние датчика (не null)
     * @param timestamp время измерения в мс (например, System.currentTimeMillis())
     */
    public synchronized WriteResult upsert(Sensor incoming, long timestamp) {
        if (incoming == null) {
            throw new IllegalArgumentException("Датчик не может быть null");
        }
        int index = indexOf(incoming.getId());
        if (index < 0) {
            if (items.size() >= capacity) {
                return WriteResult.STORAGE_FULL;
            }
            items.add(incoming);
            lastWrite.put(incoming.getId(), timestamp);
            return WriteResult.ADDED;
        }
        collisionCount++;
        Long stored = lastWrite.get(incoming.getId());
        if (stored != null && timestamp < stored) {
            return WriteResult.STALE_IGNORED;
        }
        items.set(index, incoming);
        lastWrite.put(incoming.getId(), timestamp);
        return WriteResult.UPDATED;
    }

    /** Сколько раз запись пришла в уже существующий id (коллизии), с момента создания. */
    public synchronized long getCollisionCount() {
        return collisionCount;
    }

    private int indexOf(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
}
