package org.gbif.provider.webapp.action.portal;


import org.gbif.provider.model.Region;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.RegionManager;
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