package ru.miet.osmsensors.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridLayout;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import ru.miet.osmsensors.model.*;
import ru.miet.osmsensors.view.SensorCanvas;

public class GuiController {
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 750;
    private static final Integer[] COUNT_CHOICES = {50, 100, 200, 300};
    private static final int DEFAULT_COUNT = 200;
    private static final String DEFAULT_SEED = "42";

    private static final boolean GEO_MODE = false;

    private static final double MIN_LON = 35.2;
    private static final double MAX_LON = 40.0;
    private static final double MIN_LAT = 54.5;
    private static final double MAX_LAT = 56.8;
    private static final double MIN_RADIUS_M = 1000;
    private static final double MAX_RADIUS_M = 5000;

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
    private JComboBox<Integer> countBox;
    private JTextField seedField;
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

        SwingUtilities.invokeLater(this::generateSensors);
    }

    public static boolean askLogin(BiPredicate<String, String> check, int attempts) {
        boolean[] success = {false};
        try {
            SwingUtilities.invokeAndWait(() -> success[0] = showLoginDialog(check, attempts));
        } catch (InvocationTargetException e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            System.err.println("Ошибка при входе: " + cause);
            cause.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return success[0];
    }

    private static boolean showLoginDialog(BiPredicate<String, String> check, int attempts) {
        JTextField loginField = new JTextField(16);
        JPasswordField passwordField = new JPasswordField(16);
        loginField.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(loginField::requestFocusInWindow);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel("Логин:"));
        form.add(loginField);
        form.add(new JLabel("Пароль:"));
        form.add(passwordField);

        for (int attempt = 1; attempt <= attempts; attempt++) {
            int choice = JOptionPane.showConfirmDialog(null, form, "Вход — OSM Sensor Monitor",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return false;
            }
            if (check.test(loginField.getText().trim(), new String(passwordField.getPassword()))) {
                return true;
            }
            passwordField.setText("");
            JOptionPane.showMessageDialog(null,
                    "Неверный логин или пароль. Осталось попыток: " + (attempts - attempt),
                    "Ошибка входа", JOptionPane.ERROR_MESSAGE);
        }
        return false;
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

        countBox = new JComboBox<>(COUNT_CHOICES);
        countBox.setSelectedItem(DEFAULT_COUNT);
        seedField = new JTextField(DEFAULT_SEED, 5);
        seedField.setToolTipText("Одинаковый seed даёт одинаковый набор датчиков. Пусто = случайный набор.");

        panel.add(createCaption("Датчиков:"));
        panel.add(countBox);
        panel.add(createCaption("Seed:"));
        panel.add(seedField);
        panel.add(loadButton);
        panel.add(generateButton);
        panel.add(alarmButton);
        return panel;
    }

    private JLabel createCaption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(STATUS_FONT);
        label.setForeground(Color.WHITE);
        return label;
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
        Random random;
        String seedText = seedField.getText().trim();
        if (seedText.isEmpty()) {
            random = new Random();
        } else {
            try {
                random = new Random(Long.parseLong(seedText));
            } catch (NumberFormatException e) {
                statusLabel.setText("Seed должен быть целым числом (или пустым для случайного набора)");
                return;
            }
        }

        int requested = (Integer) countBox.getSelectedItem();
        clearStorage();
        SensorFactory factory = new SensorFactory(random);
        Sensor[] created = factory.createSensors(Math.min(requested, storage.getCapacity()));
        for (Sensor sensor : created) {
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
        fitMapToSensors();
    }

    private Sensor[] createGeoSensors(int count, Random random) {
        String[] statuses = {"OK", "WARNING", "ERROR"};
        Sensor[] result = new Sensor[count];
        for (int i = 0; i < count; i++) {
            int id = i + 1;
            double x = MIN_LON + random.nextDouble() * (MAX_LON - MIN_LON);
            double y = MIN_LAT + random.nextDouble() * (MAX_LAT - MIN_LAT);
            double radius = MIN_RADIUS_M + random.nextDouble() * (MAX_RADIUS_M - MIN_RADIUS_M);
            String status = statuses[random.nextInt(statuses.length)];

            switch (random.nextInt(3)) {
                case 0:
                    result[i] = new TemperatureSensor(id, x, y, radius, -30 + random.nextDouble() * 100, status);
                    break;
                case 1:
                    result[i] = new Co2Sensor(id, x, y, radius, 300 + random.nextDouble() * 1200, status);
                    break;
                default:
                    result[i] = new GasSensor(id, x, y, radius, random.nextDouble() * 100, status);
            }
        }
        return result;
    }

    private void fitMapToSensors() {

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