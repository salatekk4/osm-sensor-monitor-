package ru.miet.osmsensors.view;

import java.awt.*;
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
        		Point2D point = mapViewer.convertGeoPositionToPoint(new org.jxmapviewer.viewer.GeoPosition(sensor.getY(), sensor.getX()));
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
	}
	
	public void setSensors(Sensor[] sensors) {
		this.sensors = (sensors != null) ? sensors : new Sensor[0];
		showLabels = (this.sensors.length <= 40);
		mapKit.repaint();
	}
	

	public void fitMapToSensors() {
		HashSet<org.jxmapviewer.viewer.GeoPosition> points = new HashSet<org.jxmapviewer.viewer.GeoPosition> ();
		for (Sensor sensor: sensors) {
			points.add(new org.jxmapviewer.viewer.GeoPosition(sensor.getY(), sensor.getX()));
		}
		mapViewer.zoomToBestFit(points, 0.9);
	}
}
