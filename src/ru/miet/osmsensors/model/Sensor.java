package ru.miet.osmsensors.model;

import java.util.Objects;

public abstract class Sensor {

    // Статический счетчик всех созданных экземпляров
    private static int sensorCount = 0;

    protected int id;
    protected double x;
    protected double y;
    protected double radius;
    protected double value;
    protected String status; // OK, ERROR, WARNING

    public Sensor(int id, double x, double y, double radius, double value, String status) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.value = value;
        this.status = status;
        sensorCount++; // инкрементируем счетчик при каждом создании объекта
    }

    // Абстрактные методы — каждый наследник реализует сам
    public abstract String getType();
    public abstract boolean isAlarm();

    // Статический метод для получения счетчика
    public static int getSensorCount() {
        return sensorCount;
    }

    // Геттеры и сеттеры
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

    // Переопределение методов Object — сравнение по id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sensor sensor = (Sensor) o;
        return id == sensor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
            "ID: %-3d | Тип: %-3s | Позиция: (%6.2f; %6.2f) | Радиус: %5.2f | Значение: %6.2f | Статус: %-7s | Авария: %s",
            id, getType(), x, y, radius, value, status, isAlarm() ? "ДА" : "нет"
        );
    }
}
