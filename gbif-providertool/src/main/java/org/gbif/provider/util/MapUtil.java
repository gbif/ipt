package org.gbif.provider.util;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.springframework.beans.factory.annotation.Autowired;

public class MapUtil {
	@Autowired
	private AppConfig cfg;
	
	public String getGeoserverMapUrl(Long resourceId, int width, int height, BBox bbox, Taxon taxon, Region region){
		if (bbox == null){
			bbox = new BBox(new Point(90f,45f), new Point(-90f,-45f));
		}else{
			double ratio = (double) height / (double) width;
			bbox.fitRatio(ratio);			
		}
		// FIXME: add taxon+region filter
		// filter: "<Filter><PropertyIsEqualTo><PropertyName>ScientificName</PropertyName><Literal>Arenaria acerosa Boiss.</Literal></PropertyIsEqualTo></Filter>"
		return String.format("%s/wms?bbox=%s&styles=&request=GetMap&layers=gbif:countries,gbif:occurrence&width=%s&height=%s&srs=EPSG:4326&Format=image/jpeg", cfg.getGeoserverUrl(), bbox.toWMSString(), width, height);
	}
}
