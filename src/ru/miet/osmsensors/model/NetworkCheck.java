package ru.miet.osmsensors.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Run as a main class; failures throw and produce a nonzero process exit. No sockets needed. */
public final class NetworkCheck {
    private static void check(boolean ok, String name) {
        if (!ok) throw new AssertionError(name);
    }
    private static Sensor sensor(int id, double value) {
        return new TemperatureSensor(id, 37.6, 55.7, 100, value, "OK");
    }
    private static void rewriteCrc(byte[] frame) throws IOException {
        long crc = SensorNetworkProtocol.crc32(Arrays.copyOfRange(frame, 4, frame.length - 4));
        for (int i = 0; i < 4; i++) frame[frame.length - 4 + i] = (byte) (crc >>> (24 - 8 * i));
    }
    public static void main(String[] args) throws Exception {
        check(SensorNetworkProtocol.crc32("123456789".getBytes(StandardCharsets.US_ASCII))
            == 0xcbf43926L, "CRC32 reference vector");
        Sensor original = sensor(1, 20);
        SensorNetworkProtocol.Packet packet = SensorNetworkProtocol.single(original, 1000);
        original.setValue(999);
        byte[] frame = SensorNetworkProtocol.encode(packet);
        SensorNetworkProtocol.Packet copy = SensorNetworkProtocol.decode(frame);
        check(copy.getReadings().get(0).getSensor().getValue() == 20, "snapshot isolation");
        check(copy.getReadings().get(0).getTimestamp() == 1000, "timestamp");
        Sensor[] all = {sensor(1, 20), new Co2Sensor(2, 37.7, 55.8, 50, 1200, "WARNING"),
            new GasSensor(3, 37.8, 55.9, 10, 90, "ERROR")};
        SensorNetworkProtocol.Packet batch = SensorNetworkProtocol.fromSensors(all, 1000);
        byte[] batchBytes = SensorNetworkProtocol.encode(batch);
        SensorStorage storage = new SensorStorage(10);
        List<WriteResult> added = SensorNetworkProtocol.receiveInto(new ByteArrayInputStream(batchBytes), storage);
        check(storage.getCount() == 3 && added.stream().allMatch(r -> r == WriteResult.ADDED), "batch apply");
        check(storage.getById(2).getType().equals("CO2") && storage.getById(3).getStatus().equals("ERROR"), "subtypes");
        check(SensorNetworkProtocol.apply(SensorNetworkProtocol.single(sensor(1, 10), 999), storage)
            .get(0) == WriteResult.STALE_IGNORED, "stale write");
        check(SensorNetworkProtocol.apply(SensorNetworkProtocol.single(sensor(1, 30), 1001), storage)
            .get(0) == WriteResult.UPDATED, "fresh write");
        byte[] corrupt = frame.clone(); corrupt[20] ^= 1;
        boolean rejected = false;
        try { SensorNetworkProtocol.receiveInto(new ByteArrayInputStream(corrupt), storage); }
        catch (SensorNetworkProtocol.ChecksumException ex) { rejected = true; }
        check(rejected && storage.getById(1).getValue() == 30, "CRC rejection without mutation");
        rejected = false;
        try { SensorNetworkProtocol.decode(Arrays.copyOf(frame, frame.length - 1)); }
        catch (EOFException ex) { rejected = true; }
        check(rejected, "truncated frame");
        rejected = false;
        try { SensorNetworkProtocol.decode(new byte[] {127, -1, -1, -1}); }
        catch (SensorNetworkProtocol.ProtocolException ex) { rejected = true; }
        check(rejected, "oversized length");
        byte[] wrongVersion = frame.clone(); wrongVersion[11] = 2; rewriteCrc(wrongVersion);
        rejected = false;
        try { SensorNetworkProtocol.decode(wrongVersion); }
        catch (SensorNetworkProtocol.ProtocolException ex) { rejected = true; }
        check(rejected, "version rejection despite valid CRC");
        byte[] invalidCoordinate = frame.clone();
        // First record: body header 12, timestamp 8, id 4, UTF type 3, longitude 8.
        ByteArrayOutputStream number = new ByteArrayOutputStream();
        new DataOutputStream(number).writeDouble(181);
        System.arraycopy(number.toByteArray(), 0, invalidCoordinate, 31, 8);
        rewriteCrc(invalidCoordinate);
        rejected = false;
        try { SensorNetworkProtocol.decode(invalidCoordinate); }
        catch (SensorNetworkProtocol.ProtocolException ex) { rejected = true; }
        check(rejected, "geographic validation despite valid CRC");
        InputStream fragmented = new FilterInputStream(new ByteArrayInputStream(batchBytes)) {
            @Override public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, 1));
            }
        };
        check(SensorNetworkProtocol.read(fragmented).getReadings().size() == 3, "fragmented input");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        SensorNetworkProtocol.write(stream, packet); SensorNetworkProtocol.write(stream, batch);
        ByteArrayInputStream joined = new ByteArrayInputStream(stream.toByteArray());
        check(SensorNetworkProtocol.read(joined).getReadings().size() == 1, "first frame");
        check(SensorNetworkProtocol.read(joined).getReadings().size() == 3, "second frame");
        SensorStorage full = new SensorStorage(1);
        rejected = false;
        try { SensorNetworkProtocol.apply(batch, full); }
        catch (IllegalStateException ex) { rejected = true; }
        check(rejected && full.getCount() == 0, "capacity rejection without partial writes");
        Path csv = Files.createTempFile("sensor-network-check-", ".csv");
        try {
            SensorFileRepository file = new SensorFileRepository(csv);
            file.saveAll(storage);
            check(SensorNetworkProtocol.fromFile(file, 2000).getReadings().size() == 3, "CSV batch import");
        } finally { Files.deleteIfExists(csv); }
        SensorStorage concurrent = new SensorStorage(10);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> jobs = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                final long time = i;
                jobs.add(executor.submit(() -> {
                    try {
                        SensorNetworkProtocol.receiveInto(new ByteArrayInputStream(
                            SensorNetworkProtocol.encode(SensorNetworkProtocol.single(sensor(1, time), time))), concurrent);
                    } catch (IOException ex) { throw new RuntimeException(ex); }
                }));
            }
            for (Future<?> job : jobs) job.get();
        } finally { executor.shutdownNow(); }
        check(concurrent.getCount() == 1 && concurrent.getById(1).getValue() == 19, "concurrent receive");
        System.out.println("NetworkCheck: OK (CRC32, round-trip, batch, corruption, validation, framing, CSV, concurrency)");
    }
}
