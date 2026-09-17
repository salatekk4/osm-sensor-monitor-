package ru.miet.osmsensors.model;

public class Sensor {
    private int id;
    private double x;
    private double y;
    private double radius;
    private double value;
    private String type;   // T, CO2, N2
    private String status; // OK, ERROR, WARNING

    public Sensor(int id, double x, double y, double radius, double value, String type, String status) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.value = value;
        this.type = type;
        this.status = status;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Форматированная строка для вывода информации о датчике
     */
    public String toFormattedString() {
        return String.format(
            "ID: %-3d | Тип: %-4s | Положение: (%6.2f; %6.2f) | Радиус: %5.2f | Значение: %6.2f | Статус: %s",
            id, type, x, y, radius, value, status
        );
    }

    @Override
    public String toString() {
        return toFormattedString();
    }
}
