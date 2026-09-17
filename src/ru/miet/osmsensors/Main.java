package ru.miet.osmsensors;

import ru.miet.osmsensors.controller.AppController;

public class Main {
    public static void main(String[] args) {
        AppController app = new AppController();
        app.start();
    }
}
