package org.gbif.provider.task;

	import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.geo.TransformationUtils;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.OccStatManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

	/**
	 * Tha main task responsible for uploading raw data into the cache and doing simple preprocessing while iterating through the ImportSource.
	 * Any further secondary postprocessing is done with the help of a second post processing task that this task will automatically schedule once its done.
	 * @author markus
	 *
	 */
	public class OccUploadTask extends ImportTask<DarwinCore, OccurrenceResource> {
		public static final int TASK_TYPE_ID = 1;
		// resource stats
		private OccurrenceResource resource;
		private int recWithCoordinates;
		private int recWithCountry;
		private int recWithAltitude;
		private int recWithDate;
		private BBox bbox;
		// not stored in resource
		private Map<Region, Map<Taxon, OccStatByRegionAndTaxon>> occByRegionAndTaxon;
		
		@Autowired
		private TransformationUtils wgs84Util;
		@Autowired
		private OccStatManager occStatManager;

		
		@Autowired
		private OccUploadTask(
				DarwinCoreManager dwcManager,
				OccResourceManager resourceManager,
				@Qualifier("geographyBuilder") RecordPostProcessor<DarwinCore, Set<Region>, OccurrenceResource> geographyBuilder,
				@Qualifier("taxonomyBuilder") RecordPostProcessor<DarwinCore, Set<DwcTaxon>, OccurrenceResource> taxonomyBuilder) {
			super(dwcManager, resourceManager, geographyBuilder, taxonomyBuilder);
		}


		@Override
		public void prepare() {
			super.prepare();
			recWithCoordinates=0;
			recWithCountry=0;
			recWithAltitude=0;
			recWithDate=0;

			bbox=new BBox();
			occByRegionAndTaxon = new HashMap<Region, Map<Taxon, OccStatByRegionAndTaxon>>();
			
			resource = loadResource();
		}
		
		
		@Override
		protected void statsPerRecord(DarwinCore dwc){
			if(StringUtils.trimToNull(dwc.getCountry())!=null){
				recWithCountry++;
			}
			if(dwc.getDateCollected()!=null){
				recWithDate++;
			}
			if(dwc.getMinimumElevationInMetersAsInteger()!=null){
				recWithAltitude++;
			}
			
			// counts per taxon & region
			Region reg = dwc.getRegion();
			Taxon tax = dwc.getTaxon();
			OccStatByRegionAndTaxon stat;
			Map<Taxon, OccStatByRegionAndTaxon> taxMap;
			if (!occByRegionAndTaxon.containsKey(reg)){
				taxMap = new HashMap<Taxon, OccStatByRegionAndTaxon>();
				occByRegionAndTaxon.put(reg, taxMap);
			}else{
				taxMap = occByRegionAndTaxon.get(reg);				
			}
			if (!taxMap.containsKey(tax)){
				stat = new OccStatByRegionAndTaxon();
				stat.setResource(resource);
				stat.setRegion(reg);
				stat.setTaxon(tax);
				stat.setNumOcc(1);
				taxMap.put(tax, stat);
			}else{
				stat = taxMap.get(tax);
				stat.incrementNumOcc();
			}
			
		}
		
		

		@Override
		protected void finalHandler(OccurrenceResource resource){
			// occByRegionAndTaxon
			int occStatCount=0;
			for (Map<Taxon, OccStatByRegionAndTaxon> taxMap : occByRegionAndTaxon.values()){
				for (OccStatByRegionAndTaxon stat : taxMap.values()){
					try {
						occStatManager.save(stat);
						occStatCount++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			log.debug(occStatCount + " occurrence stats by region & taxon inserted");
			
			// update resource properties
			resource.setRecWithCoordinates(recWithCoordinates);
			resource.setRecWithCountry(recWithCountry);
			resource.setRecWithAltitude(recWithAltitude);
			resource.setRecWithDate(recWithDate);
			resource.setBbox(bbox);
		}


		private void uploadExtension(ViewExtensionMapping vm) {
//			// see if darwin core record is affected, e.g. geo extension => coordinates
//			if (vm.getExtension().getId().equals(DarwinCore.GEO_EXTENSION_ID)){
//				// this is the geo extension!
//				DarwinCore dwc = darwinCoreManager.get(coreId);
//				if (dwc.updateWithGeoExtension(extRec)){
//					// update bbox
//					bbox.expandBox(dwc.getLocation());
//					// potentially transform coordinates
//					String geodatum=extRec.getPropertyValue(DarwinCore.GEODATUM_PROP);
//					// FIXME: dont transform coordinates for now as I have no idea how to get the SpatialReferenceID from the datum alone... 
//					if (false && geodatum!=null && dwc.getLocation()!=null){
//						// FIXME: keep hasmap of used datums and their transformer. 
//						// Its expensive to create those
//						// Wgs84Transformer t = wgs84Util.getWgs84Transformer(geodatum);
//						try {
//							wgs84Util.transformIntoWGS84(dwc.getLocation(), geodatum);
//						} catch (FactoryException e) {
//							log.debug("Can't recognise geodatic datum "+geodatum);
//						} catch (TransformException e) {
//							log.warn("Can't transform coordinates with geodatic datum "+geodatum);
//						}											 
//					}
//					darwinCoreManager.save(dwc);
//					// increase stats counter
//					if (dwc.getLocation().isValid()){
//						//FIXME: when multiple extension records for the same dwcore record exist this counter will count all instead of just one!!!
//						// might need to do a count via SQL after upload is done ...
//						recWithCoordinates++;
//						// update Taxon bbox stats
//						if (dwc.getTaxon()!=null){
//							dwc.getTaxon().expandBox(dwc.getLocation());			
//						}
//						if (dwc.getRegion()!=null){
//							dwc.getRegion().expandBox(dwc.getLocation());			
//						}
//					}
//				}
//			}
		}
		
		
		public int taskTypeId() {
			return TASK_TYPE_ID;
		}

	}
