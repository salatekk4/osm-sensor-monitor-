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

        tileFactory.setUserAgent("OSMSensorMonitor/1.0 (skeletoneyd@gmail.com");
        tileFactory.setThreadPoolSize(8);
        
        mapKit.setZoomSliderVisible(true);
        mapKit.setZoomButtonsVisible(true);

        GeoPosition moscow = new GeoPosition(55.75, 37.62);

        mapKit.setZoom(11);
        mapKit.setAddressLocation(moscow);
	}
	
	public JXMapKit getMapKit() {
		return mapKit;
	}
}
