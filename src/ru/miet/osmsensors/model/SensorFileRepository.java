package ru.miet.osmsensors.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CSV: id,type,x,y,radius,value,status; x=longitude, y=latitude, radius=metres.
 * All file records use SensorValidator, including reads and writes.
 * Methods serialize operations through this instance, not across processes or
 * separate instances. This is a file repository, not a multi-process database.
 */
public class SensorFileRepository {
    public static final String HEADER = "id,type,x,y,radius,value,status";
    private final Path file;

    public SensorFileRepository(Path file) {
        if (file == null) throw new IllegalArgumentException("Path must not be null");
        this.file = file;
    }

    public Path getFile() { return file; }

    public synchronized void saveAll(SensorStorage storage) throws IOException {
        if (storage == null) throw new IllegalArgumentException("Storage must not be null");
        List<Sensor> sensors = new ArrayList<>();
        for (Sensor s : storage.getAll()) sensors.add(s);
        writeAll(sensors);
    }

    /** Atomic append, preserving the old loadInto meaning. Missing file is a no-op. */
    public synchronized void loadInto(SensorStorage storage)
            throws IOException, SensorFileFormatException, DuplicateSensorException, StorageFullException {
        if (storage == null) throw new IllegalArgumentException("Storage must not be null");
        storage.addAllUnique(readAll());
    }

    /**
     * Atomic replacement API for controllers. A missing file is an error, not an
     * instruction to clear storage. A valid empty file intentionally clears it.
     * Controllers must call this instead of deleting and adding one by one.
     */
    public synchronized void loadReplacingInto(SensorRepository storage)
            throws IOException, SensorFileFormatException, DuplicateSensorException, StorageFullException {
        if (storage == null) throw new IllegalArgumentException("Storage must not be null");
        storage.replaceAllValidated(readAll(false));
    }

    public synchronized List<Sensor> readAll() throws IOException, SensorFileFormatException {
        return readAll(true);
    }

    private List<Sensor> readAll(boolean missingIsEmpty) throws IOException, SensorFileFormatException {
        List<Sensor> sensors = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            boolean firstRecord = true;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) line = line.substring(1);
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (firstRecord && HEADER.equalsIgnoreCase(line)) {
                    firstRecord = false;
                    continue;
                }
                firstRecord = false;
                Sensor s = parse(line, lineNumber);
                if (!ids.add(s.getId()))
                    throw new SensorFileFormatException(lineNumber, "Duplicate sensor ID: " + s.getId());
                sensors.add(s);
            }
        } catch (NoSuchFileException e) {
            if (!missingIsEmpty) throw e;
        }
        return sensors;
    }

    public synchronized void add(Sensor sensor)
            throws IOException, SensorFileFormatException, DuplicateSensorException {
        Sensor s = requireCopy(sensor);
        List<Sensor> sensors = readAll();
        ensureIdIsFree(sensors, s.getId());
        sensors.add(s);
        writeAll(sensors);
    }

    public synchronized void update(int id, Sensor updatedSensor)
            throws IOException, SensorFileFormatException, SensorNotFoundException, DuplicateSensorException {
        Sensor updated = requireCopy(updatedSensor);
        List<Sensor> sensors = readAll();
        int index = indexOf(sensors, id);
        if (index < 0) throw new SensorNotFoundException(id);
        if (updated.getId() != id) ensureIdIsFree(sensors, updated.getId());
        sensors.set(index, updated);
        writeAll(sensors);
    }

    public synchronized void delete(int id)
            throws IOException, SensorFileFormatException, SensorNotFoundException {
        List<Sensor> sensors = readAll();
        int index = indexOf(sensors, id);
        if (index < 0) throw new SensorNotFoundException(id);
        sensors.remove(index);
        writeAll(sensors);
    }

    /**
     * Write a sibling temporary file, close it, then atomically replace the target.
     * No unsafe truncate or non-atomic fallback. If ATOMIC_MOVE is unsupported,
     * saving fails with IOException rather than deliberately overwriting in place.
     * This protects replacement integrity, not durability across power failure.
     */
    private void writeAll(List<Sensor> sensors) throws IOException {
        List<String> lines = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (Sensor source : sensors) {
            Sensor s = requireCopy(source);
            try {
                s = SensorValidator.createSensor(Integer.toString(s.getId()),
                        Double.toString(s.getX()), Double.toString(s.getY()),
                        Double.toString(s.getRadius()), Double.toString(s.getValue()),
                        s.getType(), s.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid sensor data: " + e.getMessage(), e);
            }
            if (!ids.add(s.getId())) throw new IOException("Duplicate sensor ID: " + s.getId());
            lines.add(s.getId() + "," + s.getType() + "," + s.getX() + "," + s.getY()
                    + "," + s.getRadius() + "," + s.getValue() + "," + s.getStatus());
        }
        Path target = file.toAbsolutePath();
        Path parent = target.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, ".sensor-", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Sensor parse(String line, int lineNumber) throws SensorFileFormatException {
        String[] fields = line.split(",", -1);
        if (fields.length != 7)
            throw new SensorFileFormatException(lineNumber, "Expected 7 fields, got " + fields.length);
        try {
            return SensorValidator.createSensor(fields[0], fields[2], fields[3],
                    fields[4], fields[5], fields[1], fields[6]);
        } catch (IllegalArgumentException e) {
            throw new SensorFileFormatException(lineNumber, e.getMessage(), e);
        }
    }

    private Sensor requireCopy(Sensor sensor) {
        if (sensor == null) throw new IllegalArgumentException("Sensor must not be null");
        return sensor.copy();
    }

    private void ensureIdIsFree(List<Sensor> sensors, int id) throws DuplicateSensorException {
        if (indexOf(sensors, id) >= 0) throw new DuplicateSensorException(id);
    }

    private int indexOf(List<Sensor> sensors, int id) {
        for (int i = 0; i < sensors.size(); i++) if (sensors.get(i).getId() == id) return i;
        return -1;
    }
}
