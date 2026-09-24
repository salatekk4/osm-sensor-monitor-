package ru.miet.osmsensors;

import ru.miet.osmsensors.controller.AppController;
import ru.miet.osmsensors.model.Sensor;
import ru.miet.osmsensors.model.SensorStorage;
import ru.miet.osmsensors.view.ConsoleView;

public class Main {
    private static final int DEFAULT_LIMIT = 20;

    public static void main(String[] args) {
        boolean demoMode = false;
        int limit = DEFAULT_LIMIT;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--demo")) {
                demoMode = true;
            } else if (arg.equals("--limit")) {
                try {
                    limit = Integer.parseInt(args[i + 1]);
                    i++;
                } catch (NumberFormatException e) {
                    System.out.println("Некорректное значение --limit");
                    limit = DEFAULT_LIMIT;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("После --limit не указано число");
                    limit = DEFAULT_LIMIT;
                }
            }
        }
        System.out.println("Запуск приложения в демо режиме " + demoMode + ", limit: " + limit);

        ConsoleView view = new ConsoleView();
        SensorStorage storage = new SensorStorage(limit);
        AppController controller = new AppController(storage, view);
        if (demoMode) {
            controller.initDefaultData(limit);
        }
        controller.startInteractiveLoop();
    }
}
