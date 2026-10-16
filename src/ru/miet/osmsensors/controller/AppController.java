package ru.miet.osmsensors.controller;

import java.util.List;
import java.util.Random;
import java.io.IOException;

import ru.miet.osmsensors.view.ConsoleView;
import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.view.AutoTests;

public class AppController {
    private final SensorRepository storage;
    private final ConsoleView view;
    private final AppLogger logger;
    private final SensorFileRepository fileRepository;
    private final AppConfig config;
    private boolean debugMode;

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private static final int MENU_SHOW_ALL = 1;
    private static final int MENU_ADD = 2;
    private static final int MENU_FILTER = 3;
    private static final int MENU_FIND_BY_ID = 4;
    private static final int MENU_ALARM = 5;
    private static final int MENU_STATS = 6;
    private static final int MENU_ERRORS = 7;
    private static final int MENU_UPDATE = 8;
    private static final int MENU_DELETE = 9;
    private static final int MENU_SAVE = 10;
    private static final int MENU_LOAD = 11;
    private static final int MENU_DEBUG = 12;
    private static final int MENU_AUTOTESTS = 13;
    private static final int MENU_EXIT = 0;

    public AppController(SensorRepository storage, ConsoleView view, AppLogger logger, SensorFileRepository fileRepository, AppConfig config) {
        this.storage = storage;
        this.view = view;
        this.logger = logger;
        this.fileRepository = fileRepository;
        this.config = config;
        this.debugMode = config.isDebug();
    }

