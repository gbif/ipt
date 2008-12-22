package org.gbif.provider.task;

	import java.io.File;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.CacheMap;
import org.springframework.beans.factory.annotation.Autowired;

	/**
	 * Tha main task responsible for uploading raw data into the cache and doing simple preprocessing while iterating through the ImportSource.
	 * Any further secondary postprocessing is done with the help of a second post processing task that this task will automatically schedule once its done.
	 * @author markus
	 *
	 */
	public class ChecklistUploadTask extends ImportTask<Taxon, ChecklistResource> {
		public static final int TASK_TYPE_ID = 7;
		// resource stats
		private ChecklistResource resource;
		private ChecklistResourceManager checklistResourceManager;
		@Autowired
		private ThesaurusManager thesaurusManager;
		@Autowired
		private TaxonManager taxonManager;
		private CacheMap<String, Rank> rankCache = new CacheMap<String, Rank>(250);
		//dataResource stats
		
		@Autowired
		private ChecklistUploadTask(TaxonManager taxonManager, ChecklistResourceManager checklistResourceManager) {
			super(taxonManager, checklistResourceManager);
			this.checklistResourceManager=checklistResourceManager;
		}


		@Override
		protected void prepareHandler(ChecklistResource resource) {
			resource = loadResource();
		}
		
		
		@Override
		protected void recordHandler(Taxon record) {
			Rank dwcRank = rankCache.get(record.getRank()); 
			if (dwcRank==null){
				// query thesaurus to find a matching rank
				ThesaurusConcept rank = thesaurusManager.getConcept(Rank.URI, record.getRank());
				if (rank != null){
					dwcRank = Rank.getByIdentifier(rank.getIdentifier());
				}
				// also keep NULL ranks in cache
				rankCache.put(record.getRank(), dwcRank);
			}
			record.setDwcRank(dwcRank);				
		}


		@Override
		protected void closeHandler(ChecklistResource resource) {
			// lookup parentID, basionymID and acceptedID
			currentActivity = "Processing parent taxa";
			taxonManager.lookupParentTaxa(getResourceId());

			currentActivity = "Processing accepted taxa";
			taxonManager.lookupAcceptedTaxa(getResourceId());

			currentActivity = "Processing basionyms";
			taxonManager.lookupBasionymTaxa(getResourceId());
			
			// create nested set indices
			currentActivity = "Creating taxonomy index";
			taxonManager.buildNestedSet(getResourceId());
			taxonManager.setResourceStats(resource);

			currentActivity = "Creating TCS data archive";
			writeTcsArchive();
		}

		private File writeTcsArchive(){
			//FIXME: implement TCS archive dumping
			return null;
		}

		public int taskTypeId() {
			return TASK_TYPE_ID;
		}

	}
