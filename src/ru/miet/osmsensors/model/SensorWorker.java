package ru.miet.osmsensors.model;

import java.util.Random;

/**
 * Фоновый источник показаний: в цикле имитирует поток данных одного датчика
 * и пишет их в потокобезопасное хранилище через upsert().
 * Запуск: new Thread(worker, "sensor-" + id).start();
 * Задержку между показаниями можно менять на лету (setDelay), остановка - stop().
 */
public class SensorWorker implements Runnable {

    /** Подписчик на новые показания (GUI должен переключаться в EDT через SwingUtilities.invokeLater). */
    public interface ReadingListener {
        void onReading(Sensor sensor, WriteResult result);
    }

    public static final long MIN_DELAY_MS = 10;

    private final SensorStorage storage;
    private final Sensor template;
    private final Random random = new Random();

    private volatile long delayMs;
    private volatile boolean running = true;
    private volatile ReadingListener listener;
    private volatile Thread runner;

    private double current;

    /**
     * @param storage  общее потокобезопасное хранилище
     * @param template датчик-образец: определяет id, тип, координаты, радиус и начальное значение
     * @param delayMs  задержка между показаниями, мс
     */
    public SensorWorker(SensorStorage storage, Sensor template, long delayMs) {
        if (storage == null || template == null) {
            throw new IllegalArgumentException("storage и template не могут быть null");
        }
        this.storage = storage;
        this.template = template;
        this.current = template.getValue();
        setDelay(delayMs);
    }

    public void setDelay(long delayMs) {
        this.delayMs = Math.max(MIN_DELAY_MS, delayMs);
    }

    public long getDelay() {
        return delayMs;
    }

    public int getSensorId() {
        return template.getId();
    }

    public void setListener(ReadingListener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    /** Просит воркер остановиться и прерывает текущий sleep. */
    public void stop() {
        running = false;
        Thread t = runner;
        if (t != null) {
            t.interrupt();
        }
    }

    @Override
    public void run() {
        runner = Thread.currentThread();
        while (running && !Thread.currentThread().isInterrupted()) {
            Sensor reading = nextReading();
            WriteResult result = storage.upsert(reading, System.currentTimeMillis());

            ReadingListener l = listener;
            if (l != null) {
                try {
                    l.onReading(reading, result);
                } catch (RuntimeException ignored) {
                    // ошибка подписчика не должна останавливать источник данных
                }
            }

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running = false;
    }

    private Sensor nextReading() {
        current += (random.nextDouble() - 0.5) * 4.0;
        double p = random.nextDouble();
        String status = p < 0.02 ? "ERROR" : (p < 0.10 ? "WARNING" : "OK");
        int id = template.getId();
        double x = template.getX();
        double y = template.getY();
        double r = template.getRadius();
        switch (template.getType().toUpperCase()) {
            case "CO2": return new Co2Sensor(id, x, y, r, current, status);
            case "N2":  return new GasSensor(id, x, y, r, current, status);
            default:    return new TemperatureSensor(id, x, y, r, current, status);
        }
    }
}
