package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;

public class RegionManagerHibernate extends GenericManagerHibernate<Region> implements RegionManager {

	public RegionManagerHibernate() {
		super(Region.class);
	}

	public List<Region> getChildren(Long resourceId, Region node) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}

	public List<Region> getDescendants(Long resourceId, Region node) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}

	public List<Region> getParents(Long resourceId, Region node) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}

	public List<Region> getRoots(Long resourceId) {
		List<Region> result = new ArrayList<Region>(); 
		return result;
	}


}
