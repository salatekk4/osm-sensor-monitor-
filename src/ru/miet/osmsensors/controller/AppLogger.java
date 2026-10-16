package ru.miet.osmsensors.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import ru.miet.osmsensors.view.ConsoleView;

public class AppLogger {
	private static ArrayList <Exception> errList;
	private static Logger log;
	private static ConsoleView view;
	
	static {
		try {
	        Files.createDirectories(Paths.get("logs"));

	        try (InputStream is = AppLogger.class.getResourceAsStream("logging.properties")) {
	            if (is == null) {
	                throw new IllegalStateException("logging.properties не найден");
	            }
	            LogManager.getLogManager().readConfiguration(is);
	        }
	    } catch (IOException e) {
	    	log.log(Level.SEVERE, e.getMessage());
	    } catch (IllegalStateException e) {
	    	log.log(Level.SEVERE, e.getMessage());
	    }
	}
	
	public AppLogger() throws IOException {
		errList = new ArrayList <Exception>();
		LogManager.getLogManager().readConfiguration(AppLogger.class.getResourceAsStream("logging.properties"));
		log = Logger.getLogger(AppLogger.class.getName());
	}
	
	public AppLogger(ConsoleView view) throws IOException {
		AppLogger.view = view;
		errList = new ArrayList <Exception>();
		LogManager.getLogManager().readConfiguration(AppLogger.class.getResourceAsStream("logging.properties"));
		log = Logger.getLogger(AppLogger.class.getName());
	}
	
	public int addError(Exception e) {
		errList.add(e);
		return errList.size();
	}
	
	public void logInfo(String info) {
		log.log(Level.INFO, info);
	}
	
	public void logWarning(String warning) {
		log.log(Level.WARNING, warning);
	}
	
	public void logErrorMessage(String error) {
		log.log(Level.SEVERE, error);
	}
	
	public int logError(Exception e) {
		errList.add(e);
		String message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
		showErrorMessage(e);
		logErrorMessage(message);
		return errList.size();
	}
	
	public int getErrorCount() {
		return errList.size();
	}
	
	public void showErrorMessage(Exception e) {
		//view.printError(e);
		String message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
		System.err.println(message);
	}
}
