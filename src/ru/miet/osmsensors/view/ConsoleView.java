package ru.miet.osmsensors.view;

import ru.miet.osmsensors.model.Sensor;
import java.util.List;

public class ConsoleView {
    public void printWelcome() {
        System.out.println("=== OSM Sensor Monitor ===");
    }

    public void displaySensors(List<Sensor> sensors) {
        if (sensors.isEmpty()) {
            System.out.println("Нет доступных датчиков.");
            return;
        }
        for (Sensor s : sensors) {
            System.out.printf("Датчик [%s] %s: %.2f%n", s.getId(), s.getName(), s.getValue());
        }
    }
}
