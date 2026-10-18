package ru.miet.osmsensors.model;

import java.awt.Color;

/** Соответствие статусов датчиков цветам карты (ЛР5). */
public final class SensorMapStyle {
    public static final Color OK_COLOR = new Color(46, 160, 67);
    public static final Color WARNING_COLOR = new Color(240, 200, 20);
    public static final Color ERROR_COLOR = new Color(210, 40, 40);
    public static final Color UNKNOWN_COLOR = Color.GRAY;

    private SensorMapStyle() {}

    public static Color colorForStatus(String status) {
        if (status == null) return UNKNOWN_COLOR;
        switch (status.trim().toUpperCase()) {
            case "OK": return OK_COLOR;
            case "WARNING": return WARNING_COLOR;
            case "ERROR": return ERROR_COLOR;
            default: return UNKNOWN_COLOR;
        }
    }

    /** Полупрозрачная заливка для круга радиуса действия. */
    public static Color fillColorForStatus(String status) {
        Color c = colorForStatus(status);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 70);
    }
}
