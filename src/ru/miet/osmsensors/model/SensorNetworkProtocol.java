package ru.miet.osmsensors.model;

import java.io.*;
import java.util.*;
import java.util.zip.CRC32;

/**
 * Lab 8 Model API, no sockets and no UI.
 * Frame (big endian): int bodyLength, body bytes, int CRC32(body).
 * Body: int magic=0x4f534d53, int version=1, int count, then records:
 * long timestampMillis, int id, writeUTF(type), double longitude, double latitude,
 * double radiusMetres, double value, writeUTF(status).
 * Strings use Java DataInput/DataOutput modified UTF-8 (not ordinary UTF-8).
 * A file is sent as its parsed sensor records, not raw file bytes or a remote path.
 * Limits: 10000 records, 1 MiB body. Empty batch is valid.
 * CRC detects accidental corruption; it is NOT authentication or encryption.
 * Caller owns streams, configures socket timeouts and serializes writes per connection.
 * Malformed frames are fatal for that connection: close it rather than resynchronize.
 */
public final class SensorNetworkProtocol {
    public static final int MAGIC = 0x4f534d53;
    public static final int VERSION = 1;
    public static final int MAX_RECORDS = 10000;
    public static final int MAX_BODY_BYTES = 1024 * 1024;
    private SensorNetworkProtocol() {}

    public static final class ProtocolException extends IOException {
        public ProtocolException(String message) { super(message); }
        public ProtocolException(String message, Throwable cause) { super(message, cause); }
    }
    public static final class ChecksumException extends IOException {
        public ChecksumException() { super("CRC32 mismatch: packet rejected"); }
    }

    /** Immutable reading snapshot; never retains the caller's mutable Sensor. */
    public static final class Reading {
        private final long timestamp;
        private final int id;
        private final String type, status;
        private final double longitude, latitude, radius, value;
        public Reading(Sensor sensor, long timestamp) {
            if (sensor == null) throw new IllegalArgumentException("Sensor must not be null");
            if (timestamp < 0) throw new IllegalArgumentException("Negative timestamp");
            this.id = sensor.getId();
            this.type = sensor.getType();
            this.longitude = sensor.getX();
            this.latitude = sensor.getY();
            this.radius = sensor.getRadius();
            this.value = sensor.getValue();
            this.status = sensor.getStatus();
            this.timestamp = timestamp;
            validate(id, type, longitude, latitude, radius, value, status);
        }
        public long getTimestamp() { return timestamp; }
        public Sensor getSensor() {
            return SensorValidator.createSensor(Integer.toString(id), Double.toString(longitude),
                Double.toString(latitude), Double.toString(radius), Double.toString(value), type, status);
        }
    }

    public static final class Packet {
        private final List<Reading> readings;
        public Packet(List<Reading> readings) {
            if (readings == null || readings.size() > MAX_RECORDS)
                throw new IllegalArgumentException("Invalid record count");
            ArrayList<Reading> copy = new ArrayList<>(readings);
            for (Reading reading : copy) Objects.requireNonNull(reading, "Null reading");
            this.readings = Collections.unmodifiableList(copy);
        }
        public List<Reading> getReadings() { return readings; }
    }

    public static Packet single(Sensor sensor, long timestamp) {
        return new Packet(Collections.singletonList(new Reading(sensor, timestamp)));
    }

    /** Import timestamp applies to all file records; no measurement history is inferred. */
    public static Packet fromSensors(Sensor[] sensors, long timestamp) {
        if (sensors == null || sensors.length > MAX_RECORDS)
            throw new IllegalArgumentException("Invalid sensor count");
        List<Reading> records = new ArrayList<>();
        for (Sensor sensor : sensors) records.add(new Reading(sensor, timestamp));
        return new Packet(records);
    }

    /** Existing CSV parser reads locally; Reading then validates every record geographically. */
    public static Packet fromFile(SensorFileRepository file, long importTimestamp)
            throws IOException, SensorFileFormatException {
        Objects.requireNonNull(file, "File repository");
        List<Sensor> sensors = file.readAll();
        return fromSensors(sensors.toArray(new Sensor[0]), importTimestamp);
    }

    private static void validate(int id, String type, double x, double y,
                                 double radius, double value, String status) {
        ValidationResult result = SensorValidator.validate(Integer.toString(id), Double.toString(x),
            Double.toString(y), Double.toString(radius), Double.toString(value), type, status);
        if (!result.isValid()) throw new IllegalArgumentException(result.toMessage());
    }

    public static long crc32(byte[] bytes) {
        Objects.requireNonNull(bytes, "Bytes");
        CRC32 crc = new CRC32();
        crc.update(bytes);
        return crc.getValue();
    }

