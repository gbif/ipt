package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;

public interface TaxonManager extends CoreRecordManager<Taxon>, TreeNodeManager<Taxon, Rank>{	
	public void lookupAcceptedTaxa(Long resourceId);
	public void lookupBasionymTaxa(Long resourceId);
	public void lookupParentTaxa(Long resourceId);	
	// stats counting
	public int countSynonyms(Long resourceId);
	public int countAccepted(Long resourceId);

	/** count all accepted taxa grouped by rank
	 * @param taxonId
	 * @return
	 */
	public List<StatsCount> getRankStats(Long taxonId);
	public List<Taxon> getSynonyms(Long taxonId);
	/** get accepted taxa matching a rank string and with an optional optional highest root taxon
	 * If a root taxon is submitted, only descendants of this taxon are returned matching the rank. 
	 * @param resourceId 
	 * @param taxonId the optional root taxon. can be null
	 * @param rank the war rank string to look for 
	 * @return
	 */
	public List<Taxon> getByRank(Long resourceId, Long taxonId, String rank);
	/** get accepted taxa by their nomenclatural or taxonomical status and optional highest root taxon.
	 * If a root taxon is submitted, only descendants of this taxon are returned matching the status. 
	 * @param resourceId
	 * @param taxonId the optional root taxon
	 * @param st the kind of status, nomenclatural or taxonomic
	 * @param status the status to match
	 * @return
	 */
	public List<Taxon> getByStatus(Long resourceId, Long taxonId, StatusType st, String status);
}
