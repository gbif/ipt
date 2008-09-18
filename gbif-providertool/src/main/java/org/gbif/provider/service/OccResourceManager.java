package org.gbif.provider.service;

import java.util.Collection;
import java.util.List;

import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;

import com.googlecode.gchartjava.GeographicalArea;

public interface OccResourceManager extends GenericResourceManager<OccurrenceResource>{
	// DATA
	public List<StatsCount> taxaByRegion(Long resourceId, RegionType region);
	public List<StatsCount> occByRegion(Long resourceId, RegionType region, Long taxonIdFilter);
	public List<StatsCount> occByTaxon(Long resourceId, Rank rank);
	public List<StatsCount> occByHost(Long resourceId, HostType ht);
	public List<StatsCount> occByBasisOfRecord(Long resourceId);
	public List<StatsCount> occByDateColected(Long resourceId);

	// pie charts
	public String occByRegionPieUrl(Long resourceId, RegionType region, int width, int height, boolean title);
	public String occByRegionPieUrl(List<StatsCount> data, RegionType region, int width, int height, boolean title);

	public String occByTaxonPieUrl(Long resourceId, Rank rank, int width, int height, boolean title);
	public String occByTaxonPieUrl(List<StatsCount> data, Rank rank, int width, int height, boolean title);
	
	public String occByHostPieUrl(Long resourceId, HostType ht, int width, int height, boolean title);
	public String occByHostPieUrl(List<StatsCount> data, HostType ht, int width, int height, boolean title);
	
	public String occByBasisOfRecordPieUrl(Long resourceId, int width, int height, boolean title);
	public String occByBasisOfRecordPieUrl(List<StatsCount> data, int width, int height, boolean title);
	
	// line
	public String occByDateColectedUrl(Long resourceId, int width, int height, boolean title);
	public String occByDateColectedUrl(List<StatsCount> data, int width, int height, boolean title);
	
	// maps
	public String occByCountryMapUrl(GeographicalArea area, Long resourceId, int width, int height);
	public String occByCountryMapUrl(GeographicalArea area, List<StatsCount> data, int width, int height);
	
	public String taxaByCountryMapUrl(GeographicalArea area, Long resourceId, int width, int height);
	public String taxaByCountryMapUrl(GeographicalArea area, List<StatsCount> data, int width, int height);
	
	// helper
	public GeographicalArea getMapArea(String area);

}
