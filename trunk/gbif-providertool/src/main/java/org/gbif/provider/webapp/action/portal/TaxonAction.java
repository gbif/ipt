package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.MapUtil;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxonAction extends BaseOccurrenceResourceAction {
	@Autowired
	private MapUtil mapUtil;
	@Autowired
	private TaxonManager taxonManager;
	@Autowired
	private DarwinCoreManager darwinCoreManager;
    private Long id;
    private Taxon taxon;
    private List<DarwinCore> occurrences;
	public String geoserverMapUrl;
	public static int width = OccResourceStatsAction.DEFAULT_WIDTH;
	public static int height = OccResourceStatsAction.DEFAULT_HEIGHT;
	 
    public String execute(){
    	if (id!=null){
    		taxon=taxonManager.get(id);
			// geoserver map link
			geoserverMapUrl = mapUtil.getGeoserverMapUrl(resource_id, width, height, taxon.getBbox(), taxon, null);
    	}
		return SUCCESS;
    }
    
    public String occurrences(){
    	if (id!=null && resource_id!=null){
    		taxon=taxonManager.get(id);
    		occurrences = darwinCoreManager.getByTaxon(id, resource_id, true);
    		geoserverMapUrl = "http://chart.apis.google.com/chart?cht=t&chs=320x160&chd=s:_&chtm=world";
    	}
		return SUCCESS;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Taxon getTaxon() {
		return taxon;
	}

	public List<DarwinCore> getOccurrences() {
		return occurrences;
	}

	public String getGeoserverMapUrl() {
		return geoserverMapUrl;
	}

	public static int getWidth() {
		return width;
	}

	public static int getHeight() {
		return height;
	}

}