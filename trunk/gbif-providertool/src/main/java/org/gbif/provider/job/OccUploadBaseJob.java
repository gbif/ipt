package org.gbif.provider.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.gbif.logging.log.I18nDatabaseAppender;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ExtensionRecord;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.ConfigUtil;
import org.gbif.provider.util.TabFileWriter;
import org.gbif.provider.util.ZipUtil;
import org.gbif.scheduler.scheduler.Launchable;

public abstract class OccUploadBaseJob implements Job{
	protected static final Log log = LogFactory.getLog(OccDbUploadJob.class);
	protected static I18nLog logdb = I18nLogFactory.getLog(OccDbUploadJob.class);

	public static final String RESOURCE_ID = "resourceId";
	public static final String USER_ID = "userId";
	public static final String MAX_RECORDS = "maxRecords";
	
	protected UploadEventManager uploadEventManager;
	protected CoreRecordManager<DarwinCore> darwinCoreManager;
	protected ExtensionRecordManager extensionRecordManager;
	protected ResourceManager<OccurrenceResource> occResourceManager;
	protected Map<Long, String> status = new HashMap<Long, String>();

	protected OccUploadBaseJob(UploadEventManager uploadEventManager, CoreRecordManager<DarwinCore> darwinCoreManager, 
			ExtensionRecordManager extensionRecordManager, ResourceManager<OccurrenceResource> occResourceManager) {
		super();
		this.uploadEventManager = uploadEventManager;
		this.darwinCoreManager = darwinCoreManager;
		this.extensionRecordManager = extensionRecordManager;
		this.occResourceManager = occResourceManager;
	}
	
	
	public String status(Long resourceId){
		return status.get(resourceId);
	}

	public void launch(Map<String, Object> seed) {
		String webappDir = (String) seed.get(Launchable.WEBAPP_DIR);
		ConfigUtil.setWebappDir(webappDir);
		try {
			log.info("Starting "+this.getClass().getSimpleName() +" with seed "+seed);
			
			Long resourceId = Long.valueOf(seed.get(RESOURCE_ID).toString());
			try{
				Long userId = Long.valueOf(seed.get(USER_ID).toString());
				MDC.put(I18nDatabaseAppender.MDC_USER, userId);
			} catch (NumberFormatException e) {
				String[] params = {RESOURCE_ID, USER_ID, seed.toString()};
				logdb.error("{0} or {1} in seed is no Integer {2}", params, e);
			}
			MDC.put(I18nDatabaseAppender.MDC_GROUP_ID, JobUtils.getJobGroup(resourceId));

			// set sourceId to jobID
			Integer sourceId = null;
			if (seed.get(Launchable.JOB_ID) != null && !seed.get(Launchable.JOB_ID).equals("null")){
				try{
					sourceId = Integer.valueOf(seed.get(Launchable.JOB_ID).toString());
					MDC.put(I18nDatabaseAppender.MDC_SOURCE_ID, sourceId);
				} catch (NumberFormatException e) {
					String[] params = {Launchable.JOB_ID, seed.toString()};
					logdb.warn("{0} in seed is no Integer {1}", params, e);
				}
			}
			MDC.put(I18nDatabaseAppender.MDC_SOURCE_TYPE, getSourceType());

			Integer maxRecords = null;
			if (seed.get(MAX_RECORDS) != null && seed.get(MAX_RECORDS).toString().trim().toLowerCase().equals("null")){
				try{
					maxRecords = Integer.valueOf(seed.get(MAX_RECORDS).toString());
				} catch (NumberFormatException e) {
					String[] params = {MAX_RECORDS, seed.toString()};
					logdb.warn("{0} in seed is no Integer {1}", params, e);
				}
			}
			//TODO: set this in the scheduler constructor???
			//MDC.put(I18nDatabaseAppender.MDC_INSTANCE_ID, null);

			
			// get resource
			OccurrenceResource resource = occResourceManager.get(resourceId);

			// track upload in upload event metadata (mainly statistics)
			UploadEvent coreEvent = new UploadEvent();
			coreEvent.setJobSourceId(sourceId);
			coreEvent.setJobSourceType(getSourceType());
			coreEvent.setExecutionDate(new Date());
			coreEvent.setResource(resource);
			
			
			// clear old data/events
			File dataDir = resource.getDataDir();
			FileUtils.deleteDirectory(dataDir);
			log.info("Removed old occurrence data dir "+dataDir.getAbsolutePath());
			FileUtils.forceMkdir(dataDir);
			// clear old log events too?
			log.info("Old log events are kept");
			
			List<File> dumpFiles = new ArrayList<File>();
			
			// try to upload records
			try {
				Map<String, Long> idMap = new HashMap<String, Long>();
				
				// prepare core import source. Source is implementation specific
				ImportSource source = this.getCoreImportSource(seed, resource, maxRecords);
				// run import of core into db & dump file
				try {
					File coreFile = uploadCore(source, resource, coreEvent, idMap);
					dumpFiles.add(coreFile);
				} finally {
					source.close();
				}
				
				// upload further extensions one by one
				for (ViewMappingBase vm : resource.getExtensionMappings().values()){
					Extension ext = vm.getExtension();
					//  prepare import source
					source = this.getImportSource(seed, resource, ext, maxRecords);
					// run import into db & dump file
					try {
						File extFile = uploadExtension(source, idMap, resource, ext);
						dumpFiles.add(extFile);
					} finally {
						source.close();
					}
					source.close();
				}
			} catch (Exception e) {
				logdb.error("Error uploading data", e);
				e.printStackTrace();
			}
			
			// zip all files into single archive
			File archive = resource.getDumpArchiveFile();
			archive.createNewFile();
			ZipUtil.zipFiles(dumpFiles, archive);			
			
		} catch (NumberFormatException e) {
			String[] params = {RESOURCE_ID, seed.toString()};
			logdb.error("{0} in seed is no Integer {1}", params, e);
		} catch (Exception e) {
			logdb.error("Error occurred while running "+this.getClass().getSimpleName(), e);
		}		
	}
	
