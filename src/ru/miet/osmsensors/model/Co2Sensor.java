package ru.miet.osmsensors.model;

public class Co2Sensor extends Sensor {

    public Co2Sensor(int id, double x, double y, double radius, double value, String status) {
        super(id, x, y, radius, value, status);
    }

    @Override
    public String getType() {
        return "CO2";
    }

    // Тревога: концентрация CO2 превышает 1000 ppm
    @Override
    public boolean isAlarm() {
        return value > 1000.0;
    }

    @Override
    public String toString() {
        return super.toString() + " [Порог: 1000 ppm]";
    }
}
