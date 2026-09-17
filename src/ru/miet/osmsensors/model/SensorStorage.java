package ru.miet.osmsensors.model;

import java.util.Arrays;

public class SensorStorage {
    private final Sensor[] items;
    private int count;

    public SensorStorage() {
        this(20); // размер по умолчанию
    }

    public SensorStorage(int capacity) {
        this.items = new Sensor[capacity];
        this.count = 0;
    }

    /**
     * Добавление датчика в хранилище.
     * @return true, если датчик успешно добавлен; false, если массив переполнен
     */
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
    public Sensor findById(int id) {
        for (int i = 0; i < count; i++) {
            if (items[i].getId() == id) {
                return items[i];
            }
        }
        return null;
    }

    /**
     * Фильтрация датчиков по типу (например: "T", "CO2", "N2").
     * @return новый массив Sensor[] точной длины с подходящими датчиками
     */
    public Sensor[] findByType(String type) {
        if (type == null) {
            return new Sensor[0];
        }

        // 1. Считаем количество подходящих элементов
        int matchCount = 0;
        for (int i = 0; i < count; i++) {
            if (type.equalsIgnoreCase(items[i].getType())) {
                matchCount++;
            }
        }

        // 2. Создаем результирующий массив точной длины и заполняем его
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
     * Получить все добавленные датчики (массив точной длины без пустых null-ячеек)
     */
    public Sensor[] getAll() {
        return Arrays.copyOf(items, count);
    }

    public int getCount() {
        return count;
    }

    public int getCapacity() {
        return items.length;
    }
}
