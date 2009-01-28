package org.gbif.provider.geo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;


public class MapUtil {
	@Autowired
	private AppConfig cfg;
	
	public String getGeoserverMapUrl(Long resourceId, int width, int height, BBox bbox, Taxon taxon, Region region){
		if (bbox == null || !bbox.isValid()){
			bbox = BBox.NewWorldInstance();
		}else{
			if (bbox.surface()==0f){
				if (bbox.getMax().getLatitude()+0.1f < Point.MAX_LATITUDE){
					bbox.getMax().setLatitude(bbox.getMax().getLatitude()+0.1f);
					bbox.getMax().setLongitude(bbox.getMax().getLongitude()+0.1f);
				}else{
					bbox.getMin().setLatitude(bbox.getMin().getLatitude()-0.1f);
					bbox.getMin().setLongitude(bbox.getMin().getLongitude()-0.1f);
				}
			}
			bbox.resize(1.2f);
			float ratio = (float) width / (float) height;
			bbox.expandToMapRatio(ratio);
		}
		LinkedList<String> filters = new LinkedList<String>();
		
		if (taxon != null){
			filters.add(String.format("<PropertyIsEqualTo><PropertyName>TaxonId</PropertyName><Literal>%s</Literal></PropertyIsEqualTo>", taxon.getId()));
		}
		if (region != null){
			filters.add(String.format("<PropertyIsEqualTo><PropertyName>RegionId</PropertyName><Literal>%s</Literal></PropertyIsEqualTo>", region.getId()));
		}
		
		// produce final filter string
		String filter="";
		if (!filters.isEmpty()){
			filter = filters.poll();
			while (!filters.isEmpty()){
				filter = String.format("<And>%s%s</And>", filters.poll(), filter);
			}
			filter = String.format("<Filter>%s</Filter>", filter);
		}
		// produce entire WMS URL
		try {
			return String.format("%s/wms?bbox=%s&styles=&request=GetMap&layers=gbif:countries,gbif:resource%s&width=%s&height=%s&srs=EPSG:4326&bgcolor=0x7391AD&Format=image/jpeg&filter=()(%s)", cfg.getGeoserverUrl(), bbox.toStringWMS(), resourceId, width, height, URLEncoder.encode(filter, Constants.ENCODING));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
