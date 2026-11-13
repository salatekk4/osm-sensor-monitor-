package ru.miet.osmsensors.view;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

public class MapLoader {
	private static JXMapKit mapKit;
	public MapLoader() {
		mapKit = new JXMapKit();
		
		TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
		
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        
        mapKit.setTileFactory(tileFactory);

        // Use 8 threads in parallel to load the tiles
        tileFactory.setUserAgent("OSMSensorMonitor/1.0 (skeletoneyd@gmail.com");
        tileFactory.setThreadPoolSize(8);
        
        mapKit.setZoomSliderVisible(true);
        mapKit.setZoomButtonsVisible(true);

        // Set the focus
        GeoPosition zelenograd = new GeoPosition(55.9833, 37.2089);

        mapKit.setZoom(5);
        mapKit.setAddressLocation(zelenograd);
	}
	
	public JXMapKit getMapKit() {
		return mapKit;
	}
}
