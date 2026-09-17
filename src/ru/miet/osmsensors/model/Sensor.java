package ru.miet.osmsensors.model;

public class Sensor {
    private String id;
    private String name;
    private double value;

    public Sensor(String id, String name, double value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
