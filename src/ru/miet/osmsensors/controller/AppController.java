package ru.miet.osmsensors.controller;

import java.util.List;
import java.util.Random;

import ru.miet.osmsensors.view.ConsoleView;
import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.view.AppLogger;


public class AppController {
    private final SensorRepository storage;
    private final ConsoleView view;
    private final AppLogger logger;

    private static final int MENU_SHOW_ALL = 1;
    private static final int MENU_ADD = 2;
    private static final int MENU_FILTER = 3;
    private static final int MENU_FIND_BY_ID = 4;
    private static final int MENU_ALARM = 5;
    private static final int MENU_STATS = 6;
    private static final int MENU_ERRORS = 7;
    private static final int MENU_EXIT = 0;

    public AppController(SensorRepository storage, ConsoleView view, AppLogger logger) {
        this.storage = storage;
        this.view = view;
        this.logger = logger;
    }

    public void initDefaultData(int count) {
        SensorFactory factory = new SensorFactory(new Random());

        for (Sensor sensor: factory.createSensors(count)) {
            try {
                storage.addUnique(sensor);
            } catch (DuplicateSensorException e){
                reportError(e);
            } catch (StorageFullException e) {
                reportError(e);
                break;
            }
        }
        String summary = "Сгенерировано датчиков: " + storage.getCount();
        view.printMessage(summary);
        logInfo(summary + " " + storage.findAlarmSensors().length);
    }

    private Sensor createSensor(String type, int id, double x, double y, double radius, double value, String status) {
        switch (type.toUpperCase()) {
            case "T":
                return new TemperatureSensor(id, x, y, radius, value, status);
            case "CO2":
                return new Co2Sensor(id, x, y, radius, value, status);
            case "N2":
                return new GasSensor(id, x, y, radius, value, status);
            default:
                throw new IllegalArgumentException("Неизвестный тип датчика " + type);
        }
    }

    private int readMenuChoice() {
        String input = view.readLine("Введите номер пункта: ");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e){
            return -1;
        }
    }

    public void startInteractiveLoop() {
        boolean running = true;
        while (running) {
            view.showMenu();

            int choice = readMenuChoice();

            switch (choice) {
                case MENU_SHOW_ALL:
                    showAllSensors();
                    break;
                case MENU_ADD:
                    addSensorManually();
                    break;
                case MENU_FILTER:
                    filterByType();
                    break;
                case MENU_FIND_BY_ID:
                    filterById();
                    break;
                case MENU_EXIT:
                    running = false;
                    view.printMessage("Выход из программы");
                    break;
                case MENU_ALARM:
                    showAlarmSensors();
                    break;
                case MENU_STATS:
                    showStatistics();
                    break;
                case MENU_ERRORS:
                    showErrorStatistics();
                    break;
                default:
                    view.printMessage("Неизвестный пункт");
            }
        }
    }

    private void showAllSensors() {
        Sensor[] allSensors = storage.getAll();
        if (allSensors == null || allSensors.length == 0) {
            view.printMessage("Нет датчиков");
        }
        else {
                view.printSensorTable(allSensors);
        }
    }

    private void filterByType() {
        String type = view.readLine("Введите тип: ");
        Sensor[] filtered = storage.findByType(type);
        if (filtered.length == 0) {
            view.printMessage("Датчиков такого типа нет");
        } else {
                view.printSensorTable(filtered);
        }
    }

    private void filterById() {
        try {
            int id = readInt("Введите ID: ", "ID");
            Sensor found = storage.getById(id);
            view.printSensor(found);
        } catch (InvalidSensorInputException e) {
            reportError(e);
        } catch (SensorNotFoundException e) {
            reportError(e);
        }
    }

    private void showAlarmSensors() {
        Sensor[] alarmSensors = storage.findAlarmSensors();
        if (alarmSensors == null || alarmSensors.length == 0) {
            view.printMessage("Аварийных датчиков нет");
        } else {
            view.printSensorTable(alarmSensors);
        }
    }

    private void showStatistics() {
        view.printMessage("Всего создано датчиков: " + Sensor.getSensorCount());
        view.printMessage("В хранилище занято: " + storage.getCount() + " из " + storage.getCapacity());
    }

    private void addSensorManually() {
        try {
            int id = readInt("ID: ", "ID");
            if (id < 0) {
                throw new InvalidSensorInputException("ID не может быть отрицательным");
            }
        double x = readDouble("Координата X: ", "Координата X");
        double y = readDouble("Координата Y: ", "Координата Y");
        double radius = readDouble("Радиус: ", "Радиус");
        if (radius < 0) {
            throw new InvalidSensorInputException("Радиус не может быть отрицательным");
        }
        double value = readDouble("Значение: ", "Значение");
        String type = readType();
        String status = readStatus();

        Sensor sensor = createSensor(type, id, x, y, radius, value, status);
        storage.addUnique(sensor);             // бросает DuplicateSensorException или StorageFullException

        view.printMessage("Датчик добавлен");
        logInfo("Добавлен датчик id=" + id + ", тип=" + sensor.getType());
        if (sensor.isAlarm()) {
            view.printMessage("Внимание: датчик в аварийном режиме");
            logInfo("ТРЕВОГА: датчик id=" + id + ", тип=" + sensor.getType() + ", значение=" + value);
        }
    } catch (InvalidSensorInputException e) {
        reportError(e);
    } catch (DuplicateSensorException e) {
        reportError(e);
    } catch (StorageFullException e) {
        reportError(e);
        }
    }

    private void showErrorStatistics() {
        view.printMessage("Ошибок за сеанс: " + logger.getErrorCount());
    }

    private double readDouble(String prompt, String fieldName) throws InvalidSensorInputException {
        String text = view.readLine(prompt).trim().replace(",", ".");
        double number;
        try {
            number = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new InvalidSensorInputException("Поле «" + fieldName + "»: ожидалось число, введено \"" + text + "\"");
        }
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            throw new InvalidSensorInputException("Поле «" + fieldName + "»: недопустимое значение \"" + text + "\"");
        }
        return number;
    }

    private String readType() throws InvalidSensorInputException {
        String choice = view.readLine("Тип (1 - температура, 2 - CO2, 3 - газ N2): ").trim();
        switch (choice) {
            case "1":
                return "T";
            case "2":
                return "CO2";
            case "3":
                return "N2";
            default:
                throw new InvalidSensorInputException("Неизвестный тип датчика: " + choice);
        }
    }

    private String readStatus() throws InvalidSensorInputException {
        String status = view.readLine("Статус (OK, ERROR, WARNING): ").trim().toUpperCase();
        if (!status.equals("OK") && !status.equals("ERROR") && !status.equals("WARNING")) {
            throw new InvalidSensorInputException("Неизвестный статус: " + status);
        }
        return status;
    }

    private void reportError(Exception e) {
        String message = (e.getMessage() != null) ? e.getMessage(): e.getClass().getSimpleName();
        view.printMessage("Ошибка: " + message);
        logError(message);
    }

    private void logInfo(String message) {
        logger.info(message);
    }

    private void logError(String message) {
        logger.error(message);
    }
}


