package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class RegionTreeAction extends BaseTreeAction<Region, RegionType> {
	@Autowired
    public RegionTreeAction(RegionManager regionManager) {
		super(regionManager, "region");
	}
   
}