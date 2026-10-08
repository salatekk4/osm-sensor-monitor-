package ru.miet.osmsensors.model;

/** Ошибка формата файла датчиков: строка не соответствует CSV-схеме. */
public class SensorFileFormatException extends Exception {
    private final int lineNumber;

    public SensorFileFormatException(int lineNumber, String message) {
        super("Строка " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    public SensorFileFormatException(int lineNumber, String message, Throwable cause) {
        super("Строка " + lineNumber + ": " + message, cause);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
