package ru.miet.osmsensors.view;

import ru.miet.osmsensors.model.Sensor;
import java.util.Scanner;
import java.util.List;

public class ConsoleView {
	private Scanner scanner = new Scanner(System.in);
	
    public ConsoleView() { }
    public void showMenu() {
    	System.out.println(
    			"1 — Показать все датчики.\n"
    			+ "2 — Добавить новый датчик вручную.\n"
    			+ "3 — Фильтр по типу датчика (T, CO2, N2).\n"
    			+ "4 — Найти датчик по ID.\n"
    			+ "0 — Выход."
    	);
    }
    public void printSensor(Sensor s) {
    	System.out.println(s.toString());
    }
    public void printSensorTable(Sensor[] sensors) {
    	for (Sensor s: sensors) {
    		System.out.println(s.toString());
    	}
    }
    public void printMessage(String msg) {
    	System.out.println(msg);
    }
    public String readLine(String prompt) {
    	System.out.print(prompt);
    	input = scanner.nextLine();
    	return input;
    }
}
