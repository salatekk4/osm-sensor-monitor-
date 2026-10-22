package ru.miet.osmsensors.controller;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.view.SensorCanvas;

public class GuiController {
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    private static final int GENERATE_COUNT = 15;

    private final SensorRepository storage;
    private final SensorFileRepository fileRepository;
    private final AppLogger logger;

    private SensorCanvas canvas;
    private boolean alarmOnly = false;

    public GuiController(SensorRepository storage, SensorFileRepository fileRepository, AppLogger logger) {
        this.storage = storage;
        this.fileRepository = fileRepository;
        this.logger = logger;
    }

    public void start() {
        SwingUtilities.invokeLater(this::createWindow);
    }

    private void createWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.logError(e);
        }

        JFrame frame = new JFrame("OSM Sensor Monitor");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        canvas = new SensorCanvas();
        frame.add(createButtonPanel(), BorderLayout.NORTH);
        frame.add(canvas, BorderLayout.CENTER);

        refreshCanvas();
        frame.setVisible(true);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton loadButton = new JButton("Загрузить датчики");
        loadButton.addActionListener(e -> loadFromFile());

        JButton generateButton = new JButton("Сгенерировать новые");
        generateButton.addActionListener(e -> generateSensors());

        JToggleButton alarmButton = new JToggleButton("Фильтр аварийных");
        alarmButton.addActionListener(e -> {
            alarmOnly = alarmButton.isSelected();
            refreshCanvas();
        });

        panel.add(loadButton);
        panel.add(generateButton);
        panel.add(alarmButton);
        return panel;
    }

    private void loadFromFile() {
        List<Sensor> fromFile;
        try {
            fromFile = fileRepository.readAll();
        } catch (IOException | SensorFileFormatException e) {
            logger.logError(e);
            return;
        }
        clearStorage();
        for (Sensor sensor: fromFile) {
            try {
                storage.addUnique(sensor);
            } catch (DuplicateSensorException e) {
                logger.logError(e);
            } catch (StorageFullException e) {
                logger.logError(e);
                break;
            }
        }
        refreshCanvas();
    }

    private void generateSensors() {
        clearStorage();
        SensorFactory factory = new SensorFactory(new Random());
        for (Sensor sensor : factory.createSensors(Math.min(GENERATE_COUNT, storage.getCapacity()))) {
            try {
                storage.addUnique(sensor);
            } catch (DuplicateSensorException e) {
                logger.logError(e);
            } catch (StorageFullException e) {
                logger.logError(e);
                break;
            }
        }
        refreshCanvas();
    }

    private void refreshCanvas() {
        Sensor[] shown = alarmOnly ? storage.findAlarmSensors() : storage.getAll();
        canvas.setSensors(shown);
        canvas.repaint();
    }

    private void clearStorage() {
        for (Sensor sensor: storage.getAll()) {
            try {
                storage.deleteById(sensor.getId());
            } catch (SensorNotFoundException e) {
                logger.logError(e);
            }
        }
    }
}
