package ru.miet.osmsensors.controller;

import ru.miet.osmsensors.model.Sensor;
import ru.miet.osmsensors.model.SensorStorage;
import ru.miet.osmsensors.view.ConsoleView;

public class AppController {
    private final SensorStorage storage = new SensorStorage();
    private final ConsoleView view = new ConsoleView();

    public void start() {
        view.printWelcome();
        storage.addSensor(new Sensor("S1", "Датчик температуры", 24.5));
        view.displaySensors(storage.getAllSensors());
    }
}
