package ru.miet.osmsensors.view;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.List;
import ru.miet.osmsensors.model.*;

@SuppressWarnings("serial")
public class SensorCanvas extends JPanel {
	private Sensor[] sensors;
	private MapViewport viewport;
	
	public SensorCanvas() {
		sensors = new Sensor[0];
		viewport = new MapViewport();
		viewport.setPanelSize(getWidth(), getHeight());
	}
	
	public void setSensors(Sensor[] sensors) {
		this.sensors = sensors;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		viewport.fitTo(sensors);
		List<SensorRenderData> data = viewport.toRenderData(sensors);
		for (SensorRenderData renderData: data) {
			g2.setColor(renderData.getColor());
			g2.drawString(renderData.getLabel(), (int)renderData.getScreenX(), (int)renderData.getScreenY());
			Ellipse2D circle = new Ellipse2D.Double(renderData.getScreenX(), renderData.getScreenY(), renderData.getScreenRadius(), renderData.getScreenRadius());
			g2.draw(circle);
			g2.setColor(renderData.getFillColor());
			g2.fill(circle);
		}
	}
}
