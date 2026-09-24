package ru.miet.osmsensors.controller;

import java.util.Random;
import ru.miet.osmsensors.model.Sensor;
import ru.miet.osmsensors.model.SensorStorage;
import ru.miet.osmsensors.view.ConsoleView;

public class AppController {
    private final SensorStorage storage;
    private final ConsoleView view;

    private static final int MENU_SHOW_ALL = 1;
    private static final int MENU_ADD = 2;
    private static final int MENU_FILTER = 3;
    private static final int MENU_FIND_BY_ID = 4;
    private static final int MENU_EXIT = 0;

    public AppController(SensorStorage storage, ConsoleView view) {
        this.storage = storage;
        this.view = view;
    }

    public void initDefaultData(int count) {
        String[] types = {"T", "CO2", "N2"};
        String[] statuses = {"OK", "ERROR", "WARNING"};
        Random random = new Random();

        for (int i = 1; i <= count; i++) {
            String type = types[random.nextInt(types.length)];
            String status = statuses[random.nextInt(statuses.length)];
            double x = random.nextDouble() * 100;
            double y = random.nextDouble() * 100;
            double radius = random.nextDouble() * 20;
            double value = random.nextDouble() * 500;

            Sensor sensor = new Sensor(i, x, y, radius, value, type, status);
            storage.add(sensor);

        }
        view.printMessage("Сгенерировано датчиков: " + storage.getCount());
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
        String type = view.readLine("Введите тип");
        Sensor[] filtered = storage.findByType(type);
        if (filtered.length == 0) {
            view.printMessage("Датчиков такого типа нет");
        } else {
                view.printSensorTable(filtered);
        }
    }

    private void filterById() {
        String Id = view.readLine("Введите ID");
        try {
            int id = Integer.parseInt(Id);
            Sensor found = storage.findById(id);
            if (found == null) {
                view.printMessage("Датчиков с таким ID нет");
            } else {
                view.printSensor(found);
            }
        } catch (NumberFormatException e) {
            view.printMessage("ID должно быть числом");
        }
    }

    private void addSensorManually() {
        try {
            int id = Integer.parseInt(view.readLine("ID"));
            if (id < 0) {
                throw new InvalidSensorInputException("ID не может быть отрицательным");
            }
            if (storage.findById(id) != null ) {
                throw new InvalidSensorInputException("Датчик с таким " + id + " уже существует");
            }
            double x = Double.parseDouble(view.readLine("Координата X: "));
            double y = Double.parseDouble(view.readLine("Координата Y: "));
            double radius = Double.parseDouble(view.readLine("Радиус: "));
            if (radius < 0) {
                throw new InvalidSensorInputException("Радиус не может быть отрицательным");
            }
            double value = Double.parseDouble(view.readLine("Значение: "));
            String type = view.readLine("Тип (T, CO2, N2): ");
            if (!type.equalsIgnoreCase("T") && !type.equalsIgnoreCase("CO2") && !type.equalsIgnoreCase("N2")) {
                throw new InvalidSensorInputException("Неизвестный тип датчика");
            }
            String status = view.readLine("Статус (OK, ERROR, WARNING): ");
            if (!status.equalsIgnoreCase("OK") && !status.equalsIgnoreCase("ERROR") && !status.equalsIgnoreCase("WARNING")) {
                throw new InvalidSensorInputException("Неизвестный статус");
            }

            Sensor sensor = new Sensor(id, x, y, radius, value, type, status);
            boolean ok = storage.add(sensor);
            if (!ok) {
                view.printMessage("Хранилище заполнено: " + storage.getCount() + " из  " + storage.getCapacity());
            } else {
                view.printMessage("Датчик добавлен");
            }
        } catch (NumberFormatException e) {
            view.printMessage("Введено не число");
        } catch (InvalidSensorInputException e) {
            view.printMessage(e.getMessage());
        }
    }

    }


