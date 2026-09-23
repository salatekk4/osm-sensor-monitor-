package ru.miet.osmsensors.controller;

import java.util.Scanner;
import ru.miet.osmsensors.model.Sensor;
import ru.miet.osmsensors.model.SensorStorage;
import ru.miet.osmsensors.view.ConsoleView;

public class AppController {
    private final SensorStorage storage;
    private final Scanner sc;

    private static final int MENU_SHOW_ALL = 1;
    private static final int MENU_ADD = 2;
    private static final int MENU_FILTER = 3;
    private static final int MENU_FIND_BY_ID = 4;
    private static final int MENU_EXIT = 0;

    public AppController() {
        this.storage = new SensorStorage();
        this.sc = new Scanner(System.in);
    }

    public void initDefaultData() {
        Sensor s1 = new Sensor(1, 20.3, 13.4, 5.0, 36.9, "T", "OK" );
        Sensor s2 = new Sensor(2, 10.5, 34.6, 10.0, 216.4, "CO2", "ERROR");
        Sensor s3 = new Sensor(3, 5.3, 10.2, 54.1, 43.5, "N2", "WARNING");

        storage.add(s1);
        storage.add(s2);
        storage.add(s3);
        System.out.println("Мест занаято: " + storage.getCount());
    }
    private int readMenuChoice() {
        System.out.println("Введите номер пункта");
        String input = sc.nextLine();
        return Integer.parseInt(input);
    }

    public void startInteractiveLoop() {
        boolean running = true;
        while (running) {
            System.out.println("1. Вывести все датчики");
            System.out.println("2. Добавить датчики");
            System.out.println("3. Фильтр по типу");
            System.out.println("4. Поиск по ID");
            System.out.println("5. Выход");

            int choice = readMenuChoice();

            switch (choice) {
                case MENU_SHOW_ALL:

                case MENU_ADD:

                case MENU_FILTER:

                case MENU_FIND_BY_ID:

                case MENU_EXIT:
                    running = false;
                    System.out.println("Выход из программы");
                    break;

                default:
                    System.out.println("Неизвестный пункт");
            }
        }
    }

    private void showAllSensors() {
        Sensor[] allSensors = storage.getAll();
        if (allSensors.length == 0 || allSensors == null) {
            System.out.println("Нет датчиков");
        }
        else {
            for (Sensor s: allSensors) {
                System.out.println(s);
            }
        }
    }

    private void filterByType() {

    }

    private void filterByID() {

    }

    public void start() {
        initDefaultData();
        startInteractiveLoop();
    }
}

