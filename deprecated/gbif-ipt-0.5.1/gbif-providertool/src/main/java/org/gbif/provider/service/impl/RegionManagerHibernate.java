package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.hibernate.Session;

public class RegionManagerHibernate extends GenericTreeNodeManagerHibernate<Region> implements RegionManager {

	public RegionManagerHibernate() {
		super(Region.class);
	}

}
