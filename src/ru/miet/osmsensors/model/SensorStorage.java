package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.List;

public class SensorStorage {
    private final List<Sensor> sensors = new ArrayList<>();

    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    public List<Sensor> getAllSensors() {
        return new ArrayList<>(sensors);
    }
}
