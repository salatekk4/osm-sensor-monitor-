package ru.miet.osmsensors.view;

import ru.miet.osmsensors.model.Sensor;
import java.util.Scanner;
import java.util.List;

public class ConsoleView {
	private boolean isRoot = false;
	private Scanner scanner = new Scanner(System.in);
	
    public ConsoleView() { }
    
    public ConsoleView(boolean isRoot) {
    	this.isRoot = isRoot;
    }
    
    public void showMenu() {
    	System.out.println(
    			"1  - Показать все датчики.\n"
    			+ "2  - Добавить новый датчик вручную.\n"
    			+ "3  - Фильтр по типу датчика (T, CO2, N2).\n"
    			+ "4  - Найти датчик по ID.\n"
    			+ "5  - Показать аварийные датчики.\n"
    			+ "6  - Показать статистику.\n"
    			+ "7  - Показать ошибки.\n"
    			+ "8  - Обновить датчик.\n"
    			+ "9  - Удалить датчик.\n"
    			+ "10 - Сохранить датчики в файл.\n"
    			+ "11 - Загрузить датчики из файла."
    	);
    	if (isRoot) {
    		System.out.println(
    				"12 - Режим отладки (вкл/выкл).\n"
    				+ "13 - Запустить автотесты."
    		);
    	}
    	System.out.println(
    			"0  - Выход из программы"
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
    
    public void printSensorTable(List<Sensor> sensors) {
    	for (Sensor s: sensors) {
    		System.out.println(s.toString());
    	}
    }
    
    public void printMessage(String msg) {
    	System.out.println(msg);
    }
    
    public void printError(Exception e) {
    	String message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
    	System.err.println(message);
    }
    
    public void printError(String msg) {
    	System.err.println(msg);
    }
    
    public String readLine(String prompt) {
    	System.out.print(prompt);
    	return scanner.nextLine();
    }
}
