package ru.miet.osmsensors.model;

import java.util.Arrays;

public class SensorStorage implements SensorRepository {
    private final Sensor[] items;
    private int count;

    public SensorStorage() {
        this(20);
    }

    public SensorStorage(int capacity) {
        this.items = new Sensor[capacity];
        this.count = 0;
    }

    /**
     * Добавление датчика в хранилище.
     * @return true, если датчик успешно добавлен; false, если массив переполнен или s == null
     */
    @Override
    public boolean add(Sensor s) {
        if (s == null || count >= items.length) {
            return false;
        }
        items[count] = s;
        count++;
        return true;
    }

    /**
     * Поиск датчика по ID перебором элементов.
     * @return найденный Sensor или null, если не найден
     */
    @Override
    public Sensor findById(int id) {
        for (int i = 0; i < count; i++) {
            if (items[i].getId() == id) {
                return items[i];
            }
        }
        return null;
    }

    /**
     * Фильтрация датчиков по типу ("T", "CO2", "N2").
     * @return новый массив Sensor[] точной длины; пустой массив, если совпадений нет
     */
    @Override
    public Sensor[] findByType(String type) {
        if (type == null) {
            return new Sensor[0];
        }
        int matchCount = 0;
        for (int i = 0; i < count; i++) {
            if (type.equalsIgnoreCase(items[i].getType())) {
                matchCount++;
            }
        }
        Sensor[] result = new Sensor[matchCount];
        int index = 0;
        for (int i = 0; i < count; i++) {
            if (type.equalsIgnoreCase(items[i].getType())) {
                result[index++] = items[i];
            }
        }
        return result;
    }

    /**
     * Получить все аварийные датчики (isAlarm() == true).
     * @return новый массив Sensor[] точной длины; пустой массив, если аварийных нет
     */
    @Override
    public Sensor[] findAlarmSensors() {
        // 1. Считаем количество аварийных датчиков
        int alarmCount = 0;
        for (int i = 0; i < count; i++) {
            if (items[i].isAlarm()) {
                alarmCount++;
            }
        }
        // 2. Создаём массив точной длины и заполняем его
        Sensor[] result = new Sensor[alarmCount];
        int index = 0;
        for (int i = 0; i < count; i++) {
            if (items[i].isAlarm()) {
                result[index++] = items[i];
            }
        }
        return result;
    }

    /**
     * Получить все добавленные датчики (массив точной длины без пустых null-ячеек)
     */
    @Override
    public Sensor[] getAll() {
        return Arrays.copyOf(items, count);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getCapacity() {
        return items.length;
    }
}
