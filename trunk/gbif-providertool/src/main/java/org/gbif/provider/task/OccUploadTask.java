package org.gbif.provider.task;

	import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

	/**
	 * Tha main task responsible for uploading raw data into the cache and doing simple preprocessing while iterating through the ImportSource.
	 * Any further secondary postprocessing is done with the help of a second post processing task that this task will automatically schedule once its done.
	 * @author markus
	 *
	 */
	public class OccUploadTask extends ImportTask<OccurrenceResource> {
		public static final int TASK_TYPE_ID = 1;
		@Autowired
		private OccResourceManager occResourceManager;
		protected OccurrenceResource occResource;
		protected int recWithCoordinates;
		protected int recWithCountry;
		protected int recWithAltitude;
		protected int recWithDate;

		
		@Autowired
		private OccUploadTask(OccResourceManager resourceManager) {
			super(resourceManager);
			this.occResourceManager = resourceManager;
		}


		@Override
		protected void prepareHandler(OccurrenceResource resource) {
			recWithCoordinates=0;
			recWithCountry=0;
			recWithAltitude=0;
			recWithDate=0;
		}
		
		@Override
		protected void recordHandler(DarwinCore dwc){
			// potentially transform coordinates
			// FIXME: dont transform coordinates for now as I have no idea how to get the SpatialReferenceID from the datum alone... 
			//			dwc.getGeodeticDatum();			

			// STATISTICS
			// increase stats counter
			if (dwc.getLocation().isValid()){
				// update bbox for resource
				bbox.expandBox(dwc.getLocation());
				//FIXME: when multiple extension records for the same dwcore record exist this counter will count all instead of just one!!!
				// might need to do a count via SQL after upload is done ...
				recWithCoordinates++;
			}
			if(StringUtils.trimToNull(dwc.getCountry())!=null){
				recWithCountry++;
			}
			if(dwc.getCollected()!=null){
				recWithDate++;
			}
			if(dwc.getElevation()!=null){
				recWithAltitude++;
			}

		}

		@Override
		protected void extensionRecordHandler(ExtensionRecord extRec) {
		}

		@Override
		protected void closeHandler(OccurrenceResource resource){
			// build resource stats
			currentActivity = "Building resource stats";
			occResourceManager.setResourceStats(resource);			
			currentActivity = "Inserting occurrence statistics by region and taxon";
			occStatManager.updateRegionAndTaxonStats(resource);
			
			// update resource properties
			resource.setRecWithCoordinates(recWithCoordinates);
			resource.setRecWithCountry(recWithCountry);
			resource.setRecWithAltitude(recWithAltitude);
			resource.setRecWithDate(recWithDate);
			resource.setBbox(bbox);
			
			// reseed geowebcache
			geoTools.updateGeowebcache(resource);
		}


		@Override
		protected String statusHandler() {
			return String.format("%s taxa, %s regions created", numTaxa, numRegions);
		}


		public int taskTypeId() {
			return TASK_TYPE_ID;
		}

	}
