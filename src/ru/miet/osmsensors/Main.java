package ru.miet.osmsensors;

import java.util.List;

import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.view.ConsoleView;
import ru.miet.osmsensors.controller.AppController;
import ru.miet.osmsensors.view.AppLogger;

public class Main {
    private static final int DEFAULT_LIMIT = 20;

    private static final int DEFAULT_DEMO_COUNT = 10;

    private static final long BENCHMARK_SEED = 12345L;

    public static void main(String[] args) {
        boolean demoMode = false;
        boolean benchmarkMode = false;
        int limit = DEFAULT_LIMIT;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--demo")) {
                demoMode = true;
            } else if (arg.equals("--benchmark")) {
                benchmarkMode = true;
            } else if (arg.equals("--limit"))
                try {
                    int value = Integer.parseInt(args[i + 1]);
                    i++;
                    if (value < 0) {
                        System.out.println("--limit не может быть отрицательным, использую значение по умолчанию: " + DEFAULT_LIMIT);
                    } else {
                        limit = value;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Некорректное значение --limit");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("После --limit не указано число");
                }
            }

        if (benchmarkMode) {
            runBenchmark();
            return;
        }

        System.out.println("Запуск приложения. Демо-режим: " + demoMode + ", " + " limit: " + limit);

        ConsoleView view = new ConsoleView();
        SensorRepository storage = new SensorStorage(limit);
        AppController controller = new AppController(storage, view, logger);

        if (demoMode) {
            controller.initDefaultData(Math.min(limit, DEFAULT_DEMO_COUNT));

            demonstrateEqualsAndHashCode(storage);
        }
        controller.startInteractiveLoop();
    }
    private static void runBenchmark() {
        System.out.println("Режим --benchmark: эксперимент ArrayList vs LinkedList (может занять несколько секунд)...");

        List<ExperimentResult> results = new SensorCollectionExperiment(BENCHMARK_SEED).runAll();

        System.out.println();
        System.out.println(String.format("%-8s | %-10s | %16s | %16s | %16s | %16s",
                "Размер", "Коллекция", "add всего, мс", "add среднее, нс", "remove всего, мс", "remove средн., нс"));
        System.out.println("---------+------------+------------------+------------------+------------------+------------------");
        for (ExperimentResult r : results) {
            System.out.println(String.format("%-8d | %-10s | %16.3f | %16.1f | %16.3f | %16.1f",
                    r.getSize(), r.getCollectionName(),
                    r.getTotalAddNanos() / 1_000_000.0, r.getAverageAddNanos(),
                    r.getTotalRemoveNanos() / 1_000_000.0, r.getAverageRemoveNanos()));
        }
        System.out.println();
        System.out.println("Время зависит от компьютера и загрузки процессора — для выводов запусти несколько раз.");
    }

    private static Sensor sameClassWithSameId(Sensor original) {
        int id = original.getId();
        switch (original.getType()) {
            case "CO2":
                return new Co2Sensor(id, 0, 0, 0, 0, "OK");
            case "N2":
                return new GasSensor(id, 0, 0, 0, 0, "OK");
            default:
                return new TemperatureSensor(id, 0, 0, 0, 0, "OK");
        }
    }

    private static void demonstrateEqualsAndHashCode(SensorRepository storage) {
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
