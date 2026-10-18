package ru.miet.osmsensors.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Пересчёт координат датчиков (x, y) в пиксели с масштабом и отступами (ЛР5).
 * Масштаб единый по обеим осям, ось Y направлена вверх (как на карте).
 */
public class MapViewport {
    private double minX = 0, maxX = 100, minY = 0, maxY = 100;
    private int width = 800, height = 600, padding = 40;
    private double scale = 1, offsetX = 0, offsetY = 0;

    public MapViewport() { recalc(); }

    public MapViewport(int width, int height, int padding) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.padding = Math.max(0, padding);
        recalc();
    }

    public void setPanelSize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        recalc();
    }

    public void setPadding(int padding) { this.padding = Math.max(0, padding); recalc(); }

    public void setWorldBounds(double minX, double minY, double maxX, double maxY) {
        if (maxX <= minX) maxX = minX + 1;
        if (maxY <= minY) maxY = minY + 1;
        this.minX = minX; this.maxX = maxX; this.minY = minY; this.maxY = maxY;
        recalc();
    }

    /** Подгоняет границы мира под датчики (с учётом радиусов). */
    public void fitTo(Sensor[] sensors) {
        double x0 = Double.MAX_VALUE, y0 = Double.MAX_VALUE;
        double x1 = -Double.MAX_VALUE, y1 = -Double.MAX_VALUE;
        if (sensors != null) {
            for (Sensor s : sensors) {
                if (s == null) continue;
                x0 = Math.min(x0, s.getX() - s.getRadius());
                y0 = Math.min(y0, s.getY() - s.getRadius());
                x1 = Math.max(x1, s.getX() + s.getRadius());
                y1 = Math.max(y1, s.getY() + s.getRadius());
            }
        }
        if (x0 == Double.MAX_VALUE) { setWorldBounds(0, 0, 100, 100); return; }
        setWorldBounds(x0, y0, x1, y1);
    }

    private void recalc() {
        double availW = Math.max(1, width - 2.0 * padding);
        double availH = Math.max(1, height - 2.0 * padding);
        double worldW = maxX - minX, worldH = maxY - minY;
        scale = Math.min(availW / worldW, availH / worldH);
        offsetX = padding + (availW - worldW * scale) / 2.0;
        offsetY = padding + (availH - worldH * scale) / 2.0;
    }

    public double toScreenX(double worldX) { return offsetX + (worldX - minX) * scale; }
    public double toScreenY(double worldY) { return height - (offsetY + (worldY - minY) * scale); }
    public double toScreenLength(double worldLen) { return worldLen * scale; }
    public double toWorldX(double screenX) { return (screenX - offsetX) / scale + minX; }
    public double toWorldY(double screenY) { return (height - screenY - offsetY) / scale + minY; }
    public double getScale() { return scale; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public SensorRenderData toRenderData(Sensor s) {
        return new SensorRenderData(s, toScreenX(s.getX()), toScreenY(s.getY()), toScreenLength(s.getRadius()));
    }

    public List<SensorRenderData> toRenderData(Sensor[] sensors) {
        List<SensorRenderData> result = new ArrayList<>();
        if (sensors == null) return result;
        for (Sensor s : sensors) if (s != null) result.add(toRenderData(s));
        return result;
    }

    /** Датчик под курсором (последний в списке, т.е. нарисованный сверху) или null. */
    public SensorRenderData findAt(List<SensorRenderData> data, double px, double py) {
        for (int i = data.size() - 1; i >= 0; i--) {
            if (data.get(i).contains(px, py)) return data.get(i);
        }
        return null;
    }
}