	abstract protected ImportSource getCoreImportSource(Map<String, Object> seed, OccurrenceResource resource, Integer maxRecords) throws ImportSourceException;

	abstract protected ImportSource getImportSource(Map<String, Object> seed, OccurrenceResource resource, Extension extension, Integer maxRecords) throws ImportSourceException;

	protected File uploadCore(ImportSource source, OccurrenceResource resource, UploadEvent event, Map<String, Long> idMap) throws InterruptedException {
				log.info("Uploading occurrence core for resource "+resource.getTitle());
				File out = null;
				try {
					// create new tab file
					TabFileWriter writer = prepareTabFile(resource, resource.getCoreMapping());
					out = writer.getFile();
					
					// keep track of the following statistics for UploadEvent
					int recordsUploaded = 0;
					int recordsDeleted = 0;
					int recordsChanged = 0;
					int recordsAdded = 0;
					int recordsErroneous = 0;

					// make sure in the finally section that writer is closed and upload event is created properly.
					// for individual record exception there is another inner try/catch
					try{
						// flag all previously existing records as deleted before updating/inserting new ones
						darwinCoreManager.flagAllAsDeleted(resource);

						// go through source records one by one
						for (ImportRecord rec : source){
							// check if thread should shutdown...
							if (Thread.interrupted()) {
							    throw new InterruptedException();
							}			

							 // get darwincore record based on this core record alone. no exceptions here!
							 DarwinCore dwc = DarwinCore.newInstance(rec);								

							 try {
								// get previous record or null if it didnt exist yet based on localID and resource
								DarwinCore oldRecord = darwinCoreManager.findByLocalId(rec.getLocalId(), resource.getId());
								
								// attach to the occurrence resource
								dwc.setResource(resource);								
								// assign managed properties
								updateManagedProperties(dwc, oldRecord);
								
								// check if new record version is different from old one
								if (oldRecord != null && oldRecord.hashCode() == dwc.hashCode() && oldRecord.equals(dwc)){
									// same record. just reset isDeleted flag of old record and thats it!
									oldRecord.setDeleted(false);
									dwc = darwinCoreManager.save(oldRecord);
									
								}else if (oldRecord!=null){
									// modified record that existed before.
									// copying all new properties to oldRecord is too cumbersome, so
									// remove old record and save new one, preserving its GUID (copied in updateManagedProperties() )
									dwc.setModified(new Date());									
									darwinCoreManager.remove(oldRecord.getId());
									dwc = darwinCoreManager.save(dwc);									
									// increase counter of changed records
									recordsChanged++;
									
								}else{
									// new record that didnt exist before. Just save new dwc
									dwc.setModified(new Date());
									dwc = darwinCoreManager.save(dwc);
									// increase counter of added records
									recordsAdded++;
								}
								
								// increase counter for uploaded records no matter if this record is new or modified. 
								recordsUploaded++;

								// write record to tab file
								writer.write(dwc.getDataMap());

							} catch (Exception e) {
								recordsErroneous++;
								logdb.error(String.format("Error uploading record %s of resource %s", rec.getId(), resource.getTitle()), e);
							}
							
							// keep track of upload status outside of this method so that we can create services for it
							status.put(resource.getId(), String.valueOf(recordsUploaded));
							
							// clear session cache once in a while...
							if (recordsUploaded % 1000 == 0){
								log.debug(recordsUploaded+" uploaded for resource "+resource.getId());
								darwinCoreManager.flush();
							}

							// the new darwin core id (managed by hibernate) used for all other extensions
							idMap.put(rec.getLocalId(), dwc.getId());
						}
					} finally {
						// flush and close writer/file
						writer.close();
						// update resource and upload event statistics
						int existingRecords = 0;
						if (resource.getLastUpload()!=null){
							existingRecords = resource.getLastUpload().getRecordsUploaded(); 
						}
						recordsDeleted = existingRecords+recordsAdded-recordsUploaded;
						// logging
						log.info(String.format("Core upload of %s records to cache done. %s deleted, %s added and %s records changed. %s bad records were skipped.",recordsUploaded, recordsDeleted, recordsAdded, recordsChanged, recordsErroneous));
						// store event
						event.setRecordsAdded(recordsAdded);
						event.setRecordsChanged(recordsChanged);
						event.setRecordsDeleted(recordsDeleted);
						event.setRecordsUploaded(recordsUploaded);		
						event.setRecordsErroneous(recordsErroneous);		
						// save upload event
						event = uploadEventManager.save(event);
						// update resource properties
						resource.setLastUpload(event);
						occResourceManager.save(resource);
						
						// reset status
						status.put(resource.getId(), String.format("%s done.", recordsUploaded));
					}

				} catch (IOException e) {
					log.error("Couldnt open tab file. Upload aborted", e);
					throw new InterruptedException();
				}
				
				return out;
			}