    public static byte[] encode(Packet packet) throws IOException {
        Objects.requireNonNull(packet, "Packet");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream body = new DataOutputStream(bytes);
        body.writeInt(MAGIC);
        body.writeInt(VERSION);
        body.writeInt(packet.readings.size());
        for (Reading r : packet.readings) {
            body.writeLong(r.timestamp);
            body.writeInt(r.id);
            body.writeUTF(r.type.trim().toUpperCase(Locale.ROOT));
            body.writeDouble(r.longitude);
            body.writeDouble(r.latitude);
            body.writeDouble(r.radius);
            body.writeDouble(r.value);
            body.writeUTF(r.status.trim().toUpperCase(Locale.ROOT));
            if (bytes.size() > MAX_BODY_BYTES) throw new ProtocolException("Packet too large");
        }
        body.flush();
        byte[] payload = bytes.toByteArray();
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(frame);
        out.writeInt(payload.length);
        out.write(payload);
        out.writeInt((int) crc32(payload));
        out.flush();
        return frame.toByteArray();
    }

    /** Writes exactly one frame; does not close the stream. */
    public static void write(OutputStream stream, Packet packet) throws IOException {
        stream.write(encode(packet));
        stream.flush();
    }

    /** Reads one frame, including fragmented TCP input. EOFException means incomplete/no frame. */
    public static Packet read(InputStream stream) throws IOException {
        DataInputStream in = new DataInputStream(Objects.requireNonNull(stream, "Stream"));
        int length = in.readInt();
        if (length < 12 || length > MAX_BODY_BYTES)
            throw new ProtocolException("Invalid body length: " + length);
        byte[] payload = new byte[length];
        in.readFully(payload);
        long expected = Integer.toUnsignedLong(in.readInt());
        if (crc32(payload) != expected) throw new ChecksumException();
        DataInputStream body = new DataInputStream(new ByteArrayInputStream(payload));
        if (body.readInt() != MAGIC) throw new ProtocolException("Invalid magic");
        if (body.readInt() != VERSION) throw new ProtocolException("Unsupported protocol version");
        int count = body.readInt();
        if (count < 0 || count > MAX_RECORDS) throw new ProtocolException("Invalid record count");
        List<Reading> records = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                long timestamp = body.readLong();
                int id = body.readInt();
                String type = body.readUTF();
                double longitude = body.readDouble(), latitude = body.readDouble();
                double radius = body.readDouble(), value = body.readDouble();
                String status = body.readUTF();
                Sensor sensor = SensorValidator.createSensor(Integer.toString(id), Double.toString(longitude),
                    Double.toString(latitude), Double.toString(radius), Double.toString(value), type, status);
                records.add(new Reading(sensor, timestamp));
            }
        } catch (IllegalArgumentException ex) {
            throw new ProtocolException("Invalid sensor record", ex);
        }
        if (body.available() != 0) throw new ProtocolException("Trailing body bytes");
        return new Packet(records);
    }

    /** Byte array must contain exactly one frame. */
    public static Packet decode(byte[] frame) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(Objects.requireNonNull(frame, "Frame"));
        Packet packet = read(in);
        if (in.available() != 0) throw new ProtocolException("Trailing frame bytes");
        return packet;
    }

    /**
     * Packet is fully checked before storage changes. Capacity is preflighted under the
     * storage monitor; readers cannot observe half an applied batch through its methods.
     * Duplicate IDs within a batch are processed in order. Older timestamps are ignored;
     * equal timestamps follow existing upsert behavior (last received wins).
     * Client clocks must be coordinated; this is not a distributed clock protocol.
     */
    public static List<WriteResult> apply(Packet packet, SensorStorage storage) {
        Objects.requireNonNull(packet, "Packet");
        Objects.requireNonNull(storage, "Storage");
        List<Sensor> sensors = new ArrayList<>();
        for (Reading reading : packet.readings) sensors.add(reading.getSensor());
        synchronized (storage) {
            Set<Integer> missing = new HashSet<>();
            for (Sensor sensor : sensors)
                if (storage.findById(sensor.getId()) == null) missing.add(sensor.getId());
            if (missing.size() > storage.getCapacity() - storage.getCount())
                throw new IllegalStateException("Insufficient storage capacity: packet not applied");
            List<WriteResult> results = new ArrayList<>();
            for (int i = 0; i < sensors.size(); i++)
                results.add(storage.upsert(sensors.get(i), packet.readings.get(i).timestamp));
            return Collections.unmodifiableList(results);
        }
    }

    /** CRC/format/validation failure causes no storage mutation. */
    public static List<WriteResult> receiveInto(InputStream stream, SensorStorage storage) throws IOException {
        return apply(read(stream), storage);
    }
}
