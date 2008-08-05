package org.gbif.provider.service;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;

public interface OccResourceManager extends ResourceManager<OccurrenceResource>{
	public static final int CHART_WIDTH = 320;
	public static final int CHART_HEIGTH = 160;

	// pie charts
	public String occByRegionPieUrl(Long resourceId, RegionType region);
	public String occByTaxonPieUrl(Long resourceId, Rank rank);
	public String top10TaxaPieUrl(Long resourceId);
	public String occByInstitutionPieUrl(Long resourceId);
	public String occByCollectionPieUrl(Long resourceId);
	public String occByBasisOfRecordPieUrl(Long resourceId);
	// maps
	public String occByCountryMapUrl(Long resourceId);
	public String speciesByCountryMapUrl(Long resourceId);
	// line
	public String occByDateColectedUrl(Long resourceId);
}
