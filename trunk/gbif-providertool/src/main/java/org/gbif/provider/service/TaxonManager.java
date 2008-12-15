package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.voc.Rank;

public interface TaxonManager extends CoreRecordManager<Taxon>, TreeNodeManager<Taxon>{	
	public void lookupAcceptedTaxa(Long resourceId);
	public void lookupBasionymTaxa(Long resourceId);
	public void lookupParentTaxa(Long resourceId);	
	public void buildNestedSet(Long resourceId);
	public ChecklistResource setResourceStats(ChecklistResource resource);
	
	public List<Taxon> getAllByRank(Long resourceId, String rank);	
}
