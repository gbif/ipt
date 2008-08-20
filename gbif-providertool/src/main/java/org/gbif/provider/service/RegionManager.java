package org.gbif.provider.service;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;

public interface RegionManager extends TreeNodeManager<Region>{
	/**
	 * Delete all region records for a given resource
	 * @param resource that contains the taxon records to be removed
	 * @return number of deleted regions
	 */
	int deleteAll(OccurrenceResource resource);

}
