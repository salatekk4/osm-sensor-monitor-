package ru.miet.osmsensors.model;

public class TemperatureSensor extends Sensor {

    public TemperatureSensor(int id, double x, double y, double radius, double value, String status) {
        super(id, x, y, radius, value, status);
    }

    @Override
    public String getType() {
        return "T";
    }

    // Тревога: температура вышла за границы нормы
    @Override
    public boolean isAlarm() {
        return value > 50.0 || value < -20.0;
    }

    @Override
    public String toString() {
        return super.toString() + " [Диапазон: -20..50 °C]";
    }
}
