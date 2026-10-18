package ru.miet.osmsensors.model;

import java.util.Set;

/** Валидация данных формы и создание датчика из строк полей ввода. */
public class SensorValidator {
    public static final Set<String> TYPES = Set.of("T", "CO2", "N2");
    public static final Set<String> STATUSES = Set.of("OK", "WARNING", "ERROR");

    private static double parseDouble(String text, String field, ValidationResult r) {
        try {
            double v = Double.parseDouble(text.trim().replace(',', '.'));
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                r.add(field, "недопустимое число");
                return 0;
            }
            return v;
        } catch (NumberFormatException | NullPointerException e) {
            r.add(field, "ожидалось число, введено \"" + text + "\"");
            return 0;
        }
    }

    /** Проверяет все поля формы; ошибки собираются сразу по всем полям. */
    public static ValidationResult validate(String id, String x, String y, String radius,
                                            String value, String type, String status) {
        ValidationResult r = new ValidationResult();
        try {
            if (Integer.parseInt(id.trim()) < 0) r.add("ID", "не может быть отрицательным");
        } catch (NumberFormatException | NullPointerException e) {
            r.add("ID", "ожидалось целое число");
        }
        parseDouble(x, "X", r);
        parseDouble(y, "Y", r);
        if (parseDouble(radius, "Радиус", r) < 0) r.add("Радиус", "не может быть отрицательным");
        parseDouble(value, "Значение", r);
        if (type == null || !TYPES.contains(type.trim().toUpperCase()))
            r.add("Тип", "допустимы T, CO2, N2");
        if (status == null || !STATUSES.contains(status.trim().toUpperCase()))
            r.add("Статус", "допустимы OK, WARNING, ERROR");
        return r;
    }

    /** Вызывать только после validate(...), когда isValid() == true. */
    public static Sensor createSensor(String id, String x, String y, String radius,
                                      String value, String type, String status) {
        int i = Integer.parseInt(id.trim());
        double dx = Double.parseDouble(x.trim().replace(',', '.'));
        double dy = Double.parseDouble(y.trim().replace(',', '.'));
        double dr = Double.parseDouble(radius.trim().replace(',', '.'));
        double dv = Double.parseDouble(value.trim().replace(',', '.'));
        String st = status.trim().toUpperCase();
        switch (type.trim().toUpperCase()) {
            case "CO2": return new Co2Sensor(i, dx, dy, dr, dv, st);
            case "N2":  return new GasSensor(i, dx, dy, dr, dv, st);
            default:    return new TemperatureSensor(i, dx, dy, dr, dv, st);
        }
    }
}
