package ru.miet.osmsensors.model;

public class GasSensor extends Sensor {

    public GasSensor(int id, double x, double y, double radius, double value, String status) {
        super(id, x, y, radius, value, status);
    }

    @Override
    public String getType() {
        return "N2";
    }

    // Тревога: концентрация азота выше 80% — опасное вытеснение кислорода
    @Override
    public boolean isAlarm() {
        return value > 80.0;
    }

    @Override
    public String toString() {
        return super.toString() + " [Порог: 80%]";
    }
}
