package org.gbif.provider.task;

	import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.geo.TransformationUtils;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.OccStatManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.LimitedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
		private LimitedMap<String, Rank> rankCache = new LimitedMap<String, Rank>(250);
		//dataResource stats
		
		@Autowired
		private ChecklistUploadTask(TaxonManager taxonManager, ChecklistResourceManager checklistResourceManager) {
			super(taxonManager, checklistResourceManager);
			this.checklistResourceManager=checklistResourceManager;
		}


		@Override
		public void prepare() {
			super.prepare();
			resource = loadResource();
		}
		
		
		@Override
		protected void recordHandler(Taxon record) {
			Rank dwcRank = rankCache.get(record.getRank()); 
			if (dwcRank==null){
				ThesaurusConcept rank = thesaurusManager.getConcept(Vocabulary.Rank, record.getRank());
				dwcRank = Rank.getByIdentifier(rank.getIdentifier());
				rankCache.put(record.getRank(), dwcRank);
			}
			record.setDwcRank(dwcRank);				
			super.recordHandler(record);
		}


		@Override
		protected void finalHandler(ChecklistResource resource) {
			// lookup parentID, basionymID and acceptedID
			taxonManager.lookupParentTaxa(getResourceId());
			taxonManager.lookupAcceptedTaxa(getResourceId());
			taxonManager.lookupBasionymTaxa(getResourceId());
			// create nested set indices
			taxonManager.buildNestedSet(resource);
			taxonManager.setResourceStats(resource);
			super.finalHandler(resource);
		}


		public int taskTypeId() {
			return TASK_TYPE_ID;
		}

	}
