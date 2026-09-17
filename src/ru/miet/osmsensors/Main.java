package ru.miet.osmsensors;

import ru.miet.osmsensors.model.Sensor;
import ru.miet.osmsensors.model.SensorStorage;

public class Main {
    public static void main(String[] args) {
        // Создаем хранилище вместимостью до 10 датчиков
        SensorStorage storage = new SensorStorage(10);

        // Добавляем тестовые датчики
        storage.add(new Sensor(1, 12.5, 45.1, 5.0, 23.4, "T", "OK"));
        storage.add(new Sensor(2, 14.0, 48.2, 8.0, 412.0, "CO2", "WARNING"));
        storage.add(new Sensor(3, 11.2, 43.0, 4.0, 26.1, "T", "ERROR"));

        // 1. Проверяем поиск по ID
        System.out.println("--- Проверка поиска по ID ---");
        Sensor found = storage.findById(2);
        if (found != null) {
            System.out.println("Найден: " + found.toFormattedString());
        } else {
            System.out.println("Датчик не найден");
        }

        // 2. Проверяем фильтрацию по типу
        System.out.println("\n--- Проверка фильтрации по типу 'T' ---");
        Sensor[] tempSensors = storage.findByType("T");
        for (Sensor s : tempSensors) {
            System.out.println(s.toFormattedString());
        }
    }
}
