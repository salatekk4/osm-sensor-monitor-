package ru.miet.osmsensors.view;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.List;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.JXMapKit;
import ru.miet.osmsensors.model.*;

@SuppressWarnings("serial")
public class SensorCanvas extends JPanel {
	private Sensor[] sensors;
	private MapViewport viewport;
	private JXMapKit mapKit;
	private JXMapViewer mapViewer;
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
		
		add(mapKit, BorderLayout.CENTER);
		
		mapKit.getMainMap().setOverlayPainter((g2, viewer, w, h) -> {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        	viewport.setPanelSize(getWidth(), getHeight());
        	viewport.setPadding(40);
        	viewport.fitTo(sensors);
        	List<SensorRenderData> data = viewport.toRenderData(sensors);
        	for (SensorRenderData renderData: data) {
        		Sensor sensor = renderData.getSensor();
        		if (sensor.getId() == 1) System.out.println(sensor);
        		Point2D point = mapViewer.convertGeoPositionToPoint(new org.jxmapviewer.viewer.GeoPosition(sensor.getY(), sensor.getX()));
        		double x = point.getX();
        		//double x = renderData.getScreenX();
        		double y = point.getY();
        		//double y = renderData.getScreenY();
        		double r = sensor.getRadius() / (40075016.686 * Math.cos(sensor.getY() / (256 * Math.pow(2, mapViewer.getZoom()))));
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
	}
	
	public void setSensors(Sensor[] sensors) {
		this.sensors = (sensors != null) ? sensors : new Sensor[0];
		showLabels = (sensors.length <= 40);
		mapKit.repaint();
	}
}
