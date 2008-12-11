package org.gbif.provider.service;

import java.util.Collection;
import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;

import com.googlecode.gchartjava.GeographicalArea;

public interface ChecklistResourceManager extends GenericResourceManager<ChecklistResource>{

	List<StatsCount> taxByTaxon(Long resource_id, Rank rnk);
	String taxByTaxonPieUrl(List<StatsCount> data, Rank rnk, int width, int height, boolean title);

	List<StatsCount> taxByStatus(Long resource_id, HostType ht);
	String taxByStatusPieUrl(List<StatsCount> data, HostType ht, int width,	int height, boolean title);

}
