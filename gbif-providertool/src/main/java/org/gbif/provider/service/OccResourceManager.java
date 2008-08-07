package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;

import com.googlecode.gchartjava.GeographicalArea;

public interface OccResourceManager extends ResourceManager<OccurrenceResource>{
	// pie charts
	public String occByRegionPieUrl(Long resourceId, RegionType region, int width, int height, boolean title);
	public String occByRegionPieUrl(List<StatsCount> data, RegionType region, int width, int height, boolean title);
	public List<StatsCount> occByRegion(Long resourceId, RegionType region);

	public String occByTaxonPieUrl(Long resourceId, Rank rank, int width, int height, boolean title);
	public String occByTaxonPieUrl(List<StatsCount> data, Rank rank, int width, int height, boolean title);
	public List<StatsCount> occByTaxon(Long resourceId, Rank rank);
	
	public String top10TaxaPieUrl(Long resourceId, int width, int height, boolean title);
	public String top10TaxaPieUrl(List<StatsCount> data, int width, int height, boolean title);
	public List<StatsCount> top10Taxa(Long resourceId);
	
	public String occByInstitutionPieUrl(Long resourceId, int width, int height, boolean title);
	public String occByInstitutionPieUrl(List<StatsCount> data, int width, int height, boolean title);
	public List<StatsCount> occByInstitution(Long resourceId);
	
	public String occByCollectionPieUrl(Long resourceId, int width, int height, boolean title);
	public String occByCollectionPieUrl(List<StatsCount> data, int width, int height, boolean title);
	public List<StatsCount> occByCollection(Long resourceId);
	
	public String occByBasisOfRecordPieUrl(Long resourceId, int width, int height, boolean title);
	public String occByBasisOfRecordPieUrl(List<StatsCount> data, int width, int height, boolean title);
	public List<StatsCount> occByBasisOfRecord(Long resourceId);
	
	
	// line
	public String occByDateColectedUrl(Long resourceId, int width, int height, boolean title);
	public String occByDateColectedUrl(List<StatsCount> data, int width, int height, boolean title);
	public List<StatsCount> occByDateColected(Long resourceId);

	
	// maps
	public String occByCountryMapUrl(GeographicalArea area, Long resourceId, int width, int height);
	public String occByCountryMapUrl(GeographicalArea area, List<StatsCount> data, int width, int height);
	public List<StatsCount> occByCountry(Long resourceId);
	
	public String speciesByCountryMapUrl(GeographicalArea area, Long resourceId, int width, int height);
	public String speciesByCountryMapUrl(GeographicalArea area, List<StatsCount> data, int width, int height);
	/**
	 * Get number of distinct species/taxa per country
	 * @param resourceId
	 * @return
	 */
	public List<StatsCount> speciesByCountry(Long resourceId);
}
