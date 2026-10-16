package ru.miet.osmsensors.view;

import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.controller.AppLogger;

public class AutoTests {
	private int count = 10000;
	private String GREEN_COLOR = "\u001B[32m";
	private String RESET_COLOR = "\u001B[0m";
	private ConsoleView view;
	private AppLogger logger;
	private SensorStorage testStorage;
	private SensorFactory factory;
	private SensorFileRepository testFileRepository;
	private Random random;
	
	public AutoTests(ConsoleView view, AppLogger logger) {
		this.view = view;
		this.logger = logger;
		random = new Random();
		testStorage = new SensorStorage(count);
		factory = new SensorFactory(random);
		testFileRepository = new SensorFileRepository(Paths.get("test_sensors.csv"));
	}
	
	private boolean testCreateSensors(boolean success) {
		try {
			for (Sensor sensor : factory.createSensors(count)) {
				try {
					testStorage.addUnique(sensor);
				} catch(DuplicateSensorException e) {
					logger.logError(e);
					throw(e);
				} catch(StorageFullException e) {
					logger.logError(e);
					throw(e);
				}
			}
			view.printMessage(GREEN_COLOR + "Тест: Сгенерировать 10000 случайных датчиков - пройден" + RESET_COLOR);
			logger.logInfo("Тест: Сгенерировать 10000 случайных датчиков - пройден");
		} catch(Exception e) {
			view.printError("Тест: Сгенерировать 10000 случайных датчиков - провален (" + testStorage.getCount() + "/10000)");
			logger.logErrorMessage("Тест: Сгенерировать 10000 случайных датчиков - провален (" + testStorage.getCount() + "/10000)");
			success = false;
		}
		return success;
	}
	
	private boolean testFindSensors(boolean success) {
		Sensor[] testArray = testStorage.getAll();
		int successfulSearches = 0;
		for (int i = 0; i < 100; i++) {
			int randomIndex = random.nextInt(testStorage.getCount());
			Sensor result = null;
			try {
				result = testStorage.getById(testArray[randomIndex].getId());
			} catch(SensorNotFoundException e) {
				logger.logError(e);
			}
			if (result != null && result.equals(testArray[randomIndex])) successfulSearches++;
		}
		if (successfulSearches == 100) {
			view.printMessage(GREEN_COLOR + "Тест: Найти 100 случайных датчиков по ID - пройден" + RESET_COLOR);
			logger.logInfo("Тест: Найти 100 случайных датчиков по ID - пройден");
		}
		else {
			view.printError("Тест: Найти 100 случайных датчиков по ID - провален (" + successfulSearches + "/100)");
			logger.logErrorMessage("Тест: Найти 100 случайных датчиков по ID - провален (" + successfulSearches + "/100)");
			success = false;
		}
		return success;
	}
	
	private boolean testFindSensorWithWrongId(boolean success) {
		try {
			int wrongId = random.nextInt(100000, 200000);
			testStorage.getById(wrongId);
			view.printError("Тест: Поиск несуществующего датчика - провален");
			logger.logErrorMessage("Тест: Поиск несуществующего датчика - провален");
			success = false;
		} catch(SensorNotFoundException e) {
			view.printMessage(GREEN_COLOR + "Тест: Поиск несуществующего датчика - пройден" + RESET_COLOR);
			logger.logInfo("Тест: Поиск несуществующего датчика - пройден");
		}
		return success;
	}
	
	private boolean testSaveSensorsToFile(boolean success) {
		try {
			testFileRepository.saveAll(testStorage);
			view.printMessage(GREEN_COLOR + "Тест: Сохранить все датчики в файл - пройден" + RESET_COLOR);
			logger.logInfo("Тест: Сохранить все датчики в файл - пройден");
		} catch(IOException e) {
			logger.logError(e);
			view.printError("Тест: Сохранить все датчики в файл - провален");
			logger.logErrorMessage("Тест: Сохранить все датчики в файл - провален");
			success = false;
		}
		return success;
	}
	
	private boolean testLoadSensorsFromFile(boolean success) {
		try {
			SensorStorage newStorage = new SensorStorage(count);
			testFileRepository.loadInto(newStorage);
			Sensor[] testArray = testStorage.getAll();
			Sensor[] newArray = newStorage.getAll();
			for (int i = 0; i < newArray.length; i++) {
				if (!(testArray[i].equals(newArray[i]))) {
					view.printError("Тест: Загрузить датчики из файла - провален");
					logger.logErrorMessage("Тест: Загрузить датчики из файла - провален");
					success = false;
					break;
				}
				else if(i == newArray.length-1) {
					view.printMessage(GREEN_COLOR + "Тест: Загрузить датчики из файла - пройден" + RESET_COLOR);
					logger.logInfo("Тест: Загрузить датчики из файла - пройден");
				}
			}
		} catch(Exception e) {
			logger.logError(e);
			view.printError("Тест: Загрузить датчики из файла - провален");
			logger.logErrorMessage("Тест: Загрузить датчики из файла - провален");
			success = false;
		}
		return success;
	}
	
	public boolean runAll() {
		boolean success = true;
		
		success = testCreateSensors(success);
		success = testFindSensors(success);
		success = testFindSensorWithWrongId(success);
		success = testSaveSensorsToFile(success);
		success = testLoadSensorsFromFile(success);
		
		try {
			Files.deleteIfExists(Paths.get("test_sensors.csv"));
		} catch(IOException e) {
			logger.logError(e);
		}
		
		return success;
	}
}
