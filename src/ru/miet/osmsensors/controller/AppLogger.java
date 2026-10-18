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

	        InputStream is = AppLogger.class.getResourceAsStream("logging.properties");
	        if (is == null) {
	        	throw new IllegalStateException("logging.properties не найден");
	        }
	        LogManager.getLogManager().readConfiguration(is);
			errList = new ArrayList <Exception>();
			log = Logger.getLogger(AppLogger.class.getName());
	    } catch (IOException e) {
	    	String message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
	    	System.err.println(message);
	    } catch (IllegalStateException e) {
	    	String message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
	    	System.err.println(message);
	    }
	}
	
	public AppLogger(ConsoleView view) {
		AppLogger.view = view;
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
		view.printError(e);
	}
}
