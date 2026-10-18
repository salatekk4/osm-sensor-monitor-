package ru.miet.osmsensors.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Результат валидации формы: ошибки по полям. */
public class ValidationResult {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public void add(String field, String message) { errors.putIfAbsent(field, message); }
    public boolean isValid() { return errors.isEmpty(); }
    public Map<String, String> getErrors() { return errors; }

    public String toMessage() {
        StringBuilder sb = new StringBuilder();
        errors.forEach((f, m) -> sb.append("• ").append(f).append(": ").append(m).append("\n"));
        return sb.toString();
    }
}
