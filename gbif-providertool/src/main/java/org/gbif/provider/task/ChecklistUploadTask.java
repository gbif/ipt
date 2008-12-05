package org.gbif.provider.task;

	import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.geo.TransformationUtils;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.OccStatManager;
import org.gbif.provider.service.TaxonManager;
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
		
		@Autowired
		private ChecklistUploadTask(TaxonManager taxonManager, GenericResourceManager<ChecklistResource> checklistResourceManager) {
			super(taxonManager, checklistResourceManager);
		}


		@Override
		public void prepare() {
			super.prepare();
			resource = loadResource();
		}
		
		
		public int taskTypeId() {
			return TASK_TYPE_ID;
		}

	}
