package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Стресс-проверка потокобезопасности (для отчёта по лабораторной № 7).
 * Запуск: java -cp target/classes ru.miet.osmsensors.model.ConcurrencyCheck
 */
public class ConcurrencyCheck {
    private static final int WRITERS = 8;
    private static final int WRITES_PER_THREAD = 20000;
    private static final int IDS = 10;

    public static void main(String[] args) throws InterruptedException {
        SensorStorage storage = new SensorStorage(50);
        AtomicInteger added = new AtomicInteger();
        AtomicLong stale = new AtomicLong();
        AtomicInteger failures = new AtomicInteger();

        List<Thread> threads = new ArrayList<>();
        for (int w = 0; w < WRITERS; w++) {
            final int seed = w;
            Thread t = new Thread(() -> {
                try {
                    for (int i = 0; i < WRITES_PER_THREAD; i++) {
                        int id = (i + seed) % IDS;
                        Sensor s = new TemperatureSensor(id, 1, 1, 5, i, "OK");
                        WriteResult r = storage.upsert(s, System.currentTimeMillis());
                        if (r == WriteResult.ADDED) added.incrementAndGet();
                        if (r == WriteResult.STALE_IGNORED) stale.incrementAndGet();
                        if (i % 100 == 0) storage.getAll();
                    }
                } catch (RuntimeException e) {
                    failures.incrementAndGet();
                    e.printStackTrace();
                }
            }, "writer-" + w);
            threads.add(t);
        }

        long start = System.nanoTime();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long ms = (System.nanoTime() - start) / 1_000_000;

        long totalWrites = (long) WRITERS * WRITES_PER_THREAD;
        boolean ok = failures.get() == 0
                && storage.getCount() == IDS
                && added.get() == IDS
                && storage.getCollisionCount() == totalWrites - IDS;

        System.out.println("Потоков-писателей: " + WRITERS + ", записей всего: " + totalWrites + ", время: " + ms + " мс");
        System.out.println("Датчиков в хранилище: " + storage.getCount() + " (ожидалось " + IDS + ")");
        System.out.println("Добавлений (ADDED): " + added.get() + " (ожидалось " + IDS + ")");
        System.out.println("Коллизий по id: " + storage.getCollisionCount() + ", из них отброшено как устаревшие: " + stale.get());
        System.out.println("Исключений в потоках: " + failures.get());
        System.out.println(ok ? "РЕЗУЛЬТАТ: OK, данные не повреждены" : "РЕЗУЛЬТАТ: ОШИБКА");
    }
}
