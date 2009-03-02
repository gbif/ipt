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
	
	public String getWMSGoogleMapUrl(Long resourceId, Taxon taxon, Region region){
		// ESPG:900913
		// tile size=256
		// background=Transparent
		String wms="";
		try {
			wms = getWMSUrl(resourceId, 256, 256, taxon, region, URLEncoder.encode("EPSG:900913", Constants.ENCODING));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wms+"&background=Transparent";
	}
	public String getWMSUrl(Long resourceId, int width, int height, Taxon taxon, Region region){
		// uses default WGS84
		try {
			return getWMSUrl(resourceId, width, height, taxon, region, URLEncoder.encode("EPSG:4326", Constants.ENCODING));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	public String getWMSUrl(Long resourceId, int width, int height, Taxon taxon, Region region, String srs){
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
			return String.format("%s/wms?request=GetMap&layers=%s&srs=%s&Format=image/png&width=%s&height=%s&filter=(%s)", cfg.getGeoserverUrl(), URLEncoder.encode("gbif:resource"+resourceId, Constants.ENCODING), srs, width, height, URLEncoder.encode(filter, Constants.ENCODING));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}		
	}
	public String getWMSUrl(Long resourceId, int width, int height, BBox bbox, Taxon taxon, Region region){
		String wms = getWMSUrl(resourceId, width, height, taxon, region);
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
		// add bbox and size to WMS URL
		return String.format("%s&bbox=%s", wms, bbox.toStringWMS(), width, height);
	}
}
