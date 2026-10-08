package ru.miet.osmsensors.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Файловая «база данных» датчиков в CSV-формате.
 * Формат строки: id,type,x,y,radius,value,status.
 * Первая строка файла - заголовок.
 */
public class SensorFileRepository {
    public static final String HEADER = "id,type,x,y,radius,value,status";

    private final Path file;

    public SensorFileRepository(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("Путь к файлу не может быть null");
        }
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /** Полностью перезаписывает файл содержимым хранилища. */
    public void saveAll(SensorStorage storage) throws IOException {
        if (storage == null) {
            throw new IllegalArgumentException("Хранилище не может быть null");
        }
        List<Sensor> sensors = new ArrayList<Sensor>();
        for (Sensor s : storage.getAll()) {
            sensors.add(s);
        }
        writeAll(sensors);
    }

    /** Читает файл и добавляет датчики в хранилище. Если файла нет, ничего не делает. */
    public void loadInto(SensorStorage storage)
            throws IOException, SensorFileFormatException, DuplicateSensorException, StorageFullException {
        if (storage == null) {
            throw new IllegalArgumentException("Хранилище не может быть null");
        }
        for (Sensor sensor : readAll()) {
            storage.addUnique(sensor);
        }
    }

    /** Читает список всех датчиков из файла. Если файла нет, возвращает пустой список. */
    public List<Sensor> readAll() throws IOException, SensorFileFormatException {
        List<Sensor> sensors = new ArrayList<Sensor>();
        if (!Files.exists(file)) {
            return sensors;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")
                        || (lineNumber == 1 && HEADER.equalsIgnoreCase(line))) {
                    continue;
                }
                sensors.add(parse(line, lineNumber));
            }
        }
        return sensors;
    }

    /** Добавляет датчик в файл; id должен быть уникальным. */
    public void add(Sensor sensor) throws IOException, SensorFileFormatException, DuplicateSensorException {
        requireSensor(sensor);
        List<Sensor> sensors = readAll();
        ensureIdIsFree(sensors, sensor.getId());
        sensors.add(sensor);
        writeAll(sensors);
    }

    /** Заменяет данные датчика с указанным id. */
    public void update(int id, Sensor updatedSensor)
            throws IOException, SensorFileFormatException, SensorNotFoundException, DuplicateSensorException {
        requireSensor(updatedSensor);
        List<Sensor> sensors = readAll();
        int index = indexOf(sensors, id);
        if (index < 0) {
            throw new SensorNotFoundException(id);
        }
        if (updatedSensor.getId() != id) {
            ensureIdIsFree(sensors, updatedSensor.getId());
        }
        sensors.set(index, updatedSensor);
        writeAll(sensors);
    }

    /** Удаляет датчик с указанным id. */
    public void delete(int id) throws IOException, SensorFileFormatException, SensorNotFoundException {
        List<Sensor> sensors = readAll();
        int index = indexOf(sensors, id);
        if (index < 0) {
            throw new SensorNotFoundException(id);
        }
        sensors.remove(index);
        writeAll(sensors);
    }

    private void writeAll(List<Sensor> sensors) throws IOException {
        // Сначала формируем все строки: при ошибке данных файл не будет испорчен.
        List<String> lines = new ArrayList<String>();
        for (Sensor sensor : sensors) {
            lines.add(toCsv(sensor));
        }
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            writer.write(HEADER);
            writer.newLine();
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private Sensor parse(String line, int lineNumber) throws SensorFileFormatException {
        String[] fields = line.split(",", -1);
        if (fields.length != 7) {
            throw new SensorFileFormatException(lineNumber,
                    "ожидалось 7 полей, получено " + fields.length);
        }
        try {
            int id = Integer.parseInt(fields[0].trim());
            String type = fields[1].trim();
            double x = Double.parseDouble(fields[2].trim());
            double y = Double.parseDouble(fields[3].trim());
            double radius = Double.parseDouble(fields[4].trim());
            double value = Double.parseDouble(fields[5].trim());
            String status = fields[6].trim();
            if (status.isEmpty()) {
                throw new SensorFileFormatException(lineNumber, "статус не может быть пустым");
            }
            return createSensor(type, id, x, y, radius, value, status, lineNumber);
        } catch (NumberFormatException e) {
            throw new SensorFileFormatException(lineNumber, "некорректное число", e);
        }
    }

    private Sensor createSensor(String type, int id, double x, double y, double radius,
                                double value, String status, int lineNumber)
            throws SensorFileFormatException {
        if ("T".equalsIgnoreCase(type)) {
            return new TemperatureSensor(id, x, y, radius, value, status);
        }
        if ("CO2".equalsIgnoreCase(type)) {
            return new Co2Sensor(id, x, y, radius, value, status);
        }
        if ("N2".equalsIgnoreCase(type)) {
            return new GasSensor(id, x, y, radius, value, status);
        }
        throw new SensorFileFormatException(lineNumber, "неизвестный тип датчика: " + type);
    }

    private String toCsv(Sensor sensor) {
        String status = sensor.getStatus();
        if (status == null || status.trim().isEmpty() || status.indexOf(',') >= 0
                || status.indexOf('\n') >= 0 || status.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(
                    "Статус датчика должен быть непустым и без запятых и переносов строк");
        }
        return sensor.getId() + "," + sensor.getType() + "," + sensor.getX() + ","
                + sensor.getY() + "," + sensor.getRadius() + "," + sensor.getValue()
                + "," + status.trim();
    }

    private void requireSensor(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Датчик не может быть null");
        }
    }

    private void ensureIdIsFree(List<Sensor> sensors, int id) throws DuplicateSensorException {
        if (indexOf(sensors, id) >= 0) {
            throw new DuplicateSensorException(id);
        }
    }

    private int indexOf(List<Sensor> sensors, int id) {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
}
