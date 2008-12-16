package org.gbif.provider.webapp.action.portal;


import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class TaxonAction extends BaseDataResourceAction {
	@Autowired
	private MapUtil mapUtil;
	@Autowired
	private TaxonManager taxonManager;
	@Autowired
	private DarwinCoreManager darwinCoreManager;
    private Long id;
    private String guid;
    private Taxon taxon;
    private String category;
    private List<Taxon> taxa;
    private List<Taxon> synonyms;
    private List<StatsCount> stats;
    private List<DarwinCore> occurrences;
	public String geoserverMapUrl;
	public int width = OccResourceStatsAction.DEFAULT_WIDTH;
	public int height = OccResourceStatsAction.DEFAULT_HEIGHT;
	
	private void setRequestedTaxon(){
    	if (id!=null){
    		taxon=taxonManager.get(id);
    	}else if (guid!=null){
    		taxon=taxonManager.get(guid);
    	}
	}
	public String execute(){
		setRequestedTaxon();
    	if (taxon!=null){    		
			stats = taxonManager.getRankStats(taxon.getId());
			synonyms = taxonManager.getSynonyms(taxon.getId());
    	}
		return SUCCESS;
    }
    
    public String list(){
    	if (category!=null){
    		taxa=taxonManager.getAllByRank(resource_id, id, category);
    	}else{
    		taxa=taxonManager.getAll(resource_id);
    	}
		return SUCCESS;
    }
    
    public String occurrences(){
    	if (id!=null && resource_id!=null){
    		taxon=taxonManager.get(id);
    		occurrences = darwinCoreManager.getByTaxon(id, resource_id, true);
			geoserverMapUrl = mapUtil.getGeoserverMapUrl(resource_id, width, height, taxon.getBbox(), taxon, null);
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

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Long getTaxon_id() {
		return id;
	}

	public List<Taxon> getTaxa() {
		return taxa;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<StatsCount> getStats() {
		return stats;
	}

	public List<Taxon> getSynonyms() {
		return synonyms;
	}
	
}