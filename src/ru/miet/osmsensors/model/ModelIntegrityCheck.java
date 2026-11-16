package ru.miet.osmsensors.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;

/** Standalone regression checks; any failure throws and exits unsuccessfully. */
public final class ModelIntegrityCheck {
    private interface Action { void run() throws Exception; }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    private static void expect(Class<? extends Throwable> type, Action action) throws Exception {
        try {
            action.run();
        } catch (Exception e) {
            if (type.isInstance(e)) return;
            throw new AssertionError("Expected " + type.getSimpleName(), e);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static Sensor sensor(int id) {
        return new TemperatureSensor(id, 37.6, 55.7, 100, 60, "OK");
    }

    private static void generators() throws Exception {
        SensorFactory a = new SensorFactory(new Random(42));
        SensorFactory b = new SensorFactory(new Random(42));
        for (int i = 0; i < 1000; i++) {
            Sensor x = a.createRandomSensor(i), y = b.createRandomSensor(i);
            check(x.getX() == y.getX() && x.getY() == y.getY()
                    && x.getValue() == y.getValue() && x.getType().equals(y.getType())
                    && x.getStatus().equals(y.getStatus()), "seed reproducibility");
            check(SensorValidator.validate(Integer.toString(x.getId()), Double.toString(x.getX()),
                    Double.toString(x.getY()), Double.toString(x.getRadius()),
                    Double.toString(x.getValue()), x.getType(), x.getStatus()).isValid(), "geographic generation");
        }
        Sensor fixed = new SensorFactory(new Random(1), 37, 37, 55, 55, 10, 10).createRandomSensor(1);
        check(fixed.getX() == 37 && fixed.getY() == 55 && fixed.getRadius() == 10, "fixed bounds");
        expect(IllegalArgumentException.class, () -> a.createSensors(-1));
        expect(IllegalArgumentException.class, () -> new SensorFactory(new Random(), 0, 181, 0, 1, 0, 1));
    }

    private static void storage() throws Exception {
        SensorStorage store = new SensorStorage(3);
        Sensor original = sensor(1);
        store.addUnique(original);
        original.setId(9);
        original.setValue(-999);
        check(store.getById(1).getValue() == 60 && store.findById(9) == null, "input isolation");
        int count = Sensor.getSensorCount();
        store.getById(1).setId(7);
        store.findById(1).setValue(0);
        store.getAll()[0].setValue(0);
        store.findByType("T")[0].setValue(0);
        store.findAlarmSensors()[0].setValue(0);
        check(store.getById(1).getValue() == 60 && Sensor.getSensorCount() == count, "output isolation and copy counter");
        check(!store.add(sensor(1)) && store.getCount() == 1, "legacy add rejects duplicate");
        Sensor replacement = sensor(1);
        replacement.setValue(70);
        store.updateById(1, replacement);
        replacement.setValue(0);
        check(store.getById(1).getValue() == 70, "update isolation");
        Sensor incoming = sensor(2);
        store.upsert(incoming, 100);
        incoming.setId(99);
        check(store.getById(2).getValue() == 60, "upsert isolation");
        expect(DuplicateSensorException.class,
                () -> store.replaceAllValidated(Arrays.asList(sensor(3), sensor(3))));
        expect(StorageFullException.class,
                () -> store.replaceAllValidated(Arrays.asList(sensor(3), sensor(4), sensor(5), sensor(6))));
        Sensor invalid = sensor(4);
        invalid.setY(91);
        expect(IllegalArgumentException.class,
                () -> store.replaceAllValidated(Arrays.asList(sensor(3), invalid)));
        check(store.getCount() == 2 && store.getById(1).getValue() == 70, "replacement rollback");
        check(store.upsert(sensor(2), 99) == WriteResult.STALE_IGNORED, "rollback preserves timestamps");
        expect(DuplicateSensorException.class,
                () -> store.addAllUnique(Arrays.asList(sensor(3), sensor(1))));
        check(store.findById(3) == null, "append rollback");
        Sensor three = sensor(3);
        store.replaceAllValidated(Collections.singletonList(three));
        three.setId(8);
        check(store.getCount() == 1 && store.getById(3) != null, "replacement isolation");
        store.replaceAllValidated(Collections.emptyList());
        check(store.getCount() == 0 && store.upsert(sensor(2), 0) == WriteResult.ADDED, "replacement clears timestamps");
        expect(IllegalArgumentException.class, () -> new SensorStorage(-1));
    }

    private static void csv(Path directory) throws Exception {
        Path path = directory.resolve("sensors.csv");
        SensorFileRepository repository = new SensorFileRepository(path);
        SensorStorage source = new SensorStorage(3);
        source.addUnique(sensor(1));
        source.addUnique(new Co2Sensor(2, 37, 55, 20, 1200, "WARNING"));
        source.addUnique(new GasSensor(3, 37, 55, 20, 90, "ERROR"));
        repository.saveAll(source);
        byte[] before = Files.readAllBytes(path);
        check(repository.readAll().size() == 3, "CSV round trip");
        SensorStorage invalid = new SensorStorage(1);
        Sensor bad = sensor(4);
        bad.setValue(Double.NaN);
        invalid.addUnique(bad);
        expect(IOException.class, () -> repository.saveAll(invalid));
        check(Arrays.equals(before, Files.readAllBytes(path)), "invalid save preserves file");
        SensorStorage small = new SensorStorage(1);
        small.addUnique(sensor(9));
        expect(StorageFullException.class, () -> repository.loadReplacingInto(small));
        check(small.getCount() == 1 && small.getById(9) != null, "CSV replacement capacity rollback");
        SensorStorage append = new SensorStorage(4);
        append.addUnique(sensor(2));
        expect(DuplicateSensorException.class, () -> repository.loadInto(append));
        check(append.getCount() == 1 && append.findById(1) == null, "CSV append rollback");
        SensorStorage loaded = new SensorStorage(3);
        repository.loadReplacingInto(loaded);
        check(loaded.getCount() == 3 && loaded.getById(2) instanceof Co2Sensor, "CSV replacement subtypes");
        String header = SensorFileRepository.HEADER + "\n";
        String[] invalidRows = {
            "1,T,37,91,10,20,OK", "1,T,181,55,10,20,OK", "1,T,37,55,-1,20,OK",
            "1,T,37,55,10,NaN,OK", "1,T,37,55,10,20,BAD", "1,BAD,37,55,10,20,OK",
            "-1,T,37,55,10,20,OK", "1,T,Infinity,55,10,20,OK",
            "1,T,37,55,10,20,OK\n1,CO2,37,55,10,500,OK"
        };
        for (String row : invalidRows) {
            Files.writeString(path, header + row + "\n", StandardCharsets.UTF_8);
            expect(SensorFileFormatException.class, () -> repository.loadReplacingInto(loaded));
            check(loaded.getCount() == 3, "invalid CSV preserves storage");
        }
        Files.writeString(path, "\uFEFF# comment\n\n" + header + "4,t,37,55,10,20,ok\n", StandardCharsets.UTF_8);
        check(repository.readAll().get(0).getStatus().equals("OK"), "BOM, comments and normalization");
        Files.delete(path);
        check(repository.readAll().isEmpty(), "legacy missing file behavior");
        expect(NoSuchFileException.class, () -> repository.loadReplacingInto(loaded));
        check(loaded.getCount() == 3, "missing replacement file preserves storage");
        Files.writeString(path, header, StandardCharsets.UTF_8);
        repository.loadReplacingInto(loaded);
        check(loaded.getCount() == 0, "explicit empty replacement");
        try (Stream<Path> paths = Files.list(directory)) {
            check(paths.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")), "temporary cleanup");
        }
    }

    public static void main(String[] args) throws Exception {
        generators();
        storage();
        Path directory = Files.createTempDirectory("model-integrity-");
        try {
            csv(directory);
        } finally {
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator)
                    Files.deleteIfExists(path);
            }
        }
        System.out.println("ModelIntegrityCheck: OK (generation, snapshots, uniqueness, bulk rollback, CSV)");
    }
}
