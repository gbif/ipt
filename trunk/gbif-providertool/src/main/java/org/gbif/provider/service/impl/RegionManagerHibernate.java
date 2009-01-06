package org.gbif.provider.service.impl;

import org.gbif.provider.model.Region;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.RegionManager;

public class RegionManagerHibernate extends GenericTreeNodeManagerHibernate<Region, RegionType> implements RegionManager {

	public RegionManagerHibernate() {
		super(Region.class);
	}

}
