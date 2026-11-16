package ru.miet.osmsensors.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Sensor implements Cloneable {
    private static final AtomicInteger sensorCount = new AtomicInteger();
    protected int id;
    protected double x, y, radius, value;
    protected String status;

    public Sensor(int id, double x, double y, double radius, double value, String status) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.value = value;
        this.status = status;
        sensorCount.incrementAndGet();
    }

    public abstract String getType();
    public abstract boolean isAlarm();
    public static int getSensorCount() { return sensorCount.get(); }

    /**
     * Detached copy preserving the concrete subtype. Current sensor fields are
     * primitives and immutable Strings. A subclass adding mutable reference
     * fields must override this method and copy those fields as well.
     * Copying does not invoke a constructor or increment the creation counter.
     * Callers must not mutate the source concurrently while it is being copied.
     */
    public Sensor copy() {
        try {
            return (Sensor) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Sensor) o).id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format(
            "ID: %-3d | Тип: %-3s | Позиция: (%6.2f; %6.2f) | Радиус: %5.2f | Значение: %6.2f | Статус: %-7s | Авария: %s",
            id, getType(), x, y, radius, value, status, isAlarm() ? "ДА" : "нет");
    }
}
