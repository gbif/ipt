package org.gbif.provider.task;

	import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.ImportSourceFactory;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.CoreRecordFactory;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.UploadEventManager;
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
	public abstract class ImportTask<T extends CoreRecord, R extends DataResource> extends TaskBase<UploadEvent, R>{
		private Map<String, Long> idMap = new HashMap<String, Long>();
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

		
		@Autowired
		private ImportSourceFactory importSourceFactory;
		@Autowired
		private CacheManager cacheManager;
		@Autowired
		private DataArchiveManager dataArchiveManager;
		@Autowired
		private CoreRecordFactory coreRecordFactory;
		private CoreRecordManager<T> coreRecordManager;
		@Autowired
		private ExtensionRecordManager extensionRecordManager;
		@Autowired
		private UploadEventManager uploadEventManager;
		@Autowired
		@Qualifier("viewMappingManager")
		private GenericManager<ViewMappingBase> viewMappingManager;

		
		public ImportTask(CoreRecordManager<T> coreRecordManager, GenericResourceManager<R> resourceManager){
			super(resourceManager);
			this.coreRecordManager = coreRecordManager;
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
				for (ViewExtensionMapping vm : resource.getExtensionMappings()){
					// run import into db & dump file
					try {
						uploadExtension(vm);
						now = new Date();
						log.info(String.format("Import extension %s took %s ms", vm.getExtension().getName(), (now.getTime()-lastLogDate.getTime())));
						lastLogDate = now;
					} catch (ImportSourceException e) {
						// dont do nothing. Error is logged and core is uploaded. Skip this extension
					} catch (IOException e) {
						// dont do nothing. Error is logged and core is uploaded. Skip this extension
					}
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

			resource= loadResource();
			
			// call subclass handler first, might need to remove dependend records before removing the core records
			prepareHandler(resource);
			
			// remove all previous upload artifacts
			coreRecordManager.removeAll(resource);
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
			
		}
		
		
		private void close() throws IOException {
			log.info(String.format("Closing upload for resource %s", getTitle() ));					
			currentProcessed.set(0);
			currentErroneous.set(0);

			//
			// resource stats
			//
			currentActivity = "Generating resource statistics";
			// update resource and upload event statistics
			event.setResource(resource);			
			event.setRecordsAdded(recordsAdded);
			event.setRecordsChanged(recordsChanged);
			event.setRecordsDeleted(resource.getRecTotal()+recordsAdded-recordsUploaded);
			event.setRecordsUploaded(recordsUploaded);		
			event.setRecordsErroneous(recordsErroneous);
			event = uploadEventManager.save(event);			
			// update resource properties
			resource.setLastUpload(event);
			resource.setRecTotal(recordsUploaded);
			
			// call subclass handler for specific postprocessing
			closeHandler(resource);
			resourceManager.save(resource);
			log.info(String.format("Core upload of %s records to cache done. %s deleted, %s added and %s records changed. %s bad records were skipped.", event.getRecordsUploaded(), event.getRecordsDeleted(), event.getRecordsAdded(), event.getRecordsChanged(), event.getRecordsErroneous()));

			
			//
			// create data archive
			//
			currentActivity = "Creating data archive";
			File archive = dataArchiveManager.createArchive(resource);
			log.info("Data archive created at "+archive.getAbsolutePath());
		}

		private void setFinalExtensionStats(Extension ext){
			ViewMappingBase view = resource.getExtensionMapping(ext);				
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
				source = this.getImportSource();
				
				// make sure in the finally section that source & writer is closed and upload event is created properly.
				// for individual record exception there is another inner try/catch
				try{
					// go through source records one by one
					for (ImportRecord irec : source){
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
						T record = (T) coreRecordFactory.build(resource, irec);
						if (record == null){
							currentErroneous.addAndGet(1);
							annotationManager.badCoreRecord(resource, null, "Seems to be an empty record or missing local ID. Line "+String.valueOf(currentProcessed.get()));
							continue;
						}
						try {
							// get previous record or null if it didnt exist yet based on localID and resource
							T oldRecord = coreRecordManager.findByLocalId(irec.getLocalId(), getResourceId());
							
							// check if localID was unique. All old records should have deleted flag=true
							// so if deleted is false, the same localID was inserted before already!
							if (oldRecord != null && !oldRecord.isDeleted()){
								annotationManager.badCoreRecord(resource, irec.getLocalId(), "Duplicate local ID");
							}
							// assign managed properties
							updateCoreProperties(record, oldRecord);
							
							// save core record
							record = persistRecord(record, oldRecord);
							
							// set stats per record. Saving of final resource stats is done in the close() section
							recordsUploaded++;
							recordHandler(record);

						} catch (ObjectNotFoundException e2){
							annotationManager.badCoreRecord(resource, irec.getLocalId(), "Unkown local ID: "+e2.toString());
						} catch (Exception e) {
							currentErroneous.addAndGet(1);
							annotationManager.badCoreRecord(resource, irec.getLocalId(), "Unkown error: "+e.toString());
						}
						
						// clear session cache once in a while...
						if (currentProcessed.get() > 0 && currentProcessed.get() % 100 == 0){
							log.debug(status());
							coreRecordManager.flush();
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


		private T persistRecord(T record, T oldRecord) {
			// check if new record version is different from old one
			if (oldRecord != null && oldRecord.hashCode() == record.hashCode()){
				// same record. reset isDeleted flag of old record
				oldRecord.setDeleted(false);
				// also assign to old record cause this one gets saved if the record stays the same. And the old taxon or region has been deleted already...
//				oldRecord.setTaxon(record.getTaxon()); 
//				oldRecord.setRegion(record.getRegion());
				record = coreRecordManager.save(oldRecord);
				
			}else if (oldRecord!=null){
				// modified record that existed before.
				// copy all properties to oldRecord is too cumbersome, so
				// remove old record and save new one, preserving its GUID (copied in updateManagedProperties() )
				coreRecordFactory.copyPersistentProperties(oldRecord, record);
				oldRecord.setDeleted(false);
				oldRecord.setModified(new Date());									
				record = coreRecordManager.save(oldRecord);									
				// increase counter of changed records
				recordsChanged += 1;
				
			}else{
				// new record that didnt exist before. Just save new dwc
				record.setModified(new Date());
				record = coreRecordManager.save(record);
				// increase counter of added records
				recordsAdded += 1;
			}
			
			// the new darwin core id (managed by hibernate) used for all other extensions
			idMap.put(record.getLocalId(), record.getId());

			return record;
		}


		private void updateCoreProperties(T record, T oldRecord) {
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
		private File uploadExtension(ViewExtensionMapping vm) throws InterruptedException, ImportSourceException, IOException {
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
				ImportSource source = this.getImportSource(vm.getExtension());

				try{
					// Do we need a
					for (ImportRecord rec : source){
						currentProcessed.addAndGet(1);
						// check if thread should shutdown...
						if (Thread.currentThread().isInterrupted()){
							throw new InterruptedException(String.format("Cache import task for resource %s was interrupted externally", getResourceId()));
						}
						if (rec == null || rec.getLocalId()==null){
							currentErroneous.addAndGet(1);
							annotationManager.badExtensionRecord(resource, extension, null, "Seems to be an empty record or missing local ID. Line "+String.valueOf(currentProcessed.get()));
							continue;
						}
						Long coreId = idMap.get(rec.getLocalId());
						rec.setId(coreId);
						if (coreId == null){
							annotationManager.badExtensionRecord(resource, extension, rec.getLocalId(), "Unkown local ID");
						}else{
							// TODO: check if record has changed
							try {
								ExtensionRecord extRec = ExtensionRecord.newInstance(rec);
								extensionRecordHandler(extRec);
								extensionRecordManager.insertExtensionRecord(extRec);
							} catch (Exception e) {
								e.printStackTrace();
								currentErroneous.addAndGet(1);
								annotationManager.badExtensionRecord(resource, extension, rec.getLocalId(), "Unkown error: "+e.toString());
							}
						}
					}
				} finally {
					setFinalExtensionStats(extension);
				}
			} catch (ImportSourceException e) {
				annotationManager.annotateResource(resource, "Couldn't open import source for extension %s. Extension skipped: "+e.toString());
				throw e;
			} catch (Exception e){
				e.printStackTrace();
			}
			
			return out;
		}
		


		private ImportSource getImportSource() throws ImportSourceException{
			return getImportSource(null);
		}

		private ImportSource getImportSource(Extension extension) throws ImportSourceException{
			ViewMappingBase vm = null;
			ImportSource source; 

			if (extension == null){
				vm = resource.getCoreMapping();				
			}else{
				vm = resource.getExtensionMapping(extension);				
			}
			
			if (vm == null){
				String extName="";
				if (extension != null){
					extName="extension "+extension.getName();
				}else{
					extName="core extension";
				}
				throw new ImportSourceException("No mapping exists for "+extName);
			}			
			
			source = importSourceFactory.newInstance(resource, vm);
			return source;
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
		abstract protected void recordHandler(T record);
		abstract protected void extensionRecordHandler(ExtensionRecord extRec);

		/** Hook for doing final processing for the entire resource, e.g. setting statistics gathered via the record hook 
		 * @param resource
		 */
		abstract protected void closeHandler(R resource);


	}
