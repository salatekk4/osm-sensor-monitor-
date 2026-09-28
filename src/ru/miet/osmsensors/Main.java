package ru.miet.osmsensors;

import ru.miet.osmsensors.model.*;

public class Main {
    public static void main(String[] args) {

        // Создаём датчики через наследников
        Sensor s1 = new TemperatureSensor(1, 10.0, 20.0, 5.0, 55.0, "ERROR");  // тревога!
        Sensor s2 = new TemperatureSensor(2, 11.0, 21.0, 5.0, 22.0, "OK");     // норма
        Sensor s3 = new Co2Sensor(3, 12.0, 22.0, 4.0, 1200.0, "WARNING");      // тревога!
        Sensor s4 = new Co2Sensor(4, 13.0, 23.0, 4.0, 800.0, "OK");            // норма
        Sensor s5 = new GasSensor(5, 14.0, 24.0, 3.0, 85.0, "WARNING");        // тревога!

        Sensor[] sensors = {s1, s2, s3, s4, s5};

        // --- 1. Полиморфный вывод toString() ---
        System.out.println("=== Все датчики ===");
        for (Sensor s : sensors) {
            System.out.println(s);
        }

        // --- 2. Статистика: статический счетчик ---
        System.out.println("\n=== Статистика ===");
        System.out.println("Всего создано датчиков: " + Sensor.getSensorCount());

        // --- 3. Аварийные датчики (isAlarm) ---
        System.out.println("\n=== Датчики в аварийном состоянии ===");
        for (Sensor s : sensors) {
            if (s.isAlarm()) {
                System.out.println("  [!] " + s);
            }
        }

        // --- 4. Проверка equals и hashCode ---
        System.out.println("\n=== Проверка equals / hashCode ===");
        Sensor copy = new TemperatureSensor(1, 99.0, 99.0, 99.0, 99.0, "ERROR"); // тот же id=1
        System.out.println("s1.equals(copy) [ID одинаковый]:     " + s1.equals(copy));   // true
        System.out.println("s1.equals(s2)   [ID разные]:          " + s1.equals(s2));    // false
        System.out.println("hashCode s1 == hashCode copy: " + (s1.hashCode() == copy.hashCode())); // true
        System.out.println("Общий счетчик после copy: " + Sensor.getSensorCount()); // 6
    }
}
