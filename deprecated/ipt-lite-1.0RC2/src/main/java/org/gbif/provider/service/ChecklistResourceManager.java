package org.gbif.provider.service;

import java.io.File;
import java.io.IOException;
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
import org.gbif.provider.model.voc.StatusType;

import com.googlecode.gchartjava.GeographicalArea;

public interface ChecklistResourceManager extends GenericResourceManager<ChecklistResource>{

	List<StatsCount> taxByTaxon(Long resourceId, Rank rnk);
	String taxByTaxonPieUrl(List<StatsCount> data, Rank rnk, int width, int height, boolean title);

	List<StatsCount> taxByRank(Long resource_id);
	String taxByRankPieUrl(List<StatsCount> data, int width, int height, boolean title);

	List<StatsCount> taxByStatus(Long resource_id, StatusType type);
	String taxByStatusPieUrl(List<StatsCount> data, StatusType type, int width,	int height, boolean title);
	
	ChecklistResource setResourceStats(ChecklistResource resource);

}
