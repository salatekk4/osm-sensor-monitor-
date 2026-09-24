package ru.miet.osmsensors.controller;

public class InvalidSensorInputException extends Exception {
    public InvalidSensorInputException(String message) {
        super(message);
    }
}
