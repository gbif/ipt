package org.gbif.provider.task;

	import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.factory.DarwinCoreFactory;
import org.gbif.provider.model.factory.RegionFactory;
import org.gbif.provider.model.factory.TaxonFactory;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccStatManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.service.impl.GeoserverManagerImpl;
import org.gbif.provider.util.CacheMap;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

	/**
	 * Tha main task responsible for uploading raw data into the cache and doing simple preprocessing while iterating through the ImportSource.
	 * Any further secondary postprocessing is done with the help of a second post processing task that this task will automatically schedule once its done.
	 * @author markus
	 *
	 */
	/**
	 * @author markus
	 *
	 * @param <T>
	 * @param <R>
	 */
	public abstract class ImportTask<R extends DataResource> extends TaskBase<UploadEvent, R>{
		protected static final List<Rank> higherRanks;
		protected static final List<RegionType> higherGeography;
		static  
		  {  
		    List<Rank> ranks = new ArrayList<Rank>(Rank.DARWIN_CORE_HIGHER_RANKS);
		    Collections.reverse(ranks);
		    higherRanks = Collections.unmodifiableList(ranks);  
		  }  
		static  
		  {  
		    List<RegionType> regionTypes = new ArrayList<RegionType>(RegionType.DARWIN_CORE_REGIONS);
		    regionTypes.add(RegionType.Locality);
		    Collections.reverse(regionTypes);
		    higherGeography = Collections.unmodifiableList(regionTypes);  
		  }
		
		private Map<String, Long> idMap = new HashMap<String, Long>();
		private boolean extractHigherTaxonomy;
		// keep track of the following statistics for UploadEvent
		private UploadEvent event;
		private Integer recordsUploaded;
		private Integer recordsChanged;
		private Integer recordsAdded;
		private Integer recordsErroneous;
		// upload status
		protected String currentActivity;
		private AtomicInteger currentProcessed;
		private AtomicInteger currentErroneous;
		private R resource;
		private Date lastLogDate;
		// transient annotations
		private Set<Annotation> annotations = new HashSet<Annotation>();
		
		// managers
		@Autowired
		protected FullTextSearchManager fullTextSearchManager;
		@Autowired
		private ImportSourceFactory importSourceFactory;
		@Autowired
		private CacheManager cacheManager;
		@Autowired
		private DataArchiveManager dataArchiveManager;
		@Autowired
		private DarwinCoreFactory dwcFactory;
		@Autowired
		private RegionFactory regionFactory;
		@Autowired
		private TaxonFactory taxonFactory;		
		@Autowired
		private DarwinCoreManager dwcManager;
		@Autowired
		private ExtensionRecordManager extensionRecordManager;
		@Autowired
		private EmlManager emlManager;
		protected BBox bbox;
		protected int numRegions;
		protected int numTaxa;
		private CacheMap<String, Taxon> taxonCache = new CacheMap<String, Taxon>(2500);
		private LinkedList<Taxon> newTaxa = new LinkedList<Taxon>();
		private CacheMap<String, Region> regionCache = new CacheMap<String, Region>(1000);
		private LinkedList<Region> newRegions = new LinkedList<Region>();
		@Autowired
		private UploadEventManager uploadEventManager;
		@Autowired
		@Qualifier("viewMappingManager")
		private GenericManager<ExtensionMapping> viewMappingManager;
		@Autowired
		protected GeoserverManagerImpl geoTools;
		@Autowired
		protected OccStatManager occStatManager;
		@Autowired
		protected TaxonManager taxonManager;
		@Autowired
		protected RegionManager regionManager;

		
		public ImportTask(GenericResourceManager<R> resourceManager){
			super(resourceManager);
		}
		
		public final UploadEvent call() throws Exception{
			
			try {
				lastLogDate = new Date();

				// prepare upload, removing previous records, calling prepare of helper tasks
				prepare();
				Date now = new Date();
				log.info(String.format("Import preparation took %s ms", (now.getTime()-lastLogDate.getTime())));
				lastLogDate = now;
				
				// run import of core into db, calling a subclass handler per record and finally at the end
				uploadCore();
				now = new Date();
				log.info(String.format("Import core took %s ms", (now.getTime()-lastLogDate.getTime())));
				lastLogDate = now;
				
				// upload further extensions one by one
				for (ExtensionMapping vm : resource.getExtensionMappings()){
					// run import into db
					uploadExtension(vm);
					now = new Date();
					log.info(String.format("Importing %s records of extension %s took %s ms", vm.getRecTotal(), vm.getExtension().getName(), (now.getTime()-lastLogDate.getTime())));
					lastLogDate = now;
				}
				
				close();
				now = new Date();
				log.info(String.format("Import closing took %s ms", (now.getTime()-lastLogDate.getTime())));
				lastLogDate = now;
				
			} catch (InterruptedException e) {
				// thread was interrupted. Try to exit nicely by removing all potentially corrupt data.
				prepare();
				annotationManager.annotateResource(resource, "Upload task was interrupted/canceled. Partially existing upload was removed");
				throw e;
			}
			
			return event;		
		}
		

		private void prepare() {
			// prepare this instance
			currentActivity="Preparing import";
			currentProcessed = new AtomicInteger(0);
			currentErroneous = new AtomicInteger(0);
			
			bbox=new BBox();

			resource= loadResource();
			
			// call subclass handler first, might need to remove dependend records before removing the core records
			prepareHandler(resource);
			
			// remove all previous upload artifacts
			dwcManager.removeAll(resource);
			cacheManager.prepareUpload(getResourceId());

			recordsUploaded = 0;
			recordsErroneous = null;
			recordsChanged = 0;
			recordsAdded = 0;

			// track upload in upload event metadata (mainly statistics)
			event = new UploadEvent();
			event.setJobSourceId(this.hashCode());
			event.setJobSourceType(taskTypeId());
			event.setExecutionDate(new Date());
			event.setResource(resource);

			// extract higher taxonomy or use pointers aka higherTaxonID?
			if (resource.getCoreMapping().hasMappedProperty("HigherTaxonID") || resource.getCoreMapping().hasMappedProperty("HigherTaxon")){
				extractHigherTaxonomy=false;
			}else{
				extractHigherTaxonomy=true;
			}
		}
		
		
		private void close() {
			log.info(String.format("Closing upload for resource %s", getTitle() ));					
			currentProcessed.set(0);
			currentErroneous.set(0);

			// lookup parentID, basionymID and acceptedID
			currentActivity = "Resolving higher taxa";
			taxonManager.lookupParentTaxa(getResourceId());

			currentActivity = "Resolving accepted taxa";
			taxonManager.lookupAcceptedTaxa(getResourceId());

			currentActivity = "Resolving basionyms";
			taxonManager.lookupBasionymTaxa(getResourceId());

			// create nested set indices
			currentActivity = "Creating taxonomy index";
			taxonManager.buildNestedSet(getResourceId());
			
			currentActivity = "Creating region index";
			regionManager.buildNestedSet(getResourceId());

			// call subclass handler for specific postprocessing
			closeHandler(resource);
			log.info(String.format("Core upload of %s records to cache done. %s deleted, %s added and %s records changed. %s bad records were skipped.", event.getRecordsUploaded(), event.getRecordsDeleted(), event.getRecordsAdded(), event.getRecordsChanged(), event.getRecordsErroneous()));

			// update resource and upload event statistics
			currentActivity = "Generating resource statistics";			
			event.setResource(resource);
			Eml eml = emlManager.load(resource);
			event.setEmlVersion(eml.getEmlVersion());
			event.setRecordsAdded(recordsAdded);
			event.setRecordsChanged(recordsChanged);
			event.setRecordsDeleted(resource.getRecTotal()+recordsAdded-recordsUploaded);
			event.setRecordsUploaded(recordsUploaded);		
			event.setRecordsErroneous(recordsErroneous);
			event = uploadEventManager.save(event);			
			
			// update resource properties
			resource.setLastUpload(event);
			resource.setRecTotal(recordsUploaded);			
			resourceManager.save(resource);
			resourceManager.flush();
			
			// update cache db statistics
			cacheManager.analyze();
			
			//
			// create data archive
			//
			currentActivity = "Creating data archive";
			try {
				File archive = dataArchiveManager.createArchive(resource);
				log.info("DarwinCore archive created at "+archive.getAbsolutePath());
			} catch (IOException e) {
				log.error("Could not write DarwinCore archive", e);
				this.annotationManager.annotateResource(resource, "Could not write DarwinCore archive. IOException");
			} catch (Exception e) {
				log.error("Could not create DarwinCore archive", e);
				this.annotationManager.annotateResource(resource, "Could not create DarwinCore archive");
			}
			
			//
			// build the full text indexes
			//
			fullTextSearchManager.buildDataResourceIndex(resource);
		}

		private void setFinalExtensionStats(Extension ext){
			ExtensionMapping view = resource.getExtensionMapping(ext);				
			view.setRecTotal(currentProcessed.get()-currentErroneous.get());
			viewMappingManager.save(view);
		}

		@Transactional(readOnly=false, noRollbackFor={Exception.class})
		private File uploadCore() throws InterruptedException {
			log.info("Starting upload of core records for resource "+ getTitle());					
			currentActivity="Uploading "+resource.getCoreMapping().getExtension().getName();
			currentProcessed.set(0);
			currentErroneous.set(0);

			ImportSource source;
			File out = null;
			try {
				// prepare core import source. Can be a file or database source to iterate over in read-only mode
				source = importSourceFactory.newInstance(resource, resource.getCoreMapping());
				
				// make sure in the finally section that source & writer is closed and upload event is created properly.
				// for individual record exception there is another inner try/catch
				try{
					// go through source records one by one
					for (ImportRecord irec : source){
						// keep all annotations for a single record til the end so we can add the correct record GUID to them
						if (irec == null){
							continue;
						}
						// keep track of processed source records
						currentProcessed.addAndGet(1);
						
						// check if thread should shutdown...
						if (Thread.currentThread().isInterrupted()){
							throw new InterruptedException(String.format("Cache import task for resource %s was interrupted externally", getResourceId()));
						}

						 // get darwincore record based on this core record alone. no exceptions here!
						DarwinCore dwc = dwcFactory.build(resource, irec, annotations);
						if (dwc == null){
							currentErroneous.addAndGet(1);
							annotationManager.badCoreRecord(resource, null, "Seems to be an empty record or missing source ID. Line "+String.valueOf(currentProcessed.get()));
							continue;
						}
						try {
							// get previous record or null if it didnt exist yet based on sourceID and resource
							DarwinCore oldRecord = dwcManager.findBySourceId(irec.getSourceId(), getResourceId());
							
							// check if sourceID was unique. All old records should have deleted flag=true
							// so if deleted is false, the same sourceID was inserted before already!
							if (oldRecord != null && !oldRecord.isDeleted()){
								annotationManager.badCoreRecord(resource, irec.getSourceId(), "Duplicate source ID");
							}
							// assign managed properties
							updateCoreProperties(dwc, oldRecord);
							
							// extract taxon
							try{
								extractTaxon(dwc);
							} catch (Exception e) {
								annotationManager.badCoreRecord(resource, dwc.getSourceId(), "Error extracting taxon: "+e.toString());
							}

							// extract region
							try{
								extractRegion(dwc);
							} catch (Exception e) {
								annotationManager.badCoreRecord(resource, dwc.getSourceId(), "Error extracting region: "+e.toString());
							}
							
							// allow specific actions per record
							recordHandler(dwc);

							// save core record
							dwc = persistRecord(dwc, oldRecord);
							
							// persist record annotations with good GUID
							for (Annotation anno : annotations){
								anno.setGuid(dwc.getGuid());
								annotationManager.save(anno);
							}
							annotations.clear();
							
							// set stats per record. Saving of final resource stats is done in the close() section
							recordsUploaded++;

						} catch (ObjectNotFoundException e2){
							annotationManager.badCoreRecord(resource, irec.getSourceId(), "Unkown source ID: "+e2.toString());
						} catch (Exception e) {
							currentErroneous.addAndGet(1);
							annotationManager.badCoreRecord(resource, irec.getSourceId(), "Unkown error: "+e.toString());
						}
						
						// clear session cache once in a while...
						if (currentProcessed.get() > 0 && currentProcessed.get() % 100 == 0){
							dwcManager.flush();
						}
						if (currentProcessed.get() > 0 && currentProcessed.get() % 1000 == 0){
							log.debug(status());
						}
					}
				} finally {
					// store final numbers in normal Integer so that the AtomicNumber can be reset by other extension uploads
					recordsErroneous = currentErroneous.get(); 
					source.close();
				}

			} catch (ImportSourceException e) {
				annotationManager.annotateResource(resource, "Couldn't open import source. Import aborted: "+e.toString());
				throw new InterruptedException();
			}

			return out;
		}


		private DarwinCore persistRecord(DarwinCore record, DarwinCore oldRecord) {
			// check if new record version is different from old one
			if (oldRecord != null && oldRecord.hashCode() == record.hashCode()){
				// same record. reset isDeleted flag of old record
				oldRecord.setDeleted(false);
				// also assign to old record cause this one gets saved if the record stays the same. And the old taxon or region has been deleted already...
//				oldRecord.setTaxon(record.getTaxon()); 
//				oldRecord.setRegion(record.getRegion());
				record = dwcManager.save(oldRecord);
				
			}else if (oldRecord!=null){
				// modified record that existed before.
				// copy all properties to oldRecord is too cumbersome, so
				// remove old record and save new one, preserving its GUID (copied in updateManagedProperties() )
				dwcFactory.copyPersistentProperties(oldRecord, record);
				oldRecord.setDeleted(false);
				oldRecord.setModified(new Date());									
				record = dwcManager.save(oldRecord);									
				// increase counter of changed records
				recordsChanged += 1;
				
			}else{
				// new record that didnt exist before. Just save new dwc
				record.setModified(new Date());
				record = dwcManager.save(record);
				// increase counter of added records
				recordsAdded += 1;
			}
			
			// the new darwin core id (managed by hibernate) used for all other extensions
			idMap.put(record.getSourceId(), record.getId());

			return record;
		}


		private void updateCoreProperties(DarwinCore record, DarwinCore oldRecord) {
			// assign new GUID if none exists
			if (record.getGuid() == null){
				// if old version exists already reuse the previously assigned GUID
				if (oldRecord != null){
					record.setGuid(oldRecord.getGuid());
				}else{
					record.setGuid(UUID.randomUUID().toString());					
				}
			}			
			// assign link to detailed record if not existing
			if (record.getLink() == null){
				record.setLink(cfg.getDetailUrl(record));
			}
		}

		@Transactional(readOnly=false, noRollbackFor={Exception.class})
		private File uploadExtension(ExtensionMapping vm) throws InterruptedException {
			String extensionName = vm.getExtension().getName();
			Extension extension = vm.getExtension();
			log.info(String.format("Starting upload of %s extension for resource %s", extensionName, getTitle() ));					
			File out = null;
			// keep track of records for each extension and then store the totals in the viewMapping.
			// once extension is uploaded this counter will be reset by the next extension.
			// used to feed status()
			currentActivity="Uploading "+extensionName;
			currentProcessed.set(0);
			currentErroneous.set(0);

			try {
				//  prepare import source
				ImportSource source = importSourceFactory.newInstance(resource, vm);

				// catch any errors after we opened the source to close it properly and set the extension statistics at least
				try{
					// Do we need a
					for (ImportRecord rec : source){
						currentProcessed.addAndGet(1);
						// check if thread should shutdown...
						if (Thread.currentThread().isInterrupted()){
							throw new InterruptedException(String.format("Cache import task for resource %s was interrupted externally", getResourceId()));
						}
						if (rec == null || rec.getSourceId()==null){
							currentErroneous.addAndGet(1);
							annotationManager.badExtensionRecord(resource, extension, null, "Seems to be an empty record or missing source ID. Line "+String.valueOf(currentProcessed.get()));
							continue;
						}
						Long coreId = idMap.get(rec.getSourceId());
						rec.setId(coreId);
						if (coreId == null){
							annotationManager.badExtensionRecord(resource, extension, rec.getSourceId(), "Unkown source ID");
						}else{
							// TODO: check if record has changed
							try {
								ExtensionRecord extRec = ExtensionRecord.newInstance(rec);
								extensionRecordHandler(extRec);
								extensionRecordManager.insertExtensionRecord(extRec);
							} catch (Exception e) {
								e.printStackTrace();
								currentErroneous.addAndGet(1);
								annotationManager.badExtensionRecord(resource, extension, rec.getSourceId(), "Unkown error: "+e.toString());
							}
						}

						// debug status
						if (currentProcessed.get() > 0 && currentProcessed.get() % 1000 == 0){
							log.debug(status());
						}
					}
				} catch (Exception e){
					annotationManager.annotateResource(resource, String.format("Unknown error uploading extension %s. Extension skipped", extensionName));
					log.error("Unknown error uploading extension "+extensionName, e);
				} finally {
					setFinalExtensionStats(extension);
				}
			// outer catch/try for import soruce only
			} catch (ImportSourceException e) {
				annotationManager.annotateResource(resource, String.format("Couldn't open import source for extension %s. Extension skipped", extensionName));
				log.error("Couldn't open import source for extension "+extensionName, e);
			}
			return out;
		}
		
		
		public final synchronized String status() {
			String subclassInfo = StringUtils.trimToEmpty(statusHandler());
			if (subclassInfo.length()>0){
				subclassInfo = ". "+subclassInfo;
			}
			String coreInfo = "";
			String recordStatus ="";
			if (currentProcessed.get() > 0){
				// core uploaded already
				recordStatus = String.format(". %s records processed", currentProcessed.get());
			}
			if (recordsUploaded != null){
				// core uploaded already
				coreInfo = String.format(". %s core records in total", recordsUploaded);
			}
			if (currentProcessed!=null){
				return String.format("%s%s%s%s", currentActivity, recordStatus, subclassInfo, coreInfo);
			}else{
				return "Waiting for upload to start.";
			}
		}

		/** Hook for doing initial preperations before processing the resource, e.g. clearing statistics or removing old files 
		 * @param resource
		 */
		abstract protected String statusHandler();
		
		
		/** Hook for doing initial preperations before processing the resource, e.g. clearing statistics or removing old files 
		 * @param resource
		 */
		abstract protected void prepareHandler(R resource);

		/** Hook for working with a single record provided for subclasses 
		 * @param record
		 */
		abstract protected void recordHandler(DarwinCore record);
		abstract protected void extensionRecordHandler(ExtensionRecord extRec);

		/** Hook for doing final processing for the entire resource, e.g. setting statistics gathered via the record hook 
		 * @param resource
		 */
		abstract protected void closeHandler(R resource);

		/**
		 * Extract unique Taxa from Darwin Core record, either the terminal taxon, but potentially also the higher taxonomy.
		 * If dwc:HigherTaxon or dwc:HigherTaxonID are mapped, the explicit higher taxonomy is *not* extracted.
		 * Otherwise Each higher taxon becomes a new taxon if not already existing.
		 * @param dwc
		 * @return
		 */
		protected Taxon extractTaxon(DarwinCore dwc) {
			// first extract terminal taxon			
			String path = dwc.getTaxonomyPath();
			Taxon terminalTaxon=findPersistentTaxon(path);
			if (terminalTaxon == null){
				// taxon doesnt exist yet. create it based on ScientificName
				terminalTaxon = taxonFactory.build(dwc);		
				// also extract higher taxonomy? set via prepare()
				if (extractHigherTaxonomy){
					// extract higher taxonomy if not already extracted and link terminal taxon into hierarchy
					// try to find lowest persistent higher taxon 
					// create new higher taxa as we go up and havent found a persistent one yet
					Taxon parent = null;
					newTaxa.clear();
					boolean persistentParentFound = false;
					// first see if infraspecific epitheton exists. 
					// This means there also is a species which we will use as a higher taxon too
					if (dwc.getInfraspecificEpithet()!=null){
						parent = findPersistentTaxon(dwc.getTaxonomyPath(Rank.Species));
						if (parent==null){
							// cant find species. create new taxon and go further up the ranks
							parent = taxonFactory.build(dwc, Rank.Species);
							// we cant assign a parent yet, therefor put it on the new taxon stack 
							// and save it later once we reach a persistent taxon or the kingdom
							newTaxa.add(parent);
						}else{
							persistentParentFound=true;
						}
						terminalTaxon.setParent(parent);
					}
					if (!persistentParentFound){
						for (Rank rank : higherRanks){
							if (dwc.getHigherTaxonName(rank)==null){
								continue;
							}
							parent = findPersistentTaxon(dwc.getTaxonomyPath(rank));
							if (parent!=null){
								persistentParentFound=true;
							}else{
								parent = taxonFactory.build(dwc, rank);
								newTaxa.add(parent);
							}
							if (terminalTaxon.getParent()==null){
								terminalTaxon.setParent(parent);
							}
							if (persistentParentFound){
								break;
							}
						}
					}
					// save new taxa
					if (!persistentParentFound && !newTaxa.isEmpty()){
						// no persistent taxon found in entire hierarchy.
						// use highest taxon as a new taxonomy root
						parent = newTaxa.removeLast();
						saveTaxon(parent);
					}
					// save all other new taxa if there are any
					Taxon newTaxon;
					while(!newTaxa.isEmpty()){
						newTaxon = newTaxa.removeLast();
						newTaxon.setParent(parent);
						parent = saveTaxon(newTaxon);
					}
				}
				
				// finally save the terminal taxon to be linked to the darwin core record
				saveTaxon(terminalTaxon);
			}
			
			dwc.setTaxon(terminalTaxon);
			return terminalTaxon;			
		}

		private Taxon findPersistentTaxon(String mpath) {
			if (taxonCache.containsKey(mpath)){
				return taxonCache.get(mpath);
			}else{
				// cache is limited, so we need to check the db too to make sure it doesnt exist
				return taxonManager.getByMaterializedPath(getResourceId(), mpath);
			}
		}



		private Taxon saveTaxon(Taxon taxon) {
			taxonManager.save(taxon);
			taxonCache.put(taxon.getMpath(), taxon);
			numTaxa++;
			return taxon;
		}

		protected Region extractRegion(DarwinCore dwc) {			
			Region region = null;
			boolean persistentParentFound=false;
			newRegions.clear();
			for (RegionType regionType : higherGeography){
				if (dwc.getHigherGeographyName(regionType)==null){
					continue;
				}
				region = findPersistentRegion(dwc.getGeographyPath(regionType));
				if (region!=null){
					persistentParentFound=true;
					break;
				}else{
					region = regionFactory.build(dwc, regionType);
					newRegions.add(region);
				}
			}
		
			// save new taxa
			if (!persistentParentFound && !newRegions.isEmpty()){
				// no persistent region found in entire hierarchy.
				// use highest region as a new geography root region
				region = newRegions.removeLast();
				saveRegion(region);
			}
			// save all other new regions if there are any
			Region newRegion;
			while(!newRegions.isEmpty()){
				newRegion = newRegions.removeLast();
				newRegion.setParent(region);
				region = saveRegion(newRegion);
			}
				
			dwc.setRegion(region);
			return region;			
		}

		private Region findPersistentRegion(String mpath) {
			if (regionCache.containsKey(mpath)){
				return regionCache.get(mpath);
			}else{
				// cache is limited, so we need to check the db too to make sure it doesnt exist
				return regionManager.getByMaterializedPath(getResourceId(), mpath);
			}
		}

		private Region saveRegion(Region region) {
			regionManager.save(region);
			regionCache.put(region.getMpath(), region);
			numRegions++;
			return region;
		}

	}
