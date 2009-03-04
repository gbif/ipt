package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class RegionAction extends BaseOccurrenceResourceAction {
	@Autowired
	private MapUtil mapUtil;
	@Autowired
	private RegionManager regionManager;
	@Autowired
	private DarwinCoreManager darwinCoreManager;
    private Long id;
    private Region region;
    private List<DarwinCore> occurrences;
    private String geoserverMapUrl;
	private String geoserverMapBBox;
	public int width = OccResourceStatsAction.DEFAULT_WIDTH;
	public int height = OccResourceStatsAction.DEFAULT_HEIGHT;
	 
    public String execute(){
    	if (id!=null){
    		region=regionManager.get(id);
			// geoserver map link
    		if (region!=null){
    			geoserverMapUrl = mapUtil.getWMSGoogleMapUrl(resource_id, null, region);
    			if (region.getBbox()!=null && region.getBbox().isValid()){
    				geoserverMapBBox = region.getBbox().toStringWMS();
    			}else{
    				geoserverMapBBox = resource.getGeoCoverage().toStringWMS();
    			}
    		}else{
        		return RECORD404;
    		}
    	}
		return SUCCESS;
    }
    
    public String occurrences(){
    	if (resource_id!=null && id!=null){
    		region=regionManager.get(id);
    		occurrences = darwinCoreManager.getByRegion(region.getId(), resource_id, true);
    		if (region!=null){
    			geoserverMapUrl = mapUtil.getWMSGoogleMapUrl(resource_id, null, region);
    			if (region.getBbox()!=null && region.getBbox().isValid()){
    				geoserverMapBBox = region.getBbox().toStringWMS();
    			}else{
    				geoserverMapBBox = resource.getGeoCoverage().toStringWMS();
    			}
    		}else{
        		return RECORD404;
    		}
    	}
		return SUCCESS;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<DarwinCore> getOccurrences() {
		return occurrences;
	}

	public Region getRegion() {
		return region;
	}

	public String getGeoserverMapUrl() {
		return geoserverMapUrl;
	}

	public String getGeoserverMapBBox() {
		return geoserverMapBBox;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Long getRegion_id() {
		return id;
	}

}