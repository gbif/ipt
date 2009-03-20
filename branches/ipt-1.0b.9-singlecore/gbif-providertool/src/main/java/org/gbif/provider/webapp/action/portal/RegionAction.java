package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TreeNodeManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class RegionAction extends BaseTreeNodeAction<Region, RegionType> {
	public Region getRegion() {
		return node;
	}

	@Autowired
	public RegionAction(RegionManager regionManager) {
		super(regionManager);
	}

	@Override
    public String occurrences(){
		String result = super.occurrences();
		if (node!=null){
	    	occurrences = darwinCoreManager.getByRegion(node.getId(), resource_id, true);
		}
    	return result;
    }

	public Long getRegion_id() {
		return id;
	}
}