	private void updateManagedProperties(DarwinCore dwc, DarwinCore oldRecord) {
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
			dwc.setLink(String.format("%s/%s", dwc.getResource().getRecordResolverEndpoint(), dwc.getGuid()));
		}
	}

	protected File uploadExtension(ImportSource source, Map<String, Long> idMap, OccurrenceResource resource, Extension extension) throws InterruptedException {
		File out = null;
		try {
			// create new tab file
			TabFileWriter writer = prepareTabFile(resource, extension);

			try{
				out = writer.getFile();
				// Do we need a
				for (ImportRecord rec : source){
					// check if thread should shutdown...
					if (Thread.interrupted()) {
					    throw new InterruptedException();
					}					
					Long coreId = idMap.get(rec.getLocalId());
					if (coreId == null){
						String[] paras = {rec.getLocalId(), extension.getName(), resource.getId().toString()};
						//FIXME: use i18n job logging ???
						log.warn("uploadManager.unknownLocalId" +paras.toString());
					}else{
						// TODO: check if record has changed
						ExtensionRecord extRec = ExtensionRecord.newInstance(rec);
						extensionRecordManager.insertExtensionRecord(extRec);
						// see if darwin core record is affected, e.g. geo extension => coordinates
						if (extension.getId() == DarwinCore.GEO_EXTENSION_ID){
							// this is the geo extension!
							DarwinCore dwc = darwinCoreManager.get(coreId);
						}
					}
				}
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			log.error("Couldnt open tab file. Upload aborted", e);
			throw new InterruptedException();
		}
		
		return out;
	}
	
	protected static TabFileWriter prepareTabFile(OccurrenceResource resource, ViewCoreMapping coreViewMapping) throws IOException{
		List<String> additionalHeader = new ArrayList<String>();
		return prepareTabFile(resource, coreViewMapping.getExtension(), additionalHeader);
	}
	protected static TabFileWriter prepareTabFile(OccurrenceResource resource, Extension extension) throws IOException{
		return prepareTabFile(resource, extension, null);
	}
	protected static TabFileWriter prepareTabFile(OccurrenceResource resource, Extension extension, List<String> additionalHeader) throws IOException{
		// create new file, overwriting existing one
		File file = resource.getDumpFile(extension);
		file.createNewFile();

		List<String> header = new ArrayList<String>();
		// add core record id first as first column
		header.add(CoreRecord.ID_COLUMN_NAME);
		header.add(CoreRecord.MODIFIED_COLUMN_NAME);
		// add only existing mapped concepts
		ViewMappingBase mapping = resource.getExtensionMapping(extension);
		if (mapping == null){
			throw new IllegalArgumentException(String.format("Resource %s does not have the extension %s mapped",resource.getTitle(), extension.getName()));
		}
		for (ExtensionProperty prop : mapping.getMappedProperties()){
			header.add(prop.getName());
		}
		header.addAll(additionalHeader);
		TabFileWriter writer = new TabFileWriter(file, header);
		return writer;
	}

}
