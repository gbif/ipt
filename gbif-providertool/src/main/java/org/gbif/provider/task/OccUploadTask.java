package org.gbif.provider.task;

	import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.OccStatManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.CacheMap;
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
		private CacheMap<String, Taxon> taxonCache = new CacheMap<String, Taxon>(1000);
		private static final List<Rank> dwcRanks; 
		  static  
		  {  
		    List<Rank> ranks = new ArrayList<Rank>();
		    ranks .add( Rank.Kingdom );  
		    ranks .add( Rank.Phylum );  
		    ranks .add( Rank.Class );  
		    ranks .add( Rank.Order );  
		    ranks .add( Rank.Family );  
		    ranks .add( Rank.Genus );  
		    ranks .add( Rank.Species );  
		    ranks .add( Rank.InfraSpecies );  
		    dwcRanks = Collections.unmodifiableList(ranks);  
		  }  
		private CacheMap<String, Region> regionCache = new CacheMap<String, Region>(1000);
		
		@Autowired
		private OccStatManager occStatManager;
		@Autowired
		private TaxonManager taxonManager;
		@Autowired
		private RegionManager regionManager;

		
		@Autowired
		private OccUploadTask(DarwinCoreManager dwcManager, OccResourceManager resourceManager) {
			super(dwcManager, resourceManager);
		}


		@Override
		protected void prepareHandler(OccurrenceResource resource) {
			recWithCoordinates=0;
			recWithCountry=0;
			recWithAltitude=0;
			recWithDate=0;

			bbox=new BBox();
			occByRegionAndTaxon = new HashMap<Region, Map<Taxon, OccStatByRegionAndTaxon>>();
			
			resource = loadResource();
		}
		
		
		@Override
		protected void recordHandler(DarwinCore dwc){
			// extract taxon
			extractTaxon(dwc);
			// extract region
			extractRegion(dwc);
			
			
			// stats
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
		
		

		private Region extractRegion(DarwinCore dwc) {
			Region region = null;
			dwc.setRegion(region);
			return region;			
		}


		private Taxon extractTaxon(DarwinCore dwc) {
			Taxon taxon=null;
			String path = dwc.getTaxonomyPath();
			if (taxonCache.containsKey(path)){
				// taxon exists already. use persistent one for dwc
				taxon = taxonCache.get(path);
			}else{
				// cache might be overflown. Look into db before we create a new redundant taxon
				//FIXME: check db
				
				if (taxon == null){
					// try to "insert" the entire taxonomic hierarchy starting with kingdom
					Taxon parent = null;
					Rank lastUsedRank = null;
					String currPathPrefix="";
					int numDelimiter = dwcRanks.size()-1;
					for (Rank rank : dwcRanks){
						String currSciName = StringUtils.trimToEmpty(dwc.getHigherTaxon(rank));
						currPathPrefix += currSciName + "|";
						numDelimiter -= 1;
						if (currSciName.length()>0){
							// this rank does exist
							lastUsedRank = rank;
							String currMpath = currPathPrefix + StringUtils.repeat("|", numDelimiter)+currSciName; 
							if (taxonCache.containsKey(currMpath)){
								// use existing taxon as parent
								parent = taxonCache.get(currMpath); 
							}else{
								// non existing taxon. create new one
								Taxon newParent = Taxon.newInstance(resource);
								// link into hierarchy if this is not a root taxon
								newParent.setParent(parent);
								newParent.setMpath(currMpath);
								newParent.setScientificName(currSciName);
								newParent.setRank(rank.toString());
								newParent.setDwcRank(rank);
								newParent = taxonManager.save(newParent);
								taxonCache.put(currMpath, newParent);				
								parent = newParent; 
							}
						}
					}
					taxon = Taxon.newInstance(resource);
					// link into hierarchy if this is not a root taxon
					taxon.setParent(parent);
					taxon.setMpath(path);
					taxon.setScientificName(dwc.getScientificName());
					taxon.setRank(dwc.getInfraspecificRank());
					// cant figure out rank of terminal taxon
					taxon.setDwcRank(Rank.TerminalTaxon); 
					taxon = taxonManager.save(taxon);
					taxonCache.put(path, taxon);				
					parent = taxon; 
				}
			}
			
			dwc.setTaxon(taxon);
			return taxon;			
		}


		@Override
		protected void closeHandler(OccurrenceResource resource){
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
