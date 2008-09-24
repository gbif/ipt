package org.gbif.provider.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.springframework.beans.factory.annotation.Autowired;


public class MapUtil {
	@Autowired
	private AppConfig cfg;
	
	public String getGeoserverMapUrl(Long resourceId, int width, int height, BBox bbox, Taxon taxon, Region region){
		if (bbox == null || !bbox.isValid()){
			bbox = BBox.NewWorldInstance();
		}else{
			bbox.resize(1.2f);
			float ratio = (float) width / (float) height;
			bbox.expandToMapRatio(ratio);
		}
		// FIXME: add taxon+region filter to FeatureType
		LinkedList<String> filters = new LinkedList<String>();
		
		filters.add(String.format("<PropertyIsEqualTo><PropertyName>ResourceId</PropertyName><Literal>%s</Literal></PropertyIsEqualTo>", resourceId));
		if (taxon != null){
			filters.add(String.format("<PropertyIsEqualTo><PropertyName>TaxonId</PropertyName><Literal>%s</Literal></PropertyIsEqualTo>", taxon.getId()));
		}
		if (region != null){
			filters.add(String.format("<PropertyIsEqualTo><PropertyName>RegionId</PropertyName><Literal>%s</Literal></PropertyIsEqualTo>", region.getId()));
		}
		
		// produce final filter string
		String filter = filters.poll();
		while (!filters.isEmpty()){
			filter = String.format("<And>%s%s</And>", filters.poll(), filter);
		}
		filter = String.format("<Filter>%s</Filter>", filter);
		// produce entire WMS URL
		try {
			return String.format("%s/wms?bbox=%s&styles=&request=GetMap&layers=gbif:countries,gbif:occurrence&width=%s&height=%s&srs=EPSG:4326&bgcolor=0x7391AD&Format=image/jpeg&filter=()(%s)", cfg.getGeoserverUrl(), bbox.toWMSString(), width, height, URLEncoder.encode(filter, Constants.ENCODING));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