    public boolean login() {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            String login = view.readLine("Логин: ").trim();
            String password = view.readLine("Пароль: ");

            if (login.equals(config.getLogin()) && password.equals(config.getPassword())) {
                view.printMessage("Добро пожаловать - " + config.getLogin());
                logInfo("Старт программы. Пользователь: " + config.getLogin() + ", группа: " + config.getGroup());
                debug("Настройки: debug=" + debugMode + ", autotests=" + config.isAutotests()
                        + ", файл БД=" + fileRepository.getFile().toAbsolutePath());
                return true;
            }
            view.printMessage("Неверный логин или пароль. Осталось попыток: " + (MAX_LOGIN_ATTEMPTS - attempt));
            logWarning("Неудачная попытка входа, введен логин: " + login);
        }
        view.printMessage("Вход не выполнен. Программа завершена");
        logWarning("Вход не выполнен: исчерпаны попытки");
        return false;
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
        logInfo(summary + ", из них аварийных: " + storage.findAlarmSensors().length);
    }

    private int readMenuChoice() {
        String input = view.readLine("Введите номер пункта: ").trim();
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
            debug("Выбран пункт меню: " + choice);

            switch (choice) {
                case MENU_SHOW_ALL:
                    showAllSensors();
                    break;
                case MENU_ADD:
                    addSensorsManually();
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
                    logInfo("Завершение программы. Пользователь: " + config.getLogin());
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
                case MENU_UPDATE:
                    updateSensor();
                    break;
                case MENU_DELETE:
                    deleteSensor();
                    break;
                case MENU_SAVE:
                    saveToFile();
                    break;
                case MENU_LOAD:
                    loadFromFile();
                    break;
                case MENU_DEBUG:
                    if (config.isRoot()) {
                        toggleDebug();
                    } else {
                        view.printMessage("Неизвестный пункт");
                    }
                    break;
                case MENU_AUTOTESTS:
                    if (config.isRoot()) {
                        runAutoTests();
                    } else {
                        view.printMessage("Неизвестный пункт");
                    }
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

    private void showErrorStatistics() {
        view.printMessage("Ошибок за сеанс: " + logger.getErrorCount());
    }

    private void addSensorsManually() {
        boolean more = true;
        while(more) {
            addOneSensor();
            String answer = view.readLine("Добавить еще датчик? (да/нет): ").trim();
            more = answer.equalsIgnoreCase("да") || answer.equalsIgnoreCase("y");
        }
    }

    private void addOneSensor() {
        try {
            int id = readInt("ID: ", "ID");
            if (id < 0) {
                throw new InvalidSensorInputException("ID не может быть отрицательным");
            }
            Sensor sensor = readSensorData(id);
            storage.addUnique(sensor);

            view.printMessage("Датчик добавлен");
            logInfo("Добавлен датчик id=" + id + ", тип=" + sensor.getType());
            if (sensor.isAlarm()) {
                view.printMessage("Внимание: датчик в аварийном режиме");
                logWarning("ТРЕВОГА: датчик id=" + id + ", тип=" + sensor.getType()
                        + ", значение=" + sensor.getValue());
            }
        } catch (InvalidSensorInputException e) {
            reportError(e);
        } catch (DuplicateSensorException e) {
            reportError(e);
        } catch (StorageFullException e) {
            reportError(e);
        }
    }

    private void updateSensor() {
        try {
            int id = readInt("ID датчика для изменения: ", "ID");
            Sensor old = storage.getById(id);
            view.printSensor(old);
            view.printMessage("Введите новые данные: ");

            Sensor updated = readSensorData(id);
            storage.updateById(id, updated);

            view.printMessage("Датчик изменен. Чтобы изменение попало в файл - пункт 10.");
            logInfo("Изменен датчик id = " + id);
        } catch (InvalidSensorInputException e) {
            reportError(e);
        } catch (SensorNotFoundException e) {
            reportError(e);
        } catch (DuplicateSensorException e) {
            reportError(e);
        }
    }

    private void deleteSensor() {
        try {
            int id = readInt("ID датчика для удаления: ", "ID");
            storage.deleteById(id);
            view.printMessage("Датчик удален. Чтобы изменение попало в файл - пункт 10.");
            logInfo("Удален датчик id = " + id);
        } catch (InvalidSensorInputException e) {
            reportError(e);
        } catch(SensorNotFoundException e) {
            reportError(e);
        }
    }

    private void saveToFile() {
        try {
            fileRepository.saveAll((SensorStorage) storage);

            String message = "Сохранено датчиков: " + storage.getCount()
                    + " в файл " + fileRepository.getFile();
            view.printMessage(message);
            logInfo("Запись БД. " + message);
        } catch (IOException e) {
            reportError(e);
        }
    }

    private void loadFromFile() {
        List<Sensor> fromFile;
        try {
            fromFile = fileRepository.readAll();
        } catch (IOException e) {
            reportError(e);
            return;
        } catch (SensorFileFormatException e) {
            reportError(e);
            return;
        }

        if (fromFile.isEmpty()) {
            view.printMessage("Файл пуст или не найден.");
            return;
        }
        clearStorage();
        int loaded = 0;
        for (Sensor sensor: fromFile) {
            try {
                storage.addUnique(sensor);
                loaded++;
            } catch (DuplicateSensorException e) {
                reportError(e);
            } catch (StorageFullException e) {
                reportError(e);
                break;
            }
        }
        view.printMessage("Загружено датчиков из файла: " + loaded);
        showAllSensors();
        logInfo("Чтение БД из файла " + fileRepository.getFile() + ": загружено " + loaded);
    }

    private void clearStorage() {
        for (Sensor sensor: storage.getAll()) {
            try {
                storage.deleteById(sensor.getId());
            } catch (SensorNotFoundException e) {
                reportError(e);
            }
        }
    }

    private void toggleDebug() {
        debugMode = !debugMode;
        view.printMessage("Режим отладки: " + (debugMode ? "включен" : "выключен"));
        logInfo("Режим отладки переключен: " + debugMode);
    }

    public void runAutoTests() {
        logInfo("Автотесты: запуск. Пользователь: " + config.getLogin());
        AutoTests tests = new AutoTests(view, logger);
        boolean allPassed = tests.runAll();

        String summary = allPassed ? "Автотесты: все пройдены" : "Автотесты: есть непройденные";
        view.printMessage(summary);
        if (allPassed) {
            logInfo(summary);
        } else {
            logWarning(summary);
        }
    }

    private Sensor readSensorData(int id) throws InvalidSensorInputException {
        double x = readDouble("Координата X: ", "Координата X");
        double y = readDouble("Координата Y: ", "Координата Y");
        double radius = readDouble("Радиус: ", "Радиус");
        if (radius < 0) {
            throw new InvalidSensorInputException("Радиус не может быть отрицательным");
        }
        double value = readDouble("Значение: ", "Значение");
        String type = readType();
        String status = readStatus();
        return createSensor(type, id, x, y, radius, value, status);
    }

    private Sensor createSensor(String type, int id, double x, double y, double radius, double value, String status) {
        switch (type) {
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

    private int readInt(String prompt, String fieldName) throws InvalidSensorInputException {
        String text = view.readLine(prompt).trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidSensorInputException("Поле «" + fieldName + "»: ожидалось целое число, введено \"" + text + "\"");
        }
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
        logError(e);
    }

    private void debug(String message) {
        if (debugMode) {
            logger.logInfo("[DEBUG] " + message);
        }
    }

    private void logInfo(String message) {
        logger.logInfo(message);
    }

    private void logError(Exception e) {
        logger.logError(e);
    }

    private void logWarning(String message) {
        logger.logWarning(message);
    }
}


