package ru.miet.osmsensors.model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Модель данных для JTable со списком датчиков. */
public class SensorTableModel extends AbstractTableModel {
    private static final String[] COLUMNS =
        {"ID", "Тип", "X", "Y", "Радиус", "Значение", "Статус", "Авария"};
    private final List<Sensor> rows = new ArrayList<>();

    public void setSensors(Sensor[] sensors) {
        rows.clear();
        if (sensors != null) rows.addAll(List.of(sensors));
        fireTableDataChanged();
    }

    public Sensor getSensorAt(int row) { return rows.get(row); }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int c) { return COLUMNS[c]; }

    @Override public Class<?> getColumnClass(int c) {
        switch (c) {
            case 0: return Integer.class;
            case 2: case 3: case 4: case 5: return Double.class;
            default: return String.class;
        }
    }

    @Override public Object getValueAt(int r, int c) {
        Sensor s = rows.get(r);
        switch (c) {
            case 0: return s.getId();
            case 1: return s.getType();
            case 2: return s.getX();
            case 3: return s.getY();
            case 4: return s.getRadius();
            case 5: return s.getValue();
            case 6: return s.getStatus();
            default: return s.isAlarm() ? "ДА" : "нет";
        }
    }
}
