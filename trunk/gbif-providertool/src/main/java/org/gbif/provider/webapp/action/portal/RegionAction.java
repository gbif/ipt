package org.gbif.provider.webapp.action.portal;


import java.util.List;

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
	private RegionManager regionManager;
	@Autowired
	private DarwinCoreManager darwinCoreManager;
    private Long id;
    private Region region;
    private List<DarwinCore> occurrences;
	public String geoserverMapUrl;
	 
    public String execute(){
    	if (id!=null){
    		region=regionManager.get(id);
    	}
		return SUCCESS;
    }
    
    public String occurrences(){
    	if (id!=null && resource_id!=null){
    		region=regionManager.get(id);
    		occurrences = darwinCoreManager.getByRegion(id, resource_id, true);
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

	public List<DarwinCore> getOccurrences() {
		return occurrences;
	}

	public Region getRegion() {
		return region;
	}

}