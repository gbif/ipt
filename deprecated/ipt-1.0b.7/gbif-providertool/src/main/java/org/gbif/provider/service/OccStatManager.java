package org.gbif.provider.service;

import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;

public interface OccStatManager extends GenericResourceRelatedManager<OccStatByRegionAndTaxon>{

	void updateRegionAndTaxonStats(OccurrenceResource resource);

}
