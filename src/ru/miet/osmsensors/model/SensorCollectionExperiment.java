package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Эксперимент ЛР №3: сравнение ArrayList и LinkedList.
 * Для каждой операции add/remove замеряется время через System.nanoTime().
 * Запись в файл здесь не выполняется - результат возвращается вызывающему коду.
 */
public class SensorCollectionExperiment {
    public static final int[] SIZES = {10, 100, 1_000, 10_000, 100_000};
    private static final double REMOVE_FRACTION = 0.10;

    private final long seed;
    private boolean warmedUp = false;

    public SensorCollectionExperiment() {
        this(System.nanoTime());
    }

    /** Фиксированный seed позволяет повторить эксперимент с теми же данными. */
    public SensorCollectionExperiment(long seed) {
        this.seed = seed;
    }

    public ExperimentResult runArrayListExperiment(int size) {
        return run("ArrayList", new ArrayList<Sensor>(), size);
    }

    public ExperimentResult runLinkedListExperiment(int size) {
        return run("LinkedList", new LinkedList<Sensor>(), size);
    }

    /** Запускает эксперимент для обеих коллекций и всех пяти размеров (10 результатов). */
    public List<ExperimentResult> runAll() {
        List<ExperimentResult> results = new ArrayList<>();
        for (int size : SIZES) {
            results.add(runArrayListExperiment(size));
            results.add(runLinkedListExperiment(size));
        }
        return results;
    }

    private ExperimentResult run(String name, List<Sensor> list, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Размер должен быть положительным: " + size);
        }
        warmUp();
        return measure(name, list, size);
    }

    private ExperimentResult measure(String name, List<Sensor> list, int size) {
        Random random = new Random(seed + size);
        ExperimentResult result = new ExperimentResult(name, size);

        // Датчики создаются заранее, чтобы создание объектов не попало в замер add
        Sensor[] sensors = new SensorFactory(random).createSensors(size);

        // Добавление всех объектов
        for (int i = 0; i < size; i++) {
            Sensor s = sensors[i];
            long start = System.nanoTime();
            list.add(s);
            long duration = System.nanoTime() - start;
            result.addMeasurement(new OperationMeasurement(
                    OperationMeasurement.ADD, i, s.getId(), duration));
        }

        // Удаление 10% объектов по случайным индексам
        int removeCount = Math.max(1, (int) (size * REMOVE_FRACTION));
        for (int i = 0; i < removeCount; i++) {
            int index = random.nextInt(list.size());
            long start = System.nanoTime();
            Sensor removed = list.remove(index);
            long duration = System.nanoTime() - start;
            result.addMeasurement(new OperationMeasurement(
                    OperationMeasurement.REMOVE, index, removed.getId(), duration));
        }
        return result;
    }

    /** Небольшой прогрев JIT на отбрасываемых данных, чтобы первый реальный замер был честнее. */
    private void warmUp() {
        if (warmedUp) {
            return;
        }
        warmedUp = true;
        measure("warmup", new ArrayList<Sensor>(), 2_000);
        measure("warmup", new LinkedList<Sensor>(), 2_000);
    }
}
