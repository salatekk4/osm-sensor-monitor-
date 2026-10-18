package ru.miet.osmsensors.model;

import java.util.List;

/** Самопроверка пересчёта координат для отчёта по ЛР5. */
public class MapCheck {
    public static void main(String[] args) {
        Sensor[] arr = {
            new TemperatureSensor(1, 0, 0, 5, 20, "OK"),
            new Co2Sensor(2, 100, 50, 10, 1200, "WARNING"),
            new GasSensor(3, 50, 100, 8, 90, "ERROR")
        };
        MapViewport vp = new MapViewport(800, 600, 40);
        vp.fitTo(arr);
        List<SensorRenderData> d = vp.toRenderData(arr);
        boolean ok = true;
        for (SensorRenderData r : d) {
            System.out.printf("%s -> (%.1f, %.1f) r=%.1f %s%n", r.getLabel(),
                r.getScreenX(), r.getScreenY(), r.getScreenRadius(), r.getColor());
            if (r.getScreenX() < 0 || r.getScreenX() > 800
                || r.getScreenY() < 0 || r.getScreenY() > 600) ok = false;
        }
        SensorRenderData hit = vp.findAt(d, d.get(1).getScreenX(), d.get(1).getScreenY());
        if (hit == null || hit.getSensor().getId() != 2) ok = false;
        double back = vp.toWorldX(vp.toScreenX(37.5));
        if (Math.abs(back - 37.5) > 1e-9) ok = false;
        System.out.println(ok ? "RESULT: OK" : "RESULT: FAIL");
    }
}
