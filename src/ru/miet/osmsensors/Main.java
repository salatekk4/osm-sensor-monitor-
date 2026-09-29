package ru.miet.osmsensors;

import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.view.ConsoleView;
import ru.miet.osmsensors.controller.AppController;

public class Main {
    private static final int DEFAULT_LIMIT = 20;

    public static void main(String[] args) {
        boolean demoMode = false;
        int limit = DEFAULT_LIMIT;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--demo")) {
                demoMode = true;
            } else if (arg.equals("--limit")) {
                try {
                    limit = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (NumberFormatException e) {
                    System.out.println("Некорректное значение --limit");
                    limit = DEFAULT_LIMIT;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("После --limit не указано число");
                    limit = DEFAULT_LIMIT;
                }
            }
        }

        System.out.println("Запуск приложения. Демо-режим: " + demoMode + ", " + " limit: " + limit);

        ConsoleView view = new ConsoleView();
        SensorRepository storage = new SensorStorage(limit);
        AppController controller = new AppController(storage, view);
        if (demoMode) {
            controller.initDefaultData(limit);

            demonstrateEqualsandHashCode(storage);
        }
        controller.startInteractiveLoop();
    }

    private static void demonstrateEqualsandHashCode(SensorRepository storage) {
        Sensor[] all = storage.getAll();
        if (all.length == 0) {
            System.out.println("Демо equals()/hashCode() пропущено: хранилище пустое.");
            return;
        }

        Sensor original = all[0];
        Sensor duplicateById = new TemperatureSensor(original.getId(), 0, 0, 0, 0, "OK");

        System.out.println("Проверка equals()/hashCode()");
        System.out.println("Реальный датчик из storage, id=" + original.getId()
                + ", и новый объект с тем же id, но другими полями:");
        System.out.println("equals(): " + original.equals(duplicateById));
        System.out.println("hashCode совпадает: " + (original.hashCode() == duplicateById.hashCode()));

        if (all.length > 1) {
            Sensor another = all[1];
            System.out.println("Для контраста — два РАЗНЫХ реальных датчика из storage (id "
                    + original.getId() + " и " + another.getId() + "):");
            System.out.println("equals(): " + original.equals(another));
        }
    }
}
