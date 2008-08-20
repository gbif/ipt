package org.gbif.provider.service;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;

public interface TaxonManager extends TreeNodeManager<Taxon>{
	/**
	 * Delete all taxon records for a given resource
	 * @param resource that contains the taxon records to be removed
	 * @return number of deleted taxa
	 */
	 int deleteAll(OccurrenceResource resource);
	
}
