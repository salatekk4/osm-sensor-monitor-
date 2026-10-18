package ru.miet.osmsensors.model;

import java.awt.Color;

/** Готовые данные для отрисовки одного датчика (экранные координаты в пикселях). */
public final class SensorRenderData {
    private final Sensor sensor;
    private final double screenX;
    private final double screenY;
    private final double screenRadius;
    private final Color color;
    private final Color fillColor;
    private final String label;

    public SensorRenderData(Sensor sensor, double screenX, double screenY, double screenRadius) {
        this.sensor = sensor;
        this.screenX = screenX;
        this.screenY = screenY;
        this.screenRadius = screenRadius;
        this.color = SensorMapStyle.colorForStatus(sensor.getStatus());
        this.fillColor = SensorMapStyle.fillColorForStatus(sensor.getStatus());
        this.label = sensor.getType() + "#" + sensor.getId();
    }

    public Sensor getSensor() { return sensor; }
    public double getScreenX() { return screenX; }
    public double getScreenY() { return screenY; }
    public double getScreenRadius() { return screenRadius; }
    public Color getColor() { return color; }
    public Color getFillColor() { return fillColor; }
    public String getLabel() { return label; }

    /** Попадает ли точка клика (px, py) в круг датчика; нужно для MouseListener. */
    public boolean contains(double px, double py) {
        double r = Math.max(screenRadius, 6);
        double dx = px - screenX, dy = py - screenY;
        return dx * dx + dy * dy <= r * r;
    }
}
