package ru.miet.osmsensors.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AppLogger {
	private static ArrayList <Exception> errList;
	private static Logger log;
	
	public AppLogger() throws IOException {
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
	
	public int logError(Exception e) {
		errList.add(e);
		log.log(Level.SEVERE, e.getMessage());
		return errList.size();
	}
	
	public int getErrorCount() {
		return errList.size();
	}
	
	public void showErrorMessage(Exception e) {
		System.err.println(e.getMessage());
	}
	
	public Exception makeErr(Exception e) {
		addError(e);
		return new Exception(e);
	}
}
