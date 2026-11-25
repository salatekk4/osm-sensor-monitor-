package ru.miet.osmsensors.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import javax.swing.*;
import java.util.List;
import java.util.HashSet;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.JXMapKit;
import ru.miet.osmsensors.model.*;

@SuppressWarnings("serial")
public class SensorCanvas extends JPanel {
	private Sensor[] sensors;
	private MapViewport viewport;
	private JXMapKit mapKit;
	private JXMapViewer mapViewer;
	private JLayeredPane layeredPane;
	private JPanel tablePanel;
	private JScrollPane scrollPane;
	private SensorTableModel sensorTable;
	private JTable table;
	private JButton addButton;
	private JButton changeButton;
	private JButton deleteButton;
	private JButton resetButton;
	private TextField idField;
	private TextField latField;
	private TextField lonField;
	private TextField radiusField;
	private TextField valueField;
	private TextField typeField;
	private TextField statusField;
	private TextField alarmField;
	private boolean showLabels = false;
	
	public SensorCanvas() {
		sensors = new Sensor[0];
		viewport = new MapViewport();
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(800, 600));
		showLabels = (sensors.length <= 40);
		
		MapLoader map = new MapLoader();
		mapKit = map.getMapKit();
		mapViewer = mapKit.getMainMap();
		
