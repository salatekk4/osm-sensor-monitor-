package ru.miet.osmsensors.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.view.SensorCanvas;

public class GuiController {
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 750;
    private static final int GENERATE_COUNT = 15;

    // Цвета и шрифты оформления
    private static final Color HEADER_COLOR = new Color(31, 41, 55);
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SECONDARY_COLOR = new Color(75, 85, 99);
    private static final Color ALARM_COLOR = new Color(220, 38, 38);
    private static final Color STATUS_COLOR = new Color(243, 244, 246);
    private static final Color STATUS_TEXT = new Color(75, 85, 99);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font STATUS_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final SensorRepository storage;
    private final SensorFileRepository fileRepository;
    private final AppLogger logger;

    private SensorCanvas canvas;
    private JLabel statusLabel;
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
        JFrame frame = new JFrame("OSM Sensor Monitor");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        canvas = new SensorCanvas();
        frame.add(createHeader(), BorderLayout.NORTH);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(createStatusBar(), BorderLayout.SOUTH);

        refreshCanvas();
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("OSM Sensor Monitor");
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        header.add(createButtonPanel(), BorderLayout.EAST);
        return header;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        JButton loadButton = new JButton("Загрузить датчики");
        styleButton(loadButton, PRIMARY_COLOR);
        loadButton.addActionListener(e -> loadFromFile());

        JButton generateButton = new JButton("Сгенерировать новые");
        styleButton(generateButton, SECONDARY_COLOR);
        generateButton.addActionListener(e -> generateSensors());

        JToggleButton alarmButton = new JToggleButton("Фильтр аварийных");
        styleButton(alarmButton, SECONDARY_COLOR);
        alarmButton.addActionListener(e -> {
            alarmOnly = alarmButton.isSelected();
            alarmButton.setBackground(alarmOnly ? ALARM_COLOR : SECONDARY_COLOR);
            refreshCanvas();
        });

        panel.add(loadButton);
        panel.add(generateButton);
        panel.add(alarmButton);
        return panel;
    }

    private void styleButton(AbstractButton button, Color color) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boolean alarmPressed = button instanceof JToggleButton && button.isSelected();
                button.setBackground(alarmPressed ? ALARM_COLOR : color);
            }
        });
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(STATUS_COLOR);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(STATUS_FONT);
        statusLabel.setForeground(STATUS_TEXT);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
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
        for (Sensor sensor : fromFile) {
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
        statusLabel.setText("Показано датчиков: " + shown.length + " из " + storage.getCount()
                + (alarmOnly ? "   •   только аварийные" : ""));
    }

    private void clearStorage() {
        for (Sensor sensor : storage.getAll()) {
            try {
                storage.deleteById(sensor.getId());
            } catch (SensorNotFoundException e) {
                logger.logError(e);
            }
        }
    }
}