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
		setBackground(Color.WHITE);
	}
	
	public void setSensors(Sensor[] sensors) {
		this.sensors = (sensors != null) ? sensors : new Sensor[0];
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		viewport.setPanelSize(getWidth(), getHeight());
		viewport.setPadding(40);
		viewport.fitTo(sensors);
		List<SensorRenderData> data = viewport.toRenderData(sensors);
		for (SensorRenderData renderData: data) {
			double x = renderData.getScreenX();
			double y = renderData.getScreenY();
			double r = renderData.getScreenRadius();
			Ellipse2D circle = new Ellipse2D.Double(x - r/2, y - r/2, 2*r, 2*r);
			g2.setColor(renderData.getFillColor());
			g2.fill(circle);
			g2.setColor(renderData.getColor());
			g2.draw(circle);
			g2.drawString(renderData.getLabel(), (int)x, (int)y);
		}
	}
}
