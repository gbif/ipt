package org.gbif.provider.upload;

	import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.datasource.impl.FileImportSource;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ExtensionRecord;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.ProviderCfgManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.TabFileWriter;
import org.gbif.provider.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

	/**
	 * Tha main task responsible for uploading raw data into the cache and doing simple preprocessing while iterating through the ImportSource.
	 * Any further secondary postprocessing is done with the help of a second post processing task that this task will automatically schedule once its done.
	 * @author markus
	 *
	 */
	@Transactional(readOnly=false)
	public class OccUploadTask extends TaskBase implements Task<UploadEvent>{
		public static final int SOURCE_TYPE_ID = 1;
		private Map<String, Long> idMap = new HashMap<String, Long>();
		private List<File> dumpFiles = new ArrayList<File>();
		// keep track of the following statistics for UploadEvent
		private UploadEvent event;
		private Integer recordsUploaded;
		private Integer recordsDeleted;
		private Integer recordsChanged;
		private Integer recordsAdded;
		private Integer recordsErroneous;
		private int recWithCoordinates;
		private int recWithCountry;
		private int recWithAltitude;
		private int recWithDate;
		private BBox bbox;
		private AtomicInteger currentUploaded;
		private AtomicInteger currentErroneous;
		private String currentExtension;

		
		@Autowired
		private DarwinCoreManager darwinCoreManager;
		@Autowired
		private ExtensionRecordManager extensionRecordManager;
		@Autowired
		private UploadEventManager uploadEventManager;
		
		@Autowired
		@Qualifier("taxonomyBuilder")
		private RecordPostProcessor<DarwinCore, Set<DwcTaxon>> taxonomyBuilder;
		@Autowired
		@Qualifier("geographyBuilder")
		private RecordPostProcessor<DarwinCore, Set<Region>> geographyBuilder;

		
		public UploadEvent call() throws Exception{
			
			initLogging(SOURCE_TYPE_ID);
			
			try {
				// prepare upload, removing previous records, calling prepare of helper tasks
				prepare();
				
				// run import of core into db, create taxa and regions & dump file in one go!
				File coreFile = uploadCore();
				dumpFiles.add(coreFile);
				
				// upload further extensions one by one
				for (ViewMappingBase vm : getResource().getExtensionMappings()){
					Extension ext = vm.getExtension();
					// run import into db & dump file
					try {
						File extFile = uploadExtension(ext);
						dumpFiles.add(extFile);
					} catch (ImportSourceException e) {
						// dont do nothing. Error is logged and core is uploaded. Skip this extension
					} catch (IOException e) {
						// dont do nothing. Error is logged and core is uploaded. Skip this extension
					}
				}
				
				close();
				
			} catch (InterruptedException e) {
				// thread was interrupted. Try to exit nicely by removing all potentially corrupt data.
				prepare();
				logdb.fatal("log.uploadCanceled", e);
				throw e;
			}
			
			return event;		
		}
		
		private void close() throws IOException {
			log.info(String.format("Closing upload for resource %s", getResource().getTitle() ));					
			taxonomyBuilder.close();
			geographyBuilder.close();
			setStats();
			// zip all files into single archive
			File archive = cfg.getDumpArchiveFile(getResourceId());
			archive.createNewFile();
			log.info("Dump file archive created at "+archive.getAbsolutePath());
			ZipUtil.zipFiles(dumpFiles, archive);						
		}

		private void setStats(){
			// update resource and upload event statistics
			recordsDeleted = getResource().getRecTotal()+recordsAdded-recordsUploaded;
			// store event
			event.setRecordsAdded(recordsAdded);
			event.setRecordsChanged(recordsChanged);
			event.setRecordsDeleted(recordsDeleted);
			event.setRecordsUploaded(recordsUploaded);		
			event.setRecordsErroneous(recordsErroneous);		
			// save upload event
			event = uploadEventManager.save(event);
			
			// update resource properties
			getResource().setLastUpload(event);
			getResource().setRecTotal(recordsUploaded);
			getResource().setRecWithCoordinates(recWithCoordinates);
			getResource().setRecWithCountry(recWithCountry);
			getResource().setRecWithAltitude(recWithAltitude);
			getResource().setRecWithDate(recWithDate);
			getResource().setBbox(bbox);
			occResourceManager.save(getResource());
			
			log.info(String.format("Core upload of %s records to cache done. %s deleted, %s added and %s records changed. %s bad records were skipped.",recordsUploaded, recordsDeleted, recordsAdded, recordsChanged, recordsErroneous));
		}

		private void prepare() {
			currentUploaded = new AtomicInteger(0);
			currentErroneous = new AtomicInteger(0);
			recordsUploaded = null;
			recordsErroneous = null;
			recordsDeleted = 0;
			recordsChanged = 0;
			recordsAdded = 0;

			recWithCoordinates=0;
			recWithCountry=0;
			recWithAltitude=0;
			recWithDate=0;

			bbox=new BBox();
			
			// track upload in upload event metadata (mainly statistics)
			event = new UploadEvent();
			event.setJobSourceId(this.hashCode());
			event.setJobSourceType(SOURCE_TYPE_ID);
			event.setExecutionDate(new Date());
			event.setResource(getResource());
							
			// clear taxa
			taxonomyBuilder.init(getResourceId(), getUserId());
			taxonomyBuilder.prepare();
			// clear regions
			geographyBuilder.init(getResourceId(), getUserId());
			geographyBuilder.prepare();
			
			// TODO: need to remove old uploaded source and generated dump files first? I guess they will be overwritten
			// clear resource stats
			getResource().resetStats();
			occResourceManager.save(getResource());
		}
		
		protected File uploadCore() throws InterruptedException {
			log.info("Starting upload of DarwinCore for resource "+getResource().getTitle());					
			ImportSource source;
			File out = null;
			currentExtension = getResource().getCoreMapping().getExtension().getName();
			currentUploaded.set(0);
			currentErroneous.set(0);
			try {
				// create new tab file
				TabFileWriter writer = prepareTabFile(getResource().getCoreMapping());
				out = writer.getFile();
				// prepare core import source. Can be a file or database source to iterate over in read-only mode
				source = this.getCoreImportSource();
				
				// make sure in the finally section that source & writer is closed and upload event is created properly.
				// for individual record exception there is another inner try/catch
				try{
					// flag all previously existing records as deleted before updating/inserting new ones
					darwinCoreManager.flagAllAsDeleted(getResource());

					// go through source records one by one
					for (ImportRecord rec : source){
						// check if thread should shutdown...
						if (Thread.currentThread().isInterrupted()){
							throw new InterruptedException("Occurrence upload task was interrupted externally");
						}

						 // get darwincore record based on this core record alone. no exceptions here!
						 DarwinCore dwc = DarwinCore.newInstance(rec);								

						 try {
							// get previous record or null if it didnt exist yet based on localID and resource
							DarwinCore oldRecord = darwinCoreManager.findByLocalId(rec.getLocalId(), getResourceId());
							
							// assign managed properties
							updateDwcProperties(dwc, oldRecord);
							// extract and assign taxa
							taxonomyBuilder.processRecord(dwc);
							// extract and assign regions
							geographyBuilder.processRecord(dwc);
							
							// check if new record version is different from old one
							if (oldRecord != null && oldRecord.hashCode() == dwc.hashCode()){
								// same record. reset isDeleted flag of old record
								oldRecord.setDeleted(false);
								// also assign to old record cause this one gets saved if the record stays the same. And the old taxon or region has been deleted already...
								oldRecord.setTaxon(dwc.getTaxon()); 
								oldRecord.setRegion(dwc.getRegion());
								dwc = darwinCoreManager.save(oldRecord);
								
							}else if (oldRecord!=null){
								// modified record that existed before.
								// copying all new properties to oldRecord is too cumbersome, so
								// remove old record and save new one, preserving its GUID (copied in updateManagedProperties() )
								dwc.setModified(new Date());									
								darwinCoreManager.remove(oldRecord.getId());
								darwinCoreManager.flush();
								dwc = darwinCoreManager.save(dwc);									
								// increase counter of changed records
								recordsChanged += 1;
								
							}else{
								// new record that didnt exist before. Just save new dwc
								dwc.setModified(new Date());
								dwc = darwinCoreManager.save(dwc);
								// increase counter of added records
								recordsAdded += 1;
							}
							
							// count statistics
							currentUploaded.addAndGet(1);
							if(StringUtils.trimToNull(dwc.getCountry())!=null){
								recWithCountry++;
							}
							if(StringUtils.trimToNull(dwc.getEarliestDateCollected())!=null){
								//FIXME: no date type, so might contain rubbish
								recWithDate++;
							}
							if(dwc.getMinimumElevationInMetersAsInteger()!=null){
								recWithAltitude++;
							}
							
							// write record to tab file
							writer.write(dwc.getDataMap());

						} catch (InterruptedException e) {
							throw e;
						} catch (Exception e) {
							currentErroneous.addAndGet(1);
							e.printStackTrace();
							logdb.warn("log.uploadRecord", new String[]{rec.getLocalId().toString(), getResource().getTitle()}, e);
						}
						
						// clear session cache once in a while...
						if (currentUploaded.get() > 0 && currentUploaded.get() % 500 == 0){
							log.debug(status());
							darwinCoreManager.flush();
						}

						// the new darwin core id (managed by hibernate) used for all other extensions
						idMap.put(rec.getLocalId(), dwc.getId());
					}
				} finally {
					// store final numbers in normal Integer so that the AtomicNumber can be reset by other extension uploads
					recordsUploaded = currentUploaded.get();
					recordsErroneous = currentErroneous.get(); 
					source.close();
					// flush and close writer/file				
					writer.close();
				}

			} catch (IOException e) {
				logdb.error("log.fileError", e);
				throw new InterruptedException();
			} catch (ImportSourceException e) {
				logdb.error("log.sourceError", e);
				throw new InterruptedException();
			}

			return out;
		}


		private void updateDwcProperties(DarwinCore dwc, DarwinCore oldRecord) {
			// attach to the occurrence resource
			dwc.setResource(getResource());

			// assign new GUID if none exists
			if (dwc.getGuid() == null){
				// if old version exists already reuse the previously assigned GUID
				if (oldRecord != null){
					dwc.setGuid(oldRecord.getGuid());
				}else{
					dwc.setGuid(UUID.randomUUID().toString());					
				}
			}			
			// assign link to detailed record if not existing
			if (dwc.getLink() == null){
				dwc.setLink(cfg.getDetailUrl(dwc));
			}
		}

		private File uploadExtension(Extension extension) throws InterruptedException, ImportSourceException, IOException {
			log.info(String.format("Starting upload of %s extension for resource %s", extension.getName(), getResource().getTitle() ));					
			File out = null;
			// keep track of records for each extension and then store the totals in the viewMapping.
			// once extension is uploaded this counter will be reset by the next extension.
			// used to feed status()
			currentExtension = extension.getName();
			currentUploaded.set(0);
			currentErroneous.set(0);

			try {
				// create new tab file
				TabFileWriter writer = prepareTabFile(extension);
				//  prepare import source
				ImportSource source = this.getImportSource(extension);

				try{
					out = writer.getFile();
					// Do we need a
					for (ImportRecord rec : source){
						// check if thread should shutdown...
						if (Thread.currentThread().isInterrupted()){
							throw new InterruptedException("Occurrence upload task was interrupted externally");
						}
						Long coreId = idMap.get(rec.getLocalId());
						rec.setId(coreId);
						if (coreId == null){
							String[] paras = {rec.getLocalId(), extension.getName(), getResourceId().toString()};
							//FIXME: use i18n job logging ???
							log.warn("uploadManager.unknownLocalId" +paras.toString());
						}else{
							// TODO: check if record has changed
							try {
								ExtensionRecord extRec = ExtensionRecord.newInstance(rec);
								extensionRecordManager.insertExtensionRecord(extRec);
								currentUploaded.addAndGet(1);
								// see if darwin core record is affected, e.g. geo extension => coordinates
								if (extension.getId().equals(DarwinCore.GEO_EXTENSION_ID)){
									// this is the geo extension!
									DarwinCore dwc = darwinCoreManager.get(coreId);
									dwc.updateWithGeoExtension(extRec);
									// update bbox
									bbox.expandBox(dwc.getLocation());
									darwinCoreManager.save(dwc);
									// increase stats counter
									if (dwc.getLocation().isValid()){
										//FIXME: when multiple extension records for the same dwcore record exist this counter will count all instead of just one!!!
										// might need to do a count via SQL after upload is done ...
										recWithCoordinates++;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								currentErroneous.addAndGet(1);
								logdb.warn("log.uploadRecord", new String[]{rec.getLocalId().toString(), getResource().getTitle()}, e);
							}
						}
					}
				} finally {
					writer.close();
				}
			} catch (IOException e) {
				log.error("Couldnt open tab file. Upload aborted", e);
				throw e;
			} catch (ImportSourceException e) {
				logdb.error("log.sourceExtensionError", extension.getName(), e);
				throw e;
			} catch (Exception e){
				e.printStackTrace();
			}
			
			return out;
		}
		
		private TabFileWriter prepareTabFile(ViewCoreMapping coreViewMapping) throws IOException{
			List<String> additionalHeader = new ArrayList<String>();
			return prepareTabFile(coreViewMapping.getExtension(), additionalHeader);
		}
		private TabFileWriter prepareTabFile(Extension extension) throws IOException{
			return prepareTabFile(extension, null);
		}
		private TabFileWriter prepareTabFile(Extension extension, List<String> additionalHeader) throws IOException{
			// create new file, overwriting existing one
			File file = cfg.getDumpFile(getResourceId(), extension);
			// remove previously existing file
			if (file.exists()){
				file.delete();
			}
			file.createNewFile();

			List<String> header = new ArrayList<String>();
			// add core record id first as first column
			header.add(CoreRecord.ID_COLUMN_NAME);
			header.add(CoreRecord.MODIFIED_COLUMN_NAME);
			// add only existing mapped concepts
			ViewMappingBase mapping = getResource().getExtensionMapping(extension);
			if (mapping == null){
				throw new IllegalArgumentException(String.format("Resource %s does not have the extension %s mapped",getResource().getTitle(), extension.getName()));
			}
			for (ExtensionProperty prop : mapping.getMappedProperties()){
				header.add(prop.getName());
			}
			if (additionalHeader!=null){
				header.addAll(additionalHeader);
			}
			TabFileWriter writer = new TabFileWriter(file, header);
			return writer;
		}

		private ImportSource getCoreImportSource() throws ImportSourceException{
			Long resourceId = getResourceId();
			ViewCoreMapping coreViewMapping = getResource().getCoreMapping();
			ImportSource source; 
			//decide whether file or rdbm upload and create import source accordingly
			if (coreViewMapping.isMappedToDatabase()){
				// create rdbms source & set resource context for DatasourceInterceptor
				DatasourceContextHolder.setResourceId(resourceId);
				source = RdbmsImportSource.newInstance(getResource(), coreViewMapping);
			}else{
				source = FileImportSource.newInstance(getResource(), coreViewMapping);
			}
			return source;
		}

		private ImportSource getImportSource(Extension extension) throws ImportSourceException{
			Long resourceId = getResourceId();
			//FIXME: need to decide here whether file or rdbm upload and create import source accordingly 
			// create rdbms source & set resource context for DatasourceInterceptor
			DatasourceContextHolder.setResourceId(resourceId);
			ViewMappingBase vm = getResource().getExtensionMapping(extension);
			if (vm == null){
				throw new ImportSourceException("No mapping exists for extension "+extension.getName());
			}
			RdbmsImportSource source = RdbmsImportSource.newInstance(getResource(), vm);

			return source;
		}

		public synchronized String status() {
			String coreInfo = "";
			if (recordsUploaded != null){
				// core uploaded already
				coreInfo = String.format(", %s occurrences", recordsUploaded);
			}
			return String.format("Uploading %s: %s records with %s, %s%s", currentExtension, currentUploaded.get(), taxonomyBuilder.status(), geographyBuilder.status(), coreInfo);
		}

	}