		tablePanel = new JPanel();
		tablePanel.setLayout(new GridBagLayout());
		tablePanel.setPreferredSize(new Dimension(400, 354));
		tablePanel.setOpaque(false);
		
		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 300));
		
		sensorTable = new SensorTableModel();
		table = new JTable(sensorTable);
		scrollPane.setViewportView(table);
		
		addButton = new JButton("Добавить");
		changeButton = new JButton("Изменить");
		deleteButton = new JButton("Удалить");
		resetButton = new JButton("Сброс");
		
		idField = new TextField("ID...");
		latField = new TextField("Широта...");
		lonField = new TextField("Долгота...");
		radiusField = new TextField("Радиус...");
		valueField = new TextField("Значение...");
		typeField = new TextField("Тип...");
		statusField = new TextField("Статус...");
		alarmField = new TextField("Авария?");
		JPanel fieldPanel = new JPanel();
		fieldPanel.setPreferredSize(new Dimension(400, 26));
		fieldPanel.setLayout(new GridLayout());
		fieldPanel.add(idField, 0, 0);
		fieldPanel.add(latField, 0, 1);
		fieldPanel.add(lonField, 0, 2);
		fieldPanel.add(radiusField, 0, 3);
		fieldPanel.add(valueField, 0, 4);
		fieldPanel.add(typeField, 0, 5);
		fieldPanel.add(statusField, 0, 6);
		fieldPanel.add(alarmField, 0, 7);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 4;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		tablePanel.add(scrollPane, gbc);
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weighty = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		tablePanel.add(addButton, gbc);
		gbc.gridx = 1;
		tablePanel.add(changeButton, gbc);
		gbc.gridx = 2;
		tablePanel.add(deleteButton, gbc);
		gbc.gridx = 3;
		tablePanel.add(resetButton, gbc);
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.weighty = 0.1;
		tablePanel.add(fieldPanel, gbc);
		
		
		layeredPane = new JLayeredPane() {
			public Dimension getPreferredSize() {
				return getParent().getSize();
			}
			
			public void doLayout() {
				mapKit.setBounds(0, 0, getWidth(), getHeight());
				tablePanel.setBounds(getWidth()-400, 0, 400, 354);
			}
		};
		layeredPane.add(mapKit, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(tablePanel, JLayeredPane.DEFAULT_LAYER+50);
		
		add(layeredPane, BorderLayout.CENTER);
		
		mapKit.getMainMap().setOverlayPainter((g2, viewer, w, h) -> {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        	viewport.setPanelSize(getWidth(), getHeight());
        	viewport.setPadding(40);
        	viewport.fitTo(sensors);
        	List<SensorRenderData> data = viewport.toRenderData(sensors);
        	for (SensorRenderData renderData: data) {
        		Sensor sensor = renderData.getSensor();
        		Point2D point = viewer.convertGeoPositionToPoint(new org.jxmapviewer.viewer.GeoPosition(sensor.getY(), sensor.getX()));
        		double x = point.getX();
        		double y = point.getY();
        		int osmZoom = viewer.getTileFactory().getInfo().getTotalMapZoom() - viewer.getZoom();
        		double metersPerPixel = 40075016.686 * Math.cos(Math.toRadians(sensor.getY())) / (256 * Math.pow(2, osmZoom));
        		double r = Math.max(sensor.getRadius() / metersPerPixel, 4);
        		Ellipse2D circle = new Ellipse2D.Double(x - r, y - r, 2*r, 2*r);
        		g2.setColor(renderData.getFillColor());
        		g2.fill(circle);
        		g2.setColor(renderData.getColor());
        		g2.draw(circle);
        		if (showLabels) {
        			g2.drawString(renderData.getLabel(), (int)x, (int)y);
        		}
        	}
		});
		
		mapViewer.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				for (Sensor sensor: sensors) {
					Point2D point = mapViewer.convertGeoPositionToPoint(new org.jxmapviewer.viewer.GeoPosition(sensor.getY(), sensor.getX()));
	        		double x = point.getX();
	        		double y = point.getY();
	        		int osmZoom = mapViewer.getTileFactory().getInfo().getTotalMapZoom() - mapViewer.getZoom();
	        		double metersPerPixel = 40075016.686 * Math.cos(Math.toRadians(sensor.getY())) / (256 * Math.pow(2, osmZoom));
	        		double r = Math.max(sensor.getRadius() / metersPerPixel, 4);
					if (e.getPoint().distance(point) <= r) {
						selectSensorById(sensor.getId());
						selectSensor(sensor);
						break;
					}
				}
			}
		});
	}
	
	public void setSensors(Sensor[] sensors) {
		this.sensors = (sensors != null) ? sensors : new Sensor[0];
		showLabels = (this.sensors.length <= 40);
		mapKit.repaint();
		sensorTable.setSensors(this.sensors);
	}
	
	public void fitMapToSensors() {
		HashSet<org.jxmapviewer.viewer.GeoPosition> points = new HashSet<org.jxmapviewer.viewer.GeoPosition> ();
		for (Sensor sensor: sensors) {
			points.add(new org.jxmapviewer.viewer.GeoPosition(sensor.getY(), sensor.getX()));
		}
		mapViewer.zoomToBestFit(points, 0.9);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		layeredPane.moveToFront(tablePanel);
	}
	
	public void selectSensorById(int id) {
		
	}
	
	public void selectSensor(Sensor sensor) {
		idField.setForeground(Color.BLACK);
		idField.setEditable(true);
		idField.setText(String.valueOf(sensor.getId()));
		latField.setForeground(Color.BLACK);
		latField.setEditable(true);
		latField.setText(String.format("%6.3f", sensor.getY()));
		lonField.setForeground(Color.BLACK);
		lonField.setEditable(true);
		lonField.setText(String.format("%6.3f", sensor.getX()));
		radiusField.setForeground(Color.BLACK);
		radiusField.setEditable(true);
		radiusField.setText(String.format("%5.3f", sensor.getRadius()));
		valueField.setForeground(Color.BLACK);
		valueField.setEditable(true);
		valueField.setText(String.format("%6.3f", sensor.getValue()));
		typeField.setForeground(Color.BLACK);
		typeField.setEditable(true);
		typeField.setText(sensor.getType());
		statusField.setForeground(Color.BLACK);
		statusField.setEditable(true);
		statusField.setText(sensor.getStatus());
		alarmField.setForeground(Color.BLACK);
		alarmField.setEditable(true);
		alarmField.setText(sensor.isAlarm() ? "ДА" : "нет");
	}
}